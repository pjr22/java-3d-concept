package com.sim3d.graphics;

import com.sim3d.model.Player;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private float pitch;
    private float yaw;
    private float fov = 70f;
    private float nearPlane = 0.1f;
    private float farPlane = 1000f;

    public Camera() {
        this.position = new Vector3f(0, 0, 0);
        this.pitch = 0;
        this.yaw = 0;
    }

    public Camera(Vector3f position) {
        this.position = new Vector3f(position);
        this.pitch = 0;
        this.yaw = 0;
    }

    public Matrix4f getViewMatrix() {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();
        viewMatrix.rotateX((float) Math.toRadians(pitch));
        viewMatrix.rotateY((float) Math.toRadians(yaw));
        viewMatrix.translate(-position.x, -position.y, -position.z);
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix(float aspectRatio) {
        return new Matrix4f().perspective(
            (float) Math.toRadians(fov),
            aspectRatio,
            nearPlane,
            farPlane
        );
    }

    public void updateFromPlayer(Player player) {
        this.position.set(player.getPosition());
        this.position.y += 1.7f;
        this.pitch = player.getPitch();
        this.yaw = player.getYaw();
    }

    public Vector3f getForward() {
        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);
        return new Vector3f(
            (float) (Math.sin(yawRad) * Math.cos(pitchRad)),
            (float) (-Math.sin(pitchRad)),
            (float) (-Math.cos(yawRad) * Math.cos(pitchRad))
        ).normalize();
    }

    public Vector3f getRight() {
        float yawRad = (float) Math.toRadians(yaw);
        return new Vector3f(
            (float) Math.cos(yawRad),
            0,
            (float) Math.sin(yawRad)
        ).normalize();
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
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

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }
}
