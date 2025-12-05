package com.sim3d.loader;

import com.sim3d.graphics.Mesh;
import org.joml.Vector3f;

import java.util.List;

public class Model {
    private final String name;
    private final List<Mesh> meshes;
    private final Vector3f boundingBoxMin;
    private final Vector3f boundingBoxMax;

    public Model(String name, List<Mesh> meshes) {
        this.name = name;
        this.meshes = meshes;
        this.boundingBoxMin = new Vector3f(Float.MAX_VALUE);
        this.boundingBoxMax = new Vector3f(-Float.MAX_VALUE);
    }

    public Model(String name, List<Mesh> meshes, Vector3f boundingBoxMin, Vector3f boundingBoxMax) {
        this.name = name;
        this.meshes = meshes;
        this.boundingBoxMin = new Vector3f(boundingBoxMin);
        this.boundingBoxMax = new Vector3f(boundingBoxMax);
    }

    public void render() {
        for (Mesh mesh : meshes) {
            mesh.render();
        }
    }

    public void cleanup() {
        for (Mesh mesh : meshes) {
            mesh.cleanup();
        }
    }

    public String getName() {
        return name;
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    public Vector3f getBoundingBoxMin() {
        return new Vector3f(boundingBoxMin);
    }

    public Vector3f getBoundingBoxMax() {
        return new Vector3f(boundingBoxMax);
    }

    public Vector3f[] getBoundingBox() {
        return new Vector3f[] { getBoundingBoxMin(), getBoundingBoxMax() };
    }

    public Vector3f getBoundingBoxCenter() {
        return new Vector3f(boundingBoxMin).add(boundingBoxMax).mul(0.5f);
    }

    public Vector3f getBoundingBoxSize() {
        return new Vector3f(boundingBoxMax).sub(boundingBoxMin);
    }
}
