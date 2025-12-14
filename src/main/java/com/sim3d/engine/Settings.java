package com.sim3d.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Settings {
    public static final String DEFAULT_WORLD_PATH = "worlds/demo_world.json";
    public static final String DEFAULT_LOG_LEVEL = "INFO";
    private static final Logger logger = LoggerFactory.getLogger(Settings.class);
    private static final String SETTINGS_FILE = "settings.json";
    private static Settings instance;
    
    private WindowSettings window;
    private String logLevel;
    private DisplaySettings display;
    private WorldSettings world;
    
    private Settings() {
        // Private constructor for singleton
    }
    
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
            instance.loadSettings(null);
        }
        return instance;
    }
    
    public static Settings getInstance(String customSettingsPath) {
        if (instance == null) {
            instance = new Settings();
            instance.loadSettings(customSettingsPath);
        }
        return instance;
    }
    
    private void loadSettings(String customSettingsPath) {
        // First try to load from resources (default settings)
        try {
            String resourcePath = "/" + SETTINGS_FILE;
            try (Reader reader = new java.io.InputStreamReader(getClass().getResourceAsStream(resourcePath))) {
                if (reader != null) {
                    Gson gson = new Gson();
                    SettingsData data = gson.fromJson(reader, SettingsData.class);
                    
                    if (data != null && data.window != null) {
                        this.window = data.window;
                        this.logLevel = data.logLevel;
                        this.display = data.display;
                        this.world = data.world;
                        logger.info("Default settings loaded from resources");
                    } else {
                        logger.warn("Invalid default settings format, creating default settings");
                        createDefaultSettings();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not load default settings from resources: {}", e.getMessage());
            createDefaultSettings();
        }
        
        // Then try to load from user settings file (overwrites defaults)
        String settingsFileName = customSettingsPath != null ? customSettingsPath : SETTINGS_FILE;
        Path settingsPath = Paths.get(settingsFileName);
        if (Files.exists(settingsPath)) {
            try (Reader reader = new FileReader(settingsFileName)) {
                Gson gson = new Gson();
                SettingsData data = gson.fromJson(reader, SettingsData.class);                
                if (data != null) {
                    logger.info("User settings loaded successfully from {}", settingsFileName);
                    if (data.window != null) {
                        this.window = data.window;
                        logger.info("Overriding Window settings with user settings.");
                    }
                    if (data.logLevel != null) {
                        this.logLevel = data.logLevel;
                        logger.info("Overriding Log Level settings with user settings.");
                    }
                    if (data.display != null) {
                        this.display = data.display;
                        logger.info("Overriding Display settings with user settings.");
                    }
                    if (data.world != null) {
                        this.world = data.world;
                        logger.info("Overriding World settings with user settings.");
                    }
                } else {
                    logger.warn("Invalid user settings format, keeping default settings");
                }
            } catch (IOException e) {
                logger.error("Failed to load user settings from {}, using default settings: {}", settingsFileName, e.getMessage());
            }
        }
    }
    
    private void createDefaultSettings() {
        this.window = new WindowSettings();
        this.window.fullscreen = false;
        this.window.width = 1280;
        this.window.height = 720;
        this.logLevel = DEFAULT_LOG_LEVEL;
        this.display = new DisplaySettings();
        this.display.showFPS = false;
        this.world = new WorldSettings();
        this.world.path = DEFAULT_WORLD_PATH;
    }
    
    public void saveSettings() {
        try (Writer writer = new FileWriter(SETTINGS_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            SettingsData data = new SettingsData();
            data.window = this.window;
            data.logLevel = this.logLevel;
            data.display = this.display;
            data.world = this.world;
            gson.toJson(data, writer);
            logger.info("Settings saved to {}", SETTINGS_FILE);
        } catch (IOException e) {
            logger.error("Failed to save settings to {}: {}", SETTINGS_FILE, e.getMessage());
        }
    }
    
    public WindowSettings getWindow() {
        return window;
    }
    
    public void setWindow(WindowSettings window) {
        this.window = window;
    }
    
    public boolean isFullscreen() {
        return window != null && window.fullscreen;
    }
    
    public int getWindowWidth() {
        return window != null ? window.width : 1280;
    }
    
    public int getWindowHeight() {
        return window != null ? window.height : 720;
    }
    
    public void setFullscreen(boolean fullscreen) {
        if (window != null) {
            window.fullscreen = fullscreen;
        }
    }
    
    public void setWindowResolution(int width, int height) {
        if (window != null) {
            window.width = width;
            window.height = height;
        }
    }
    
    public String getLogLevel() {
        return logLevel != null ? logLevel : DEFAULT_LOG_LEVEL;
    }
    
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    
    public DisplaySettings getDisplay() {
        return display != null ? display : new DisplaySettings();
    }
    
    public void setDisplay(DisplaySettings display) {
        this.display = display;
    }
    
    public boolean isShowFPS() {
        return display != null && display.showFPS;
    }
    
    public void setShowFPS(boolean showFPS) {
        if (display != null) {
            display.showFPS = showFPS;
        }
    }
    
    public String getWorldPath() {
        return world != null ? world.path : DEFAULT_WORLD_PATH;
    }
    
    public void setWorldPath(String worldPath) {
        if (world != null) {
            world.path = worldPath;
        }
    }
    
    // Inner classes for JSON serialization
    public static class SettingsData {
        public WindowSettings window;
        public String logLevel;
        public DisplaySettings display;
        public WorldSettings world;
    }
    
    public static class WindowSettings {
        public boolean fullscreen;
        public int width;
        public int height;
    }
    
    public static class DisplaySettings {
        public boolean showFPS;
    }

    public static class WorldSettings {
        public String path;
    }
}