package com.sim3d.engine;

/**
 * WindowFactory creates the appropriate window implementation based on settings.
 */
public class WindowFactory {
    
    public static IWindow createWindow(String title, int width, int height, boolean fullscreen) {
        Settings settings = Settings.getInstance();
        
        if (settings.isRaspberryPiFramebuffer()) {
            // Create a framebuffer window for Raspberry Pi
            return new FramebufferWindow(title, width, height, fullscreen);
        } else {
            // Create a standard GLFW window
            return new Window(title, width, height, fullscreen);
        }
    }
    
    public static IWindow createWindow(String title) {
        return createWindow(title, 1280, 720, false);
    }
    
    public static IWindow createWindow(String title, int width, int height) {
        return createWindow(title, width, height, false);
    }
}