package com.sim3d.model;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class Environment {
    protected String id;
    protected String name;
    protected String type;
    protected Vector3f bounds;
    protected Vector3f spawnPoint;
    protected Map<String, Vector3f> spawnPoints;
    protected List<GameObject> objects;
    protected List<Portal> portals;
    protected Vector3f groundColor;
    protected Vector3f skyColor;

    public Environment(String id, String name) {
        this.id = id;
        this.name = name;
        this.bounds = new Vector3f(100, 50, 100);
        this.spawnPoint = new Vector3f(0, 1, 0);
        this.spawnPoints = new HashMap<>();
        this.spawnPoints.put("default", new Vector3f(spawnPoint));
        this.objects = new ArrayList<>();
        this.portals = new ArrayList<>();
        this.groundColor = new Vector3f(0.3f, 0.5f, 0.2f);
        this.skyColor = new Vector3f(0.5f, 0.7f, 1.0f);
    }

    public void addObject(GameObject object) {
        objects.add(object);
    }

    public boolean removeObject(GameObject object) {
        return objects.remove(object);
    }

    public boolean removeObjectById(String id) {
        return objects.removeIf(obj -> obj.getId().equals(id));
    }

    public List<GameObject> getObjects() {
        return objects;
    }

    public Optional<GameObject> findObjectById(String id) {
        return objects.stream()
                .filter(obj -> obj.getId().equals(id))
                .findFirst();
    }

    public void update(float deltaTime) {
        for (GameObject object : objects) {
            object.update(deltaTime);
        }
    }

    public void addPortal(Portal portal) {
        portals.add(portal);
    }

    public List<Portal> getPortals() {
        return portals;
    }

    public void addSpawnPoint(String name, Vector3f position) {
        spawnPoints.put(name, new Vector3f(position));
    }

    public Vector3f getSpawnPoint(String name) {
        return spawnPoints.getOrDefault(name, spawnPoint);
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

    public String getType() {
        return type;
    }

    public Vector3f getBounds() {
        return bounds;
    }

    public void setBounds(Vector3f bounds) {
        this.bounds.set(bounds);
    }

    public void setBounds(float width, float height, float depth) {
        this.bounds.set(width, height, depth);
    }

    public Vector3f getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Vector3f spawnPoint) {
        this.spawnPoint.set(spawnPoint);
        this.spawnPoints.put("default", new Vector3f(spawnPoint));
    }

    public Map<String, Vector3f> getSpawnPoints() {
        return spawnPoints;
    }

    public Vector3f getGroundColor() {
        return groundColor;
    }

    public void setGroundColor(Vector3f groundColor) {
        this.groundColor.set(groundColor);
    }

    public void setGroundColor(float r, float g, float b) {
        this.groundColor.set(r, g, b);
    }

    public Vector3f getSkyColor() {
        return skyColor;
    }

    public void setSkyColor(Vector3f skyColor) {
        this.skyColor.set(skyColor);
    }

    public void setSkyColor(float r, float g, float b) {
        this.skyColor.set(r, g, b);
    }
}
