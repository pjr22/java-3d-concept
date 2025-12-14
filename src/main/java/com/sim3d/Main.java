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
        // Parse command line arguments
        String customSettingsPath = parseCommandLineArgs(args);
        
        // Initialize settings first to get log level
        Settings settings = customSettingsPath != null ?
            Settings.getInstance(customSettingsPath) : Settings.getInstance();
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
    
    private static String parseCommandLineArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--settings") || args[i].equals("-s")) {
                if (i + 1 < args.length) {
                    return args[i + 1];
                } else {
                    System.err.println("Error: --settings option requires a file path");
                    System.exit(1);
                }
            } else if (args[i].equals("--help") || args[i].equals("-h")) {
                printUsage();
                System.exit(0);
            }
        }
        return null;
    }
    
    private static void printUsage() {
        System.out.println("Usage: java -jar java_3d_concept.jar [options]");
        System.out.println("Options:");
        System.out.println("  -s, --settings <path>   Path to custom settings file");
        System.out.println("  -h, --help              Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar java_3d_concept.jar");
        System.out.println("  java -jar java_3d_concept.jar --settings my_config.json");
        System.out.println("  java -jar java_3d_concept.jar -s /path/to/custom_settings.json");
    }
}
