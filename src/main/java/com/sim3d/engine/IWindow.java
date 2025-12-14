package com.sim3d.engine;

/**
 * IWindow interface defines the common methods for both GLFW and framebuffer windows.
 */
public interface IWindow {
    void init();
    boolean shouldClose();
    void swapBuffers();
    void pollEvents();
    void cleanup();
    int getWidth();
    int getHeight();
    String getTitle();
    boolean isFullscreen();
    void setFullscreen(boolean fullscreen);
    
    /**
     * Get the window handle (GLFW window handle for GLFW windows)
     * @return window handle, or -1 if not applicable (e.g., framebuffer mode)
     */
    long getWindowHandle();
}