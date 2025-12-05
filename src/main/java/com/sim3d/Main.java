package com.sim3d;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.sim3d.engine.Engine;
import com.sim3d.engine.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static Logger logger;

    public static void main(String[] args) {
        // Initialize settings first to get log level
        Settings settings = Settings.getInstance();
        String logLevel = settings.getLogLevel();
        
        // Configure logging level programmatically
        configureLogLevel(logLevel);
        
        logger = LoggerFactory.getLogger(Main.class);
        logger.info("Starting 3D Simulation Engine...");
        
        Engine engine = new Engine();
        
        try {
            engine.init();
            engine.run();
        } catch (Exception e) {
            logger.error("Engine error: ", e);
            System.exit(1);
        } finally {
            engine.cleanup();
        }
        
        logger.info("Engine shutdown complete.");
    }
    
    private static void configureLogLevel(String logLevel) {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            ch.qos.logback.classic.Logger sim3dLogger = loggerContext.getLogger("com.sim3d");
            
            Level level = Level.toLevel(logLevel.toUpperCase(), Level.INFO);
            rootLogger.setLevel(level);
            sim3dLogger.setLevel(level);
        } catch (Exception e) {
            // Fallback to default if configuration fails
            System.err.println("Failed to configure log level: " + e.getMessage());
        }
    }
}
