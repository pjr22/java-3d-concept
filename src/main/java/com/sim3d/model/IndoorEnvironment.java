package com.sim3d.model;

import org.joml.Vector3f;

public class IndoorEnvironment extends Environment {
    private boolean hasCeiling;
    private float ambientLightLevel;

    public IndoorEnvironment(String id, String name) {
        super(id, name);
        this.type = "indoor";
        this.hasCeiling = true;
        this.ambientLightLevel = 0.3f;
    }

    public IndoorEnvironment(String id, String name, Vector3f bounds, Vector3f spawnPoint,
                             Vector3f groundColor, Vector3f skyColor) {
        super(id, name);
        this.type = "indoor";
        this.hasCeiling = true;
        this.ambientLightLevel = 0.3f;
        if (bounds != null) this.bounds.set(bounds);
        if (spawnPoint != null) {
            this.spawnPoint.set(spawnPoint);
            this.spawnPoints.put("default", new Vector3f(spawnPoint));
        }
        if (groundColor != null) this.groundColor.set(groundColor);
        if (skyColor != null) this.skyColor.set(skyColor);
    }

    public boolean hasCeiling() {
        return hasCeiling;
    }

    public void setHasCeiling(boolean hasCeiling) {
        this.hasCeiling = hasCeiling;
    }

    public float getAmbientLightLevel() {
        return ambientLightLevel;
    }

    public void setAmbientLightLevel(float ambientLightLevel) {
        this.ambientLightLevel = Math.max(0, Math.min(1, ambientLightLevel));
    }
}
