package com.sim3d.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.sim3d.model.Environment;
import com.sim3d.model.World;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class WorldLoader {
    private final Gson gson;
    private final EnvironmentLoader environmentLoader;

    public WorldLoader() {
        this.gson = new GsonBuilder().create();
        this.environmentLoader = new EnvironmentLoader();
    }

    public World loadWorld(String resourcePath) throws IOException {
        WorldData data = loadWorldData(resourcePath);
        
        World world = new World(
            data.id != null ? data.id : "unknown",
            data.name != null ? data.name : "Unnamed World"
        );

        String basePath = getBasePath(resourcePath);
        
        if (data.environments != null) {
            for (String envFile : data.environments) {
                String envPath = basePath + envFile;
                Environment environment = environmentLoader.loadEnvironment(envPath);
                world.addEnvironment(environment);
            }
        }

        if (data.startEnvironment != null) {
            world.setCurrentEnvironmentId(data.startEnvironment);
        }

        return world;
    }

    private WorldData loadWorldData(String resourcePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return gson.fromJson(reader, WorldData.class);
            }
        }
    }

    private String getBasePath(String resourcePath) {
        int lastSlash = resourcePath.lastIndexOf('/');
        return lastSlash >= 0 ? resourcePath.substring(0, lastSlash + 1) : "";
    }

    public static class WorldData {
        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("startEnvironment")
        public String startEnvironment;

        @SerializedName("environments")
        public List<String> environments;
    }
}
