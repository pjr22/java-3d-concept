package com.sim3d.model;

import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class World {
    private String id;
    private String name;
    private Map<String, Environment> environments;
    private String currentEnvironmentId;

    public World(String id, String name) {
        this.id = id;
        this.name = name;
        this.environments = new HashMap<>();
        this.currentEnvironmentId = null;
    }

    public Environment getCurrentEnvironment() {
        if (currentEnvironmentId == null) {
            return null;
        }
        return environments.get(currentEnvironmentId);
    }

    public void transitionTo(String environmentId, String spawnPointId) {
        Environment targetEnv = environments.get(environmentId);
        if (targetEnv != null) {
            this.currentEnvironmentId = environmentId;
        }
    }

    public Optional<Vector3f> getSpawnPointForTransition(String environmentId, String spawnPointId) {
        Environment env = environments.get(environmentId);
        if (env != null) {
            return Optional.of(env.getSpawnPoint(spawnPointId));
        }
        return Optional.empty();
    }

    public void update(float deltaTime) {
        Environment current = getCurrentEnvironment();
        if (current != null) {
            current.update(deltaTime);
        }
    }

    public void addEnvironment(Environment env) {
        environments.put(env.getId(), env);
        if (currentEnvironmentId == null) {
            currentEnvironmentId = env.getId();
        }
    }

    public Optional<Environment> getEnvironment(String id) {
        return Optional.ofNullable(environments.get(id));
    }

    public Map<String, Environment> getEnvironments() {
        return environments;
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

    public String getCurrentEnvironmentId() {
        return currentEnvironmentId;
    }

    public void setCurrentEnvironmentId(String environmentId) {
        if (environments.containsKey(environmentId)) {
            this.currentEnvironmentId = environmentId;
        }
    }
}
