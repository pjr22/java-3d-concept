package com.sim3d.loader;

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
        logger.info("Loading model: {}", path);

        Model existingModel = modelCache.remove(path);
        if (existingModel != null) {
            existingModel.cleanup();
        }

        Model model = loadModelInternal(path);
        if (model != null) {
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

    public void clearCache() {
        logger.info("Clearing model cache ({} models)", modelCache.size());
        for (Model model : modelCache.values()) {
            model.cleanup();
        }
        modelCache.clear();
    }

    public void cleanup() {
        logger.info("Cleaning up AssetManager");
        clearCache();
    }
}
