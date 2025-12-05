package com.sim3d.model;

import org.joml.Vector3f;

public class Portal {
    private String id;
    private String name;
    private String targetEnvironmentId;
    private String targetSpawnPointId;
    private Transform transform;
    private Vector3f triggerSize;

    public Portal(String id, String name, String targetEnvironmentId, String targetSpawnPointId) {
        this.id = id;
        this.name = name;
        this.targetEnvironmentId = targetEnvironmentId;
        this.targetSpawnPointId = targetSpawnPointId;
        this.transform = Transform.identity();
        this.triggerSize = new Vector3f(2, 3, 2);
    }

    public Portal(String id, String name, String targetEnvironmentId, String targetSpawnPointId,
                  Vector3f position, Vector3f triggerSize) {
        this.id = id;
        this.name = name;
        this.targetEnvironmentId = targetEnvironmentId;
        this.targetSpawnPointId = targetSpawnPointId;
        this.transform = new Transform(position, new Vector3f(), new Vector3f(1, 1, 1));
        this.triggerSize = triggerSize != null ? triggerSize : new Vector3f(2, 3, 2);
    }

    public boolean isPlayerInTrigger(Vector3f playerPos) {
        Vector3f portalPos = transform.getPosition();
        float halfWidth = triggerSize.x / 2;
        float halfHeight = triggerSize.y / 2;
        float halfDepth = triggerSize.z / 2;

        return playerPos.x >= portalPos.x - halfWidth && playerPos.x <= portalPos.x + halfWidth &&
               playerPos.y >= portalPos.y - halfHeight && playerPos.y <= portalPos.y + halfHeight &&
               playerPos.z >= portalPos.z - halfDepth && playerPos.z <= portalPos.z + halfDepth;
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

    public String getTargetEnvironmentId() {
        return targetEnvironmentId;
    }

    public void setTargetEnvironmentId(String targetEnvironmentId) {
        this.targetEnvironmentId = targetEnvironmentId;
    }

    public String getTargetSpawnPointId() {
        return targetSpawnPointId;
    }

    public void setTargetSpawnPointId(String targetSpawnPointId) {
        this.targetSpawnPointId = targetSpawnPointId;
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public Vector3f getTriggerSize() {
        return triggerSize;
    }

    public void setTriggerSize(Vector3f triggerSize) {
        this.triggerSize.set(triggerSize);
    }

    public void setTriggerSize(float width, float height, float depth) {
        this.triggerSize.set(width, height, depth);
    }
}
