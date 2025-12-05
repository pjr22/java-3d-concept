package com.sim3d.input;

import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    private long windowHandle;
    private final boolean[] keys = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] keysPressed = new boolean[GLFW_KEY_LAST + 1];
    private GLFWKeyCallback keyCallback;

    public void init(long windowHandle) {
        this.windowHandle = windowHandle;
        
        keyCallback = glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (key >= 0 && key <= GLFW_KEY_LAST) {
                if (action == GLFW_PRESS) {
                    keys[key] = true;
                    keysPressed[key] = true;
                } else if (action == GLFW_RELEASE) {
                    keys[key] = false;
                }
            }
        });
    }

    public boolean isKeyPressed(int keyCode) {
        if (keyCode >= 0 && keyCode <= GLFW_KEY_LAST) {
            boolean pressed = keysPressed[keyCode];
            keysPressed[keyCode] = false;
            return pressed;
        }
        return false;
    }

    public boolean isKeyDown(int keyCode) {
        if (keyCode >= 0 && keyCode <= GLFW_KEY_LAST) {
            return keys[keyCode];
        }
        return false;
    }

    public boolean isForward() {
        return isKeyDown(GLFW_KEY_W);
    }

    public boolean isBackward() {
        return isKeyDown(GLFW_KEY_S);
    }

    public boolean isLeft() {
        return isKeyDown(GLFW_KEY_A);
    }

    public boolean isRight() {
        return isKeyDown(GLFW_KEY_D);
    }

    public boolean isUp() {
        return isKeyDown(GLFW_KEY_SPACE);
    }

    public boolean isDown() {
        return isKeyDown(GLFW_KEY_LEFT_SHIFT);
    }

    public boolean isEscapePressed() {
        return isKeyPressed(GLFW_KEY_ESCAPE);
    }

    public boolean isUpArrowPressed() {
        return isKeyPressed(GLFW_KEY_UP);
    }

    public boolean isDownArrowPressed() {
        return isKeyPressed(GLFW_KEY_DOWN);
    }

    public boolean isEnterPressed() {
        return isKeyPressed(GLFW_KEY_ENTER);
    }

    public void cleanup() {
        if (keyCallback != null) {
            keyCallback.free();
        }
    }
}
