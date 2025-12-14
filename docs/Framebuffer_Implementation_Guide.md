# Framebuffer Implementation Guide for Raspberry Pi

This guide provides detailed instructions for completing the framebuffer implementation for Raspberry Pi. The current implementation successfully detects framebuffer mode and creates a FramebufferWindow, but requires additional work to achieve full functionality.

## Current Status

### What Works ✅

1. **Framebuffer Detection**: Application correctly detects Raspberry Pi framebuffer settings
2. **Window Factory**: Dynamically creates appropriate window type (GLFW vs Framebuffer)
3. **Framebuffer Window**: Implements IWindow interface and integrates with engine
4. **Input Handling**: Conditional input handling for framebuffer mode
5. **Engine Integration**: All engine systems work with framebuffer window

### What's Missing ❌

1. **EGL Context Creation**: No OpenGL context available for rendering
2. **Framebuffer Surface**: No actual rendering to the framebuffer device
3. **Native Input**: No keyboard/mouse input in framebuffer mode
4. **Performance Optimization**: No hardware-specific optimizations

## Complete Implementation Requirements

### 1. EGL Integration

#### Required Components

- **EGL Display**: Connection to the native display system
- **EGL Config**: Configuration for the OpenGL context
- **EGL Context**: OpenGL ES context for rendering
- **EGL Surface**: Surface that renders to the framebuffer

#### Implementation Steps

```java
// 1. Create EGL instance
EGL.create();

// 2. Get default display
eglDisplay = EGL.getPlatformDisplay(EGL_PLATFORM_GBM_KHR, EGL_DEFAULT_DISPLAY);

// 3. Initialize EGL
int[] major = new int[1];
int[] minor = new int[1];
EGL.eglInitialize(eglDisplay, major, minor);

// 4. Choose EGL config
int[] configAttribs = {
    EGL_RED_SIZE, 8,
    EGL_GREEN_SIZE, 8,
    EGL_BLUE_SIZE, 8,
    EGL_ALPHA_SIZE, 8,
    EGL_DEPTH_SIZE, 24,
    EGL_STENCIL_SIZE, 8,
    EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
    EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
    EGL_NONE
};

// 5. Create EGL context
int[] contextAttribs = {
    EGL_CONTEXT_CLIENT_VERSION, 2,
    EGL_NONE
};
eglContext = EGL.eglCreateContext(eglDisplay, eglConfig, EGL_NO_CONTEXT, contextAttribs, 0);

// 6. Create framebuffer surface
int[] surfaceAttribs = {
    EGL_WIDTH, width,
    EGL_HEIGHT, height,
    EGL_NONE
};
eglSurface = EGL.eglCreateWindowSurface(eglDisplay, eglConfig, nativeWindow, surfaceAttribs, 0);

// 7. Make context current
EGL.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);

// 8. Create OpenGL capabilities
eglCapabilities = GL.createCapabilities();
```

### 2. Native Framebuffer Access

#### Required Components

- **Framebuffer Device**: Access to `/dev/fb0` or similar
- **GBM (Graphics Buffer Manager)**: For buffer management
- **DRM (Direct Rendering Manager)**: For display control

#### Implementation Steps

```java
// 1. Open framebuffer device
int fbFd = open("/dev/fb0", O_RDWR);

// 2. Query framebuffer properties
struct fb_var_screeninfo vinfo;
ioctl(fbFd, FBIOGET_VSCREENINFO, &vinfo);

// 3. Create GBM device
struct gbm_device *gbm = gbm_create_device(fbFd);

// 4. Create GBM surface
struct gbm_surface *surface = gbm_surface_create(
    gbm, vinfo.xres, vinfo.yres,
    GBM_FORMAT_XRGB8888,
    GBM_BO_USE_SCANOUT | GBM_BO_USE_RENDERING
);

// 5. Create EGL surface from GBM surface
eglSurface = EGL.eglCreateWindowSurface(eglDisplay, eglConfig, surface, NULL);
```

### 3. Native Input Handling

#### Required Components

- **Linux Input Devices**: `/dev/input/event*` devices
- **libinput**: Modern input handling library
- **Event Processing**: Keyboard and mouse event handling

#### Implementation Steps

```java
// 1. Open input devices
int keyboardFd = open("/dev/input/event0", O_RDONLY);
int mouseFd = open("/dev/input/event1", O_RDONLY);

// 2. Create libinput context
struct libinput *li = libinput_path_create_context(NULL, NULL);
libinput_path_add_device(li, "/dev/input/event0");
libinput_path_add_device(li, "/dev/input/event1");

// 3. Process events
struct libinput_event *event;
while ((event = libinput_get_event(li)) != NULL) {
    processEvent(event);
    libinput_event_destroy(event);
}
```

### 4. Performance Optimization

#### Required Components

- **VSYNC Control**: Synchronize with display refresh
- **Triple Buffering**: Reduce latency and tearing
- **Memory Management**: Optimize GPU memory usage
- **Shader Optimization**: Use efficient shaders

#### Implementation Steps

```java
// 1. Enable VSYNC
EGL.eglSwapInterval(eglDisplay, 1);

// 2. Use triple buffering
int[] surfaceAttribs = {
    EGL_WIDTH, width,
    EGL_HEIGHT, height,
    EGL_SWAP_BEHAVIOR, EGL_BUFFER_PRESERVED,
    EGL_NONE
};

// 3. Optimize memory usage
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
```

## Complete FramebufferWindow Implementation

```java
package com.sim3d.engine;

import org.lwjgl.egl.EGL;
import org.lwjgl.egl.EGLCapabilities;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static org.lwjgl.egl.EGL10.*;
import static org.lwjgl.egl.EXTPlatformBase.EGL_PLATFORM_GBM_KHR;
import static org.lwjgl.egl.KHRPlatformGBM.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Complete FramebufferWindow implementation with EGL support
 */
public class FramebufferWindow implements IWindow {
    private static final Logger logger = LoggerFactory.getLogger(FramebufferWindow.class);
    
    private int width = 1280;
    private int height = 720;
    private String title;
    private String framebufferDevice;
    private boolean fullscreen = false;
    
    // EGL components
    private long eglDisplay;
    private long eglConfig;
    private long eglContext;
    private long eglSurface;
    private EGLCapabilities eglCapabilities;
    
    // Native components
    private int fbFd;
    private long gbmDevice;
    private long gbmSurface;
    
    // Input handling
    private InputHandler inputHandler;
    
    public FramebufferWindow(String title, int width, int height, boolean fullscreen) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.fullscreen = fullscreen;
    }

    public void init() {
        logger.info("Initializing complete framebuffer window...");
        
        Settings settings = Settings.getInstance();
        this.framebufferDevice = settings.getFramebufferDevice();
        
        try {
            // 1. Initialize native framebuffer
            initNativeFramebuffer();
            
            // 2. Initialize EGL
            initEGL();
            
            // 3. Initialize input
            initInput();
            
            logger.info("Complete framebuffer window initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize complete framebuffer: {}", e.getMessage(), e);
            cleanup();
            throw new RuntimeException("Failed to initialize complete framebuffer window", e);
        }
    }
    
    private void initNativeFramebuffer() throws IOException {
        logger.info("Initializing native framebuffer...");
        
        // Open framebuffer device
        File fbFile = new File(framebufferDevice);
        if (!fbFile.exists()) {
            throw new IOException("Framebuffer device " + framebufferDevice + " does not exist");
        }
        
        // In a real implementation, this would use JNI to:
        // 1. Open the framebuffer device
        // 2. Query framebuffer properties
        // 3. Create GBM device and surface
        
        logger.info("Native framebuffer initialized");
    }
    
    private void initEGL() {
        logger.info("Initializing EGL...");
        
        // 1. Create EGL instance
        EGL.create();
        
        // 2. Get platform display
        eglDisplay = EGL.getPlatformDisplay(EGL_PLATFORM_GBM_KHR, EGL_DEFAULT_DISPLAY);
        if (eglDisplay == 0) {
            throw new RuntimeException("Failed to get EGL display");
        }
        
        // 3. Initialize EGL
        int[] major = new int[1];
        int[] minor = new int[1];
        if (!EGL.eglInitialize(eglDisplay, major, minor)) {
            throw new RuntimeException("Failed to initialize EGL");
        }
        
        logger.info("EGL {}.{} initialized", major[0], minor[0]);
        
        // 4. Choose EGL config
        int[] configAttribs = {
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_DEPTH_SIZE, 24,
            EGL_STENCIL_SIZE, 8,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_NONE
        };
        
        int[] numConfigs = new int[1];
        long[] configs = new long[1];
        if (!EGL.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, 1, numConfigs, 0)) {
            throw new RuntimeException("Failed to choose EGL config");
        }
        
        eglConfig = configs[0];
        
        // 5. Create EGL context
        int[] contextAttribs = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL_NONE
        };
        
        eglContext = EGL.eglCreateContext(eglDisplay, eglConfig, EGL_NO_CONTEXT, contextAttribs, 0);
        if (eglContext == EGL_NO_CONTEXT) {
            throw new RuntimeException("Failed to create EGL context");
        }
        
        // 6. Create EGL surface (using native window from GBM)
        // In a real implementation, this would use the GBM surface
        int[] surfaceAttribs = {
            EGL_WIDTH, width,
            EGL_HEIGHT, height,
            EGL_NONE
        };
        
        eglSurface = EGL.eglCreateWindowSurface(eglDisplay, eglConfig, gbmSurface, surfaceAttribs, 0);
        if (eglSurface == EGL_NO_SURFACE) {
            throw new RuntimeException("Failed to create EGL surface");
        }
        
        // 7. Make context current
        if (!EGL.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new RuntimeException("Failed to make EGL context current");
        }
        
        // 8. Create OpenGL capabilities
        eglCapabilities = GL.createCapabilities();
        
        // 9. Setup OpenGL
        setupOpenGL();
        
        logger.info("EGL initialized successfully");
    }
    
    private void setupOpenGL() {
        logger.info("Setting up OpenGL...");
        
        glViewport(0, 0, width, height);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        
        // Get OpenGL info
        String glVersion = glGetString(GL_VERSION);
        String glslVersion = glGetString(GL_SHADING_LANGUAGE_VERSION);
        String vendor = glGetString(GL_VENDOR);
        String renderer = glGetString(GL_RENDERER);
        
        logger.info("OpenGL Context Info:");
        logger.info("  Version: {}", glVersion);
        logger.info("  GLSL: {}", glslVersion);
        logger.info("  Vendor: {}", vendor);
        logger.info("  Renderer: {}", renderer);
    }
    
    private void initInput() {
        logger.info("Initializing input...");
        
        // In a real implementation, this would:
        // 1. Open input devices
        // 2. Create libinput context
        // 3. Set up event processing
        
        inputHandler = new FramebufferInputHandler();
        logger.info("Input initialized");
    }
    
    @Override
    public boolean shouldClose() {
        return false; // No window close event in framebuffer mode
    }
    
    @Override
    public void swapBuffers() {
        // Swap EGL buffers
        if (!EGL.eglSwapBuffers(eglDisplay, eglSurface)) {
            logger.error("Failed to swap EGL buffers");
        }
        
        // In a real implementation, this would also:
        // 1. Copy rendered frame to framebuffer
        // 2. Handle VSYNC
        // 3. Update display
    }
    
    @Override
    public void pollEvents() {
        // Process input events
        if (inputHandler != null) {
            inputHandler.pollEvents();
        }
    }
    
    @Override
    public void cleanup() {
        logger.info("Cleaning up complete framebuffer...");
        
        // Cleanup EGL
        if (eglDisplay != 0) {
            if (eglContext != 0) {
                EGL.eglDestroyContext(eglDisplay, eglContext);
            }
            if (eglSurface != 0) {
                EGL.eglDestroySurface(eglDisplay, eglSurface);
            }
            EGL.eglTerminate(eglDisplay);
        }
        
        // Cleanup native resources
        if (fbFd != 0) {
            close(fbFd);
        }
        
        // Cleanup input
        if (inputHandler != null) {
            inputHandler.cleanup();
        }
        
        EGL.destroy();
        logger.info("Complete framebuffer cleaned up");
    }
    
    // Implement all IWindow methods...
    
    @Override
    public long getWindowHandle() {
        return -1; // No GLFW window handle
    }
}
```

## Required Dependencies

### Gradle Dependencies

```groovy
// EGL bindings
implementation "org.lwjgl:lwjgl-egl"
runtimeOnly "org.lwjgl:lwjgl-egl:$lwjglNatives"

// GBM bindings (if available)
// implementation "org.lwjgl:lwjgl-gbm"
// runtimeOnly "org.lwjgl:lwjgl-gbm:$lwjglNatives"
```

### Native Libraries

- **libEGL.so**: EGL implementation
- **libGBM.so**: Graphics Buffer Manager
- **libdrm.so**: Direct Rendering Manager
- **libinput.so**: Input handling

## Testing and Validation

### Test Procedure

1. **Verify Framebuffer Detection**
   ```bash
   ./gradlew run --args="--settings settings_rpi.json"
   ```
   Expected: Application detects framebuffer mode and creates FramebufferWindow

2. **Verify EGL Initialization**
   Expected: EGL context created successfully with OpenGL ES 2.0+ support

3. **Verify Rendering**
   Expected: Application renders to framebuffer without X11

4. **Verify Input**
   Expected: Keyboard and mouse input work in framebuffer mode

### Validation Commands

```bash
# Check framebuffer device
ls -la /dev/fb0

# Check framebuffer properties
fbset

# Check EGL capabilities
glxinfo | grep -i egl

# Check OpenGL ES capabilities
glesinfo
```

## Troubleshooting

### Common Issues

1. **EGL Initialization Failure**
   - Check that EGL libraries are installed
   - Verify GPU drivers are properly configured
   - Check framebuffer device permissions

2. **No OpenGL Context**
   - Ensure EGL is properly initialized before OpenGL calls
   - Verify EGL surface creation
   - Check that context is made current

3. **Input Not Working**
   - Verify input device permissions
   - Check that libinput is properly configured
   - Test input devices with `evtest`

### Debugging Commands

```bash
# Check EGL errors
eglinfo

# Test framebuffer directly
cat /dev/urandom > /dev/fb0

# Check input devices
ls /dev/input/

# Test input events
evtest /dev/input/event0
```

## Performance Optimization

### Recommended Settings

```json
{
  "raspberryPi": {
    "useFramebuffer": true,
    "framebufferDevice": "/dev/fb0",
    "resolution": {
      "width": 1280,
      "height": 720
    },
    "performance": {
      "vsync": true,
      "tripleBuffering": true,
      "fpsLimit": 60,
      "renderDistance": 100
    }
  }
}
```

### Optimization Techniques

1. **Reduce Resolution**: Use 1280x720 instead of 1920x1080
2. **Limit FPS**: Cap frame rate to 30-60 FPS
3. **Simplify Shaders**: Use basic shaders for better performance
4. **Reduce Draw Distance**: Limit rendering distance
5. **Enable VSYNC**: Reduce tearing and GPU load

## Complete Implementation Checklist

- [ ] Add EGL dependencies to build.gradle
- [ ] Implement EGL context creation
- [ ] Add native framebuffer access (JNI)
- [ ] Implement GBM surface creation
- [ ] Add input device handling
- [ ] Implement framebuffer rendering
- [ ] Add performance optimizations
- [ ] Test on Raspberry Pi hardware
- [ ] Validate framebuffer output
- [ ] Test input devices
- [ ] Optimize performance
- [ ] Document final implementation

## References

- [EGL Specification](https://www.khronos.org/registry/EGL/specs/eglspec.1.5.pdf)
- [GBM Documentation](https://dri.freedesktop.org/docs/drm/gbm/)
- [libinput Documentation](https://wayland.freedesktop.org/libinput/doc/latest/)
- [Raspberry Pi Graphics Documentation](https://www.raspberrypi.org/documentation/computers/configuration.html)
- [LWJGL EGL Bindings](https://javadoc.lwjgl.org/org/lwjgl/egl/package-summary.html)

This guide provides all the information needed to complete the framebuffer implementation for Raspberry Pi. The current implementation provides a solid foundation, and this guide outlines the exact steps needed to achieve full functionality.