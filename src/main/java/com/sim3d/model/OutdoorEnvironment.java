package com.sim3d.model;

import org.joml.Vector3f;

public class OutdoorEnvironment extends Environment {
    private boolean renderSky;

    public OutdoorEnvironment(String id, String name) {
        super(id, name);
        this.type = "outdoor";
        this.renderSky = true;
    }

    public OutdoorEnvironment(String id, String name, Vector3f bounds, Vector3f spawnPoint,
                              Vector3f groundColor, Vector3f skyColor) {
        super(id, name);
        this.type = "outdoor";
        this.renderSky = true;
        if (bounds != null) this.bounds.set(bounds);
        if (spawnPoint != null) {
            this.spawnPoint.set(spawnPoint);
            this.spawnPoints.put("default", new Vector3f(spawnPoint));
        }
        if (groundColor != null) this.groundColor.set(groundColor);
        if (skyColor != null) this.skyColor.set(skyColor);
    }

    public boolean isRenderSky() {
        return renderSky;
    }

    public void setRenderSky(boolean renderSky) {
        this.renderSky = renderSky;
    }
}
