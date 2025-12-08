package com.sim3d.loader;

import com.sim3d.graphics.Mesh;
import com.sim3d.graphics.Texture;
import org.joml.Vector3f;

import java.util.List;

public class Model {
    private final String name;
    private final List<Mesh> meshes;
    private final Vector3f boundingBoxMin;
    private final Vector3f boundingBoxMax;
    private Texture texture;
    private String texturePath;

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
        render(false);
    }

    public void render(boolean useTexture) {
        for (Mesh mesh : meshes) {
            // Only render meshes that have texture coordinates when using texture
            if (!useTexture || mesh.hasTextureCoords()) {
                mesh.render();
            }
        }
    }

    public void cleanup() {
        for (Mesh mesh : meshes) {
            mesh.cleanup();
        }
        if (texture != null) {
            texture.cleanup();
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

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
    }

    public boolean hasTexture() {
        return texture != null || texturePath != null;
    }

    public boolean hasTextureCoordinates() {
        return meshes.stream().anyMatch(Mesh::hasTextureCoords);
    }
}
