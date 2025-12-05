package com.sim3d.model;

import org.joml.Vector3f;

public abstract class GameObject {
    protected String id;
    protected String name;
    protected Transform transform;
    protected Vector3f color;
    protected String modelType;
    protected String modelPath;

    public GameObject(String id, String name) {
        this.id = id;
        this.name = name;
        this.transform = Transform.identity();
        this.color = new Vector3f(1, 1, 1);
        this.modelType = "cube";
        this.modelPath = null;
    }

    public abstract void update(float deltaTime);

    public void render() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color.set(color);
    }

    public void setColor(float r, float g, float b) {
        this.color.set(r, g, b);
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public boolean hasCustomModel() {
        return modelPath != null && !modelPath.isEmpty();
    }
}
