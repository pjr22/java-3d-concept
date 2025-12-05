package com.sim3d.model;

import org.joml.Vector3f;

public class Player {
    private Vector3f position;
    private float pitch;
    private float yaw;
    private float moveSpeed = 5.0f;
    private float mouseSensitivity = 0.1f;

    public Player() {
        this.position = new Vector3f(0, 1.7f, 0);
        this.pitch = 0;
        this.yaw = 0;
    }

    public Player(Vector3f startPosition) {
        this.position = new Vector3f(startPosition);
        this.pitch = 0;
        this.yaw = 0;
    }

    public void update(float deltaTime, boolean forward, boolean backward, boolean left, boolean right,
                       boolean up, boolean down, float mouseDX, float mouseDY) {
        yaw += mouseDX * mouseSensitivity;
        pitch -= mouseDY * mouseSensitivity;

        pitch = Math.max(-89, Math.min(89, pitch));

        float yawRad = (float) Math.toRadians(yaw);

        float forwardX = (float) Math.sin(yawRad);
        float forwardZ = (float) -Math.cos(yawRad);

        float rightX = (float) Math.cos(yawRad);
        float rightZ = (float) Math.sin(yawRad);

        float moveX = 0;
        float moveY = 0;
        float moveZ = 0;

        if (forward) {
            moveX += forwardX;
            moveZ += forwardZ;
        }
        if (backward) {
            moveX -= forwardX;
            moveZ -= forwardZ;
        }
        if (left) {
            moveX -= rightX;
            moveZ -= rightZ;
        }
        if (right) {
            moveX += rightX;
            moveZ += rightZ;
        }
        if (up) {
            moveY += 1;
        }
        if (down) {
            moveY -= 1;
        }

        float length = (float) Math.sqrt(moveX * moveX + moveY * moveY + moveZ * moveZ);
        if (length > 0) {
            moveX /= length;
            moveY /= length;
            moveZ /= length;
        }

        position.x += moveX * moveSpeed * deltaTime;
        position.y += moveY * moveSpeed * deltaTime;
        position.z += moveZ * moveSpeed * deltaTime;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public void setMouseSensitivity(float mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    public Vector3f getLookDirection() {
        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);

        float x = (float) (Math.cos(pitchRad) * Math.sin(yawRad));
        float y = (float) Math.sin(pitchRad);
        float z = (float) (-Math.cos(pitchRad) * Math.cos(yawRad));

        return new Vector3f(x, y, z);
    }
}
