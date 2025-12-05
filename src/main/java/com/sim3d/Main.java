package com.sim3d;

import com.sim3d.engine.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
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
}
