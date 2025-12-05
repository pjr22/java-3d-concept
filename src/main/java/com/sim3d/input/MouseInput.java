package com.sim3d.input;

import org.lwjgl.BufferUtils;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class MouseInput {
    private long windowHandle;
    private double lastX;
    private double lastY;
    private float deltaX;
    private float deltaY;
    private boolean firstMouse = true;
    private boolean captured = false;

    private final DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
    private final DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);

    public void init(long windowHandle) {
        this.windowHandle = windowHandle;
        this.firstMouse = true;
        this.captured = false;
        this.deltaX = 0;
        this.deltaY = 0;
    }

    public void update() {
        xBuffer.clear();
        yBuffer.clear();
        glfwGetCursorPos(windowHandle, xBuffer, yBuffer);
        
        double currentX = xBuffer.get(0);
        double currentY = yBuffer.get(0);

        if (firstMouse) {
            lastX = currentX;
            lastY = currentY;
            firstMouse = false;
        }

        deltaX = (float) (currentX - lastX);
        deltaY = (float) (lastY - currentY);

        lastX = currentX;
        lastY = currentY;
    }

    public void captureMouse() {
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        captured = true;
        firstMouse = true;
    }

    public void releaseMouse() {
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        captured = false;
        firstMouse = true;
    }

    public boolean isCaptured() {
        return captured;
    }

    public float getDeltaX() {
        float dx = deltaX;
        deltaX = 0;
        return dx;
    }

    public float getDeltaY() {
        float dy = deltaY;
        deltaY = 0;
        return dy;
    }
}
