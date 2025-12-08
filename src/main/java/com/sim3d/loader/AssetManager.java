package com.sim3d.loader;

import com.sim3d.graphics.Texture;
import com.sim3d.graphics.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AssetManager {
    private static final Logger logger = LoggerFactory.getLogger(AssetManager.class);
    private static AssetManager instance;

    private final Map<String, Model> modelCache = new ConcurrentHashMap<>();
    private final Map<String, Texture> textureCache = new ConcurrentHashMap<>();

    private AssetManager() {}

    public static synchronized AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    public Model getModel(String path) {
        Model cached = modelCache.get(path);
        if (cached != null) {
            logger.debug("Returning cached model: {}", path);
            return cached;
        }
        return loadModel(path);
    }

    public Model loadModel(String path) {
        return loadModel(path, null);
    }

    public Model loadModel(String path, String texturePath) {
        logger.info("Loading model: {}", path);
        if (texturePath != null) {
            logger.info("With texture: {}", texturePath);
        }

        Model existingModel = modelCache.remove(path);
        if (existingModel != null) {
            existingModel.cleanup();
        }

        Model model = loadModelInternal(path);
        if (model != null) {
            // Load texture if specified
            if (texturePath != null && !texturePath.isEmpty()) {
                Texture texture = getTexture(texturePath);
                model.setTexture(texture);
                model.setTexturePath(texturePath);
            }
            
            modelCache.put(path, model);
            logger.info("Cached model: {}", path);
        }
        return model;
    }

    private Model loadModelInternal(String path) {
        if (path.toLowerCase().endsWith(".obj")) {
            if (isFilePath(path)) {
                return ObjLoader.loadFromFile(path);
            } else {
                return ObjLoader.load(path);
            }
        }

        logger.warn("Unsupported model format: {}", path);
        return null;
    }

    private boolean isFilePath(String path) {
        if (path.startsWith("/") && !path.startsWith("//")) {
            File file = new File(path);
            return file.exists();
        }
        if (path.length() > 2 && path.charAt(1) == ':') {
            return true;
        }
        if (path.startsWith("./") || path.startsWith("..")) {
            return true;
        }
        return false;
    }

    public void preloadModels(List<String> paths) {
        logger.info("Preloading {} models", paths.size());
        for (String path : paths) {
            if (!modelCache.containsKey(path)) {
                loadModel(path);
            }
        }
    }

    public boolean hasModel(String path) {
        return modelCache.containsKey(path);
    }

    public Texture getTexture(String path) {
        Texture cached = textureCache.get(path);
        if (cached != null) {
            logger.debug("Returning cached texture: {}", path);
            return cached;
        }
        return loadTexture(path);
    }

    public Texture loadTexture(String path) {
        logger.info("Loading texture: {}", path);

        Texture existingTexture = textureCache.remove(path);
        if (existingTexture != null) {
            existingTexture.cleanup();
        }

        Texture texture = TextureLoader.load(path);
        if (texture != null) {
            textureCache.put(path, texture);
            logger.info("Cached texture: {}", path);
        }
        return texture;
    }

    public void preloadTextures(List<String> paths) {
        logger.info("Preloading {} textures", paths.size());
        for (String path : paths) {
            if (!textureCache.containsKey(path)) {
                loadTexture(path);
            }
        }
    }

    public boolean hasTexture(String path) {
        return textureCache.containsKey(path);
    }

    public void clearModelCache() {
        logger.info("Clearing model cache ({} models)", modelCache.size());
        for (Model model : modelCache.values()) {
            model.cleanup();
        }
        modelCache.clear();
    }

    public void clearTextureCache() {
        logger.info("Clearing texture cache ({} textures)", textureCache.size());
        for (Texture texture : textureCache.values()) {
            texture.cleanup();
        }
        textureCache.clear();
    }

    public void clearCache() {
        logger.info("Clearing all caches");
        clearModelCache();
        clearTextureCache();
    }

    public void cleanup() {
        logger.info("Cleaning up AssetManager");
        clearCache();
    }
}
