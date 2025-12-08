package com.sim3d.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.sim3d.model.*;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class EnvironmentLoader {
    private final Gson gson;

    public EnvironmentLoader() {
        this.gson = new GsonBuilder().create();
    }

    public Environment loadEnvironment(String resourcePath) throws IOException {
        EnvironmentData data = loadEnvironmentData(resourcePath);
        
        Vector3f bounds = toVector3f(data.bounds);
        Vector3f spawnPoint = toVector3f(data.spawnPoint);
        Vector3f groundColor = toColorVector(data.groundColor);
        Vector3f skyColor = toColorVector(data.skyColor);

        Environment environment = createEnvironment(data, bounds, spawnPoint, groundColor, skyColor);

        if (data.spawnPoints != null) {
            for (Map.Entry<String, PositionData> entry : data.spawnPoints.entrySet()) {
                environment.addSpawnPoint(entry.getKey(), toVector3f(entry.getValue()));
            }
        }

        if (data.objects != null) {
            for (ObjectData objData : data.objects) {
                GameObject gameObject = createGameObject(objData);
                if (gameObject != null) {
                    environment.addObject(gameObject);
                }
            }
        }

        if (data.portals != null) {
            for (PortalData portalData : data.portals) {
                Portal portal = createPortal(portalData);
                environment.addPortal(portal);
            }
        }

        return environment;
    }

    private EnvironmentData loadEnvironmentData(String resourcePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return gson.fromJson(reader, EnvironmentData.class);
            }
        }
    }

    private Environment createEnvironment(EnvironmentData data, Vector3f bounds, 
                                          Vector3f spawnPoint, Vector3f groundColor, Vector3f skyColor) {
        String type = data.type != null ? data.type : "outdoor";
        String id = data.id != null ? data.id : "unknown";
        String name = data.name != null ? data.name : "Unnamed Environment";

        if ("indoor".equalsIgnoreCase(type)) {
            return new IndoorEnvironment(id, name, bounds, spawnPoint, groundColor, skyColor);
        } else {
            return new OutdoorEnvironment(id, name, bounds, spawnPoint, groundColor, skyColor);
        }
    }

    private GameObject createGameObject(ObjectData data) {
        if (data == null) return null;

        Vector3f position = new Vector3f();
        Vector3f rotation = new Vector3f();
        Vector3f scale = new Vector3f(1, 1, 1);

        if (data.transform != null) {
            if (data.transform.position != null) {
                position = toVector3f(data.transform.position);
            }
            if (data.transform.rotation != null) {
                rotation = toVector3f(data.transform.rotation);
            }
            if (data.transform.scale != null) {
                scale = toVector3f(data.transform.scale);
            }
        }

        Vector3f color = toColorVector(data.color);
        String id = data.id != null ? data.id : "obj_" + System.nanoTime();
        String name = data.name != null ? data.name : id;
        String subtype = data.subtype != null ? data.subtype : "unknown";
        String model = data.model != null ? data.model : "cube";
        String type = data.type != null ? data.type : "static";

        Transform transform = new Transform(position, rotation, scale);
        String modelPath = data.modelPath;
        String texturePath = data.texturePath;

        GameObject gameObject = switch (type.toLowerCase()) {
            case "actor" -> new Actor(id, name, subtype, model, transform, color);
            case "container" -> new Container(id, name, model, transform, color);
            default -> new StaticObject(id, name, subtype, model, transform, color);
        };

        if (modelPath != null && !modelPath.isEmpty()) {
            gameObject.setModelPath(modelPath);
        }

        if (texturePath != null && !texturePath.isEmpty()) {
            gameObject.setTexturePath(texturePath);
        }

        return gameObject;
    }

    private Portal createPortal(PortalData data) {
        Vector3f position = new Vector3f();
        Vector3f triggerSize = new Vector3f(2, 3, 2);

        if (data.transform != null && data.transform.position != null) {
            position = toVector3f(data.transform.position);
        }

        if (data.triggerSize != null) {
            triggerSize = toVector3f(data.triggerSize);
        }

        return new Portal(
            data.id != null ? data.id : "portal_" + System.nanoTime(),
            data.name != null ? data.name : "Portal",
            data.targetEnvironmentId,
            data.targetSpawnPoint != null ? data.targetSpawnPoint : "default",
            position,
            triggerSize
        );
    }

    private Vector3f toVector3f(PositionData data) {
        if (data == null) return new Vector3f();
        return new Vector3f(data.x, data.y, data.z);
    }

    private Vector3f toVector3f(BoundsData data) {
        if (data == null) return new Vector3f(100, 50, 100);
        return new Vector3f(data.width, data.height, data.depth);
    }

    private Vector3f toColorVector(ColorData data) {
        if (data == null) return new Vector3f(1.0f, 1.0f, 1.0f);
        return new Vector3f(data.r, data.g, data.b);
    }

    public static class EnvironmentData {
        @SerializedName("id")
        public String id;

        @SerializedName("type")
        public String type;

        @SerializedName("name")
        public String name;

        @SerializedName("bounds")
        public BoundsData bounds;

        @SerializedName("spawnPoint")
        public PositionData spawnPoint;

        @SerializedName("spawnPoints")
        public Map<String, PositionData> spawnPoints;

        @SerializedName("objects")
        public List<ObjectData> objects;

        @SerializedName("portals")
        public List<PortalData> portals;

        @SerializedName("groundColor")
        public ColorData groundColor;

        @SerializedName("skyColor")
        public ColorData skyColor;
    }

    public static class BoundsData {
        @SerializedName("width")
        public float width;

        @SerializedName("height")
        public float height;

        @SerializedName("depth")
        public float depth;
    }

    public static class PositionData {
        @SerializedName("x")
        public float x;

        @SerializedName("y")
        public float y;

        @SerializedName("z")
        public float z;
    }

    public static class ColorData {
        @SerializedName("r")
        public float r;

        @SerializedName("g")
        public float g;

        @SerializedName("b")
        public float b;
    }

    public static class TransformData {
        @SerializedName("position")
        public PositionData position;

        @SerializedName("rotation")
        public PositionData rotation;

        @SerializedName("scale")
        public PositionData scale;
    }

    public static class ObjectData {
        @SerializedName("id")
        public String id;

        @SerializedName("type")
        public String type;

        @SerializedName("subtype")
        public String subtype;

        @SerializedName("name")
        public String name;

        @SerializedName("model")
        public String model;

        @SerializedName("modelPath")
        public String modelPath;

        @SerializedName("transform")
        public TransformData transform;

        @SerializedName("color")
        public ColorData color;

        @SerializedName("texturePath")
        public String texturePath;
    }

    public static class PortalData {
        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("targetEnvironmentId")
        public String targetEnvironmentId;

        @SerializedName("targetSpawnPoint")
        public String targetSpawnPoint;

        @SerializedName("transform")
        public TransformData transform;

        @SerializedName("triggerSize")
        public BoundsData triggerSize;
    }
}
