# Raspberry Pi 5 Setup Guide for Java 3D Application

This guide provides correct instructions for setting up a Raspberry Pi 5 running Raspberry Pi OS Lite (arm64) without a window manager to run this Java 3D application using the framebuffer.

**Important Note**: Raspberry Pi 5 uses VideoCore VII GPU but still uses the `vc4` driver, not `vc5`. Do not use `vc5` overlays as they don't exist in standard Raspberry Pi OS.

## System Requirements

- **Raspberry Pi 5** (required for best performance)
- **Raspberry Pi OS Lite** (64-bit, arm64) - headless installation
- **At least 2GB RAM** (4GB recommended for better performance)
- **Proper cooling** (active cooling recommended for sustained 3D performance)
- **High-quality power supply** (5V/5A USB-C PD power supply)

## Step 1: Update System

```bash
# Update package lists and upgrade existing packages
sudo apt update && sudo apt upgrade -y

# Install basic utilities
sudo apt install -y curl wget git build-essential

# Reboot to ensure all updates are applied
sudo reboot
```

## Step 2: Install Java 21

```bash
# Install OpenJDK 21 (required for the application)
sudo apt install -y openjdk-21-jdk

# Verify installation
java -version
# Should show: openjdk version "21" or similar
```

## Step 3: Configure Framebuffer for Direct Rendering

Since you're running without a window manager, we need to configure the framebuffer properly:

```bash
# Install framebuffer utilities
sudo apt install -y fbset fbi

# Check current framebuffer resolution
sudo fbset

# Set framebuffer resolution if necessary (adjust to your display's native resolution)
# For 1920x1080 display:
# Note: Raspberry Pi typically uses 16 bits per pixel (not 32)
sudo fbset -g 1920 1080 1920 1080 16

# Make framebuffer resolution persistent by adding to /boot/firmware/config.txt
sudo sed -i '/^framebuffer_width=/d' /boot/firmware/config.txt
sudo sed -i '/^framebuffer_height=/d' /boot/firmware/config.txt
echo "framebuffer_width=1920" | sudo tee -a /boot/firmware/config.txt
echo "framebuffer_height=1080" | sudo tee -a /boot/firmware/config.txt
```

## Step 4: Install Correct Graphics Drivers for Raspberry Pi 5

Raspberry Pi 5 uses a new VideoCore VII GPU. The correct approach is:

```bash
# Install the correct Mesa drivers for Raspberry Pi 5
sudo apt install -y libgles2-mesa-dev libegl1-mesa-dev libgbm1 libgl1-mesa-dev

# Install additional libraries needed for LWJGL
sudo apt install -y libxrandr2 libxinerama1 libxi6 libxcursor1 libxcomposite1 libasound2-dev

# Install firmware updates (important for Pi 5)
# Note: On Raspberry Pi OS, use raspi-firmware package
sudo apt install -y raspi-firmware

# Install the new Vulkan driver for Pi 5 (optional but recommended)
sudo apt install -y vulkan-tools libvulkan1 libvulkan-dev
```

## Step 4.5: Check Current Configuration (Important!)

Before making changes, check your current configuration:

```bash
# Check which Raspberry Pi model you have
cat /sys/firmware/devicetree/base/model

# Check current GPU drivers in use
dmesg | grep -i "vc4\|vc5\|v3d" | head -5

# Check current framebuffer settings
fbset
```

For Raspberry Pi 5, you should see:
- Model: `Raspberry Pi 5` or `Raspberry Pi 500`
- GPU drivers should include `vc4` (VideoCore IV/VI/VII - Raspberry Pi 5 uses VideoCore VII but still uses vc4 driver)

If you see display issues, you may need to update the configuration.

## Step 5: Configure Boot Configuration for Pi 5

Edit the boot configuration file:

```bash
sudo nano /boot/firmware/config.txt
```

**Important**: Raspberry Pi 5 uses VideoCore VII GPU but still uses the `vc4` driver. The configuration is similar to previous models but with some optimizations.

For Raspberry Pi 5, use the following configuration:

```
# Use the standard vc4-kms-v3d overlay for Raspberry Pi 5
# This is the correct overlay for VideoCore VII GPU
# The cma-512 parameter allocates 512MB of CMA memory for better graphics performance
dtoverlay=vc4-kms-v3d,cma-512

# Enable 64-bit mode (should already be enabled)
arm_64bit=1

# Framebuffer settings (should match your display)
# These may already be set automatically, but you can specify them:
framebuffer_width=1920
framebuffer_height=1080

# Keep these existing useful settings:
max_framebuffers=2
disable_fw_kms_setup=1
disable_overscan=1
```

Save the file (Ctrl+O, Enter, Ctrl+X) and reboot:

```bash
sudo reboot
```

After reboot, verify the changes took effect:
```bash
dmesg | grep -i "vc4\|v3d"
```

## Step 6: Verify Graphics Setup

After reboot, verify your graphics setup:

```bash
# Check framebuffer info (should match your configuration)
fbset

# Check if framebuffer device is available
ls -la /dev/fb0

# Test framebuffer with a simple C program (no X11 required)
cat > test_fb.c << 'EOF'
#include <stdio.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/fb.h>

int main() {
    int fb = open("/dev/fb0", O_RDWR);
    if (fb < 0) { perror("framebuffer"); return 1; }
    
    struct fb_var_screeninfo vinfo;
    if (ioctl(fb, FBIOGET_VSCREENINFO, &vinfo)) { perror("ioctl"); return 1; }
    
    printf("Framebuffer: %dx%d, %d bpp\n", vinfo.xres, vinfo.yres, vinfo.bits_per_pixel);
    close(fb);
    return 0;
}
EOF
gcc -o test_fb test_fb.c && ./test_fb

# Check system temperature (important for 3D performance)
vcgencmd measure_temp

# Check if VC5 GPU is detected (for Raspberry Pi 5)
dmesg | grep -i "vc5\|videocore"
```

If you want to test OpenGL ES functionality, you can install a simple test:

```bash
# Install OpenGL ES test (requires X11, but useful for verification)
sudo apt install -y glmark2-es2

# For headless testing, you can use the framebuffer version if available
# Or test with a simple OpenGL ES program
```

## Step 7: Build and Run the Application

```bash
# Clone the repository (if not already done)
git clone https://github.com/your-repo/java-3d-concept.git
cd java-3d-concept

# Build the application
./gradlew build

# Create a settings file optimized for Raspberry Pi
cat > settings_rpi.json << EOF
{
  "window": {
    "fullscreen": true,
    "width": 1920,
    "height": 1080
  },
  "logLevel": "info",
  "display": {
    "showFPS": true
  },
  "raspberryPi": {
    "useFramebuffer": true,
    "framebufferDevice": "/dev/fb0"
  }
}
EOF

# Run the application with Raspberry Pi optimized settings
java -jar build/libs/java_3d_concept.jar --settings settings_rpi.json
```

## Step 8: LWJGL Framebuffer Configuration

For the application to work properly with the framebuffer, you may need to modify the LWJGL setup. Here's how to configure it:

1. **Modify the Window.java file** to support framebuffer:

```java
// In the Window creation code, add framebuffer support:
if (Settings.getInstance().isRaspberryPiFramebuffer()) {
    // Configure GLFW for framebuffer
    glfwWindowHint(GLFW_GLFW_CONTEXT_CREATION_API, GLFW_GLFW_NATIVE_CONTEXT_API);
    glfwWindowHint(GLFW_GLFW_CONTEXT_API, GLFW_GLFW_OPENGL_ES_API);
    glfwWindowHint(GLFW_GLFW_CLIENT_API, GLFW_GLFW_OPENGL_ES_API);
    
    // Create window on framebuffer
    windowHandle = glfwCreateWindow(width, height, title, glfwGetPrimaryMonitor(), 0);
}
```

2. **Add Raspberry Pi specific settings** to the Settings class:

```java
public class Settings {
    // ... existing code ...
    
    private boolean raspberryPiFramebuffer = false;
    private String framebufferDevice = "/dev/fb0";
    
    // Add getter and setter methods
    public boolean isRaspberryPiFramebuffer() {
        return raspberryPiFramebuffer;
    }
    
    public void setRaspberryPiFramebuffer(boolean raspberryPiFramebuffer) {
        this.raspberryPiFramebuffer = raspberryPiFramebuffer;
    }
    
    public String getFramebufferDevice() {
        return framebufferDevice;
    }
    
    public void setFramebufferDevice(String framebufferDevice) {
        this.framebufferDevice = framebufferDevice;
    }
}
```

## Step 9: Performance Optimization

For better performance on Raspberry Pi 5:

```bash
# Install performance monitoring tools
sudo apt install -y sysstat htop

# Monitor CPU and GPU usage during application run
top

# Check GPU frequency (should show VC5 GPU info)
vcgencmd measure_clock arm
vcgencmd measure_clock core
```

### Performance Tips:

1. **Reduce resolution**: Use 1280x720 instead of 1920x1080 for better performance
2. **Limit FPS**: Cap the frame rate to 30 FPS to reduce GPU load
3. **Disable vsync**: Set `glfwSwapInterval(0)` for better performance
4. **Use simpler shaders**: Modify shaders to be less complex
5. **Reduce draw distance**: Limit how far objects are rendered

## Step 10: Troubleshooting

### Common Issues and Solutions:

**Issue: Wrong GPU drivers or configuration on Raspberry Pi 5**
- **Symptoms**: `dmesg` shows `vc4` drivers (which is correct), poor 3D performance, or display issues
- **Solution**: Update `/boot/firmware/config.txt` to use the correct `vc4-kms-v3d` overlay as shown in Step 5

**Issue: Application crashes with OpenGL errors**
- **Solution**: Ensure you're using the correct OpenGL ES drivers and that the framebuffer is properly configured

**Issue: Black screen or no display**
- **Solution**: Check framebuffer resolution matches your display and that the HDMI cable is properly connected

**Issue: Poor performance**
- **Solution**: Reduce resolution, limit FPS, ensure proper cooling, and check power supply

**Issue: Java crashes with memory errors**
- **Solution**: Increase Java heap size: `java -Xmx1024m -jar build/libs/java_3d_concept.jar`

**Issue: LWJGL native libraries not found**
- **Solution**: Ensure the LWJGL natives for ARM64 are included in the build

**Issue: Framebuffer not working**
- **Solution**: Check `/dev/fb0` exists and has correct permissions. Test with `fbset` and simple framebuffer programs.

**Issue: Black screen or no display after changing to vc5 overlays**
- **Symptoms**: Display stops working after adding `vc5` overlays to `/boot/firmware/config.txt`
- **Solution**: Raspberry Pi 5 does not support `vc5` overlays. Remove any `vc5` overlay lines and use `vc4-kms-v3d,cma-512` instead:
  ```bash
  sudo sed -i '/dtoverlay=vc5/d' /boot/firmware/config.txt
  sudo sed -i '/dtoverlay=vc5-kms-v3d/d' /boot/firmware/config.txt
  sudo sed -i '/dtoverlay=vc5-v3d/d' /boot/firmware/config.txt
  # Remove any duplicate vc4-kms-v3d lines
  sudo sed -i '/dtoverlay=vc4-kms-v3d/d' /boot/firmware/config.txt
  # Add the correct overlay with CMA memory
  echo "dtoverlay=vc4-kms-v3d,cma-512" | sudo tee -a /boot/firmware/config.txt
  sudo reboot
  ```

### Raspberry Pi 5 Specific Issues:

**Issue: System still having display issues after configuration change**
- **Solution**: The Raspberry Pi 5 firmware might need an update. Run:
  ```bash
  sudo apt update
  sudo apt upgrade
  sudo rpi-update
  sudo reboot
  ```
  Also, ensure you're using the correct `vc4-kms-v3d` overlay, not `vc5` overlays which don't exist.

**Issue: GPU memory not increasing**
- **Solution**: Raspberry Pi 5 uses dynamic GPU memory allocation, so the `gpu_mem` parameter is not needed. Ensure you're using the `vc4-kms-v3d,cma-512` overlay which allocates 512MB of CMA memory. Some Pi 5 firmware versions have bugs with memory allocation.

### Debugging Commands:

```bash
# Check OpenGL ES capabilities
glesinfo

# Check Vulkan capabilities (if installed)
vulkaninfo

# Check framebuffer status
cat /sys/class/graphics/fb0/virtual_size

# Check system temperature
vcgencmd measure_temp

# Check throttling status
vcgencmd get_throttled
```

## Step 11: Alternative Approach - X11 Virtual Framebuffer

If you still have issues with direct framebuffer rendering, you can use Xvfb (X11 virtual framebuffer):

```bash
# Install Xvfb
sudo apt install -y xvfb

# Run application with virtual framebuffer
Xvfb :1 -screen 0 1920x1080x24 &
export DISPLAY=:1
java -jar build/libs/java_3d_concept.jar
```

## Final Notes

- Raspberry Pi 5 has significantly better 3D performance than previous models
- The VideoCore VII GPU supports OpenGL ES 3.1 and Vulkan 1.2, but still uses the `vc4` driver (not `vc5`)
- Always ensure your system is up-to-date with `sudo apt update && sudo apt upgrade`
- Monitor temperatures to prevent thermal throttling
- Consider overclocking carefully if you need more performance
- **Important**: Do not use `vc5` overlays as they don't exist in standard Raspberry Pi OS - use `vc4-kms-v3d` instead

This setup should allow you to run the Java 3D application successfully on your Raspberry Pi 5 with Raspberry Pi OS Lite without requiring a window manager.
