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
    private static final Logger logger = LoggerFactory.getLogger(Settings.class);
    private static final String SETTINGS_FILE = "settings.json";
    private static Settings instance;
    
    private WindowSettings window;
    private String logLevel;
    
    private Settings() {
        // Private constructor for singleton
    }
    
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
            instance.loadSettings();
        }
        return instance;
    }
    
    private void loadSettings() {
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
        Path settingsPath = Paths.get(SETTINGS_FILE);
        if (Files.exists(settingsPath)) {
            try (Reader reader = new FileReader(SETTINGS_FILE)) {
                Gson gson = new Gson();
                SettingsData data = gson.fromJson(reader, SettingsData.class);
                
                if (data != null && data.window != null) {
                    this.window = data.window;
                    if (data.logLevel != null) {
                        this.logLevel = data.logLevel;
                    }
                    logger.info("User settings loaded successfully from {}", SETTINGS_FILE);
                } else {
                    logger.warn("Invalid user settings format, keeping default settings");
                }
            } catch (IOException e) {
                logger.error("Failed to load user settings from {}, using default settings: {}", SETTINGS_FILE, e.getMessage());
            }
        }
    }
    
    private void createDefaultSettings() {
        this.window = new WindowSettings();
        this.window.fullscreen = false;
        this.window.width = 1280;
        this.window.height = 720;
        this.logLevel = "info";
    }
    
    public void saveSettings() {
        try (Writer writer = new FileWriter(SETTINGS_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            SettingsData data = new SettingsData();
            data.window = this.window;
            data.logLevel = this.logLevel;
            
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
        return logLevel != null ? logLevel : "info";
    }
    
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    
    // Inner classes for JSON serialization
    public static class SettingsData {
        public WindowSettings window;
        public String logLevel;
    }
    
    public static class WindowSettings {
        public boolean fullscreen;
        public int width;
        public int height;
    }
}