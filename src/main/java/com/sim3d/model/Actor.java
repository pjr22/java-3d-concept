package com.sim3d.model;

import org.joml.Vector3f;

public class Actor extends GameObject {
    private String subtype;
    private Vector3f velocity;
    private float wanderTimer;
    private float wanderInterval = 2.0f;

    public Actor(String id, String name, String subtype) {
        super(id, name);
        this.subtype = subtype;
        this.velocity = new Vector3f(0, 0, 0);
        this.wanderTimer = 0;
    }

    public Actor(String id, String name, String subtype, String model,
                 Transform transform, Vector3f color) {
        super(id, name);
        this.subtype = subtype;
        this.velocity = new Vector3f(0, 0, 0);
        this.wanderTimer = 0;
        this.modelType = model;
        if (transform != null) this.transform = transform;
        if (color != null) this.color.set(color);
    }

    @Override
    public void update(float deltaTime) {
        wanderTimer += deltaTime;

        if (wanderTimer >= wanderInterval) {
            wanderTimer = 0;
            float angle = (float) (Math.random() * Math.PI * 2);
            float speed = 1.0f;
            velocity.x = (float) Math.cos(angle) * speed;
            velocity.z = (float) Math.sin(angle) * speed;
        }

        Vector3f position = transform.getPosition();
        position.x += velocity.x * deltaTime;
        position.z += velocity.z * deltaTime;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity.set(velocity);
    }

    public void setVelocity(float x, float y, float z) {
        this.velocity.set(x, y, z);
    }

    public boolean isCreature() {
        return "creature".equals(subtype);
    }

    public boolean isNpc() {
        return "npc".equals(subtype);
    }
}
