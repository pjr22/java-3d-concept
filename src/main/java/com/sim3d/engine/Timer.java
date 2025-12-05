package com.sim3d.engine;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Timer {
    private double lastTime;
    private float deltaTime;

    public void init() {
        lastTime = glfwGetTime();
        deltaTime = 0.0f;
    }

    public void update() {
        double currentTime = glfwGetTime();
        deltaTime = (float) (currentTime - lastTime);
        lastTime = currentTime;
    }

    public float getDeltaTime() {
        return deltaTime;
    }
}
