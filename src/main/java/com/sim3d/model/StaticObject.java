package com.sim3d.model;

import org.joml.Vector3f;

public class StaticObject extends GameObject {
    private String subtype;

    public StaticObject(String id, String name, String subtype) {
        super(id, name);
        this.subtype = subtype;
    }

    public StaticObject(String id, String name, String subtype, String model,
                        Transform transform, Vector3f color) {
        super(id, name);
        this.subtype = subtype;
        this.modelType = model;
        if (transform != null) this.transform = transform;
        if (color != null) this.color.set(color);
    }

    @Override
    public void update(float deltaTime) {
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public boolean isNaturalFeature() {
        return "natural_feature".equals(subtype);
    }

    public boolean isTool() {
        return "tool".equals(subtype);
    }

    public boolean isFurniture() {
        return "furniture".equals(subtype);
    }
}
