package com.sim3d.engine;

import com.sim3d.graphics.Renderer;
import com.sim3d.graphics.TextRenderer;
import com.sim3d.input.InputHandler;
import com.sim3d.input.MouseInput;
import com.sim3d.loader.WorldLoader;
import com.sim3d.model.Environment;
import com.sim3d.model.Portal;
import com.sim3d.model.Player;
import com.sim3d.model.World;
import com.sim3d.ui.MenuSystem;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;

public class Engine {
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    private String worldPath;
    private Window window;
    private Renderer renderer;
    private TextRenderer textRenderer;
    private InputHandler inputHandler;
    private MouseInput mouseInput;
    private MenuSystem menuSystem;
    private World world;
    private Player player;
    private boolean running;

    private long lastTime;
    
    // FPS calculation variables
    private int frameCount = 0;
    private double fpsTimeAccumulator = 0.0;
    private double currentFPS = 0.0;
    
    // Portal cooldown to prevent rapid triggering
    private float portalCooldownTimer = 0.0f;
    private static final float PORTAL_COOLDOWN_TIME = 2.0f; // 2 seconds cooldown

    public void init() {
        logger.info("Initializing engine...");

        Settings settings = Settings.getInstance();

        WorldLoader worldLoader = new WorldLoader();
        try {
            worldPath = settings.getWorldPath();
            world = worldLoader.loadWorld(worldPath);
            if (world == null) {
                logger.warn("World loader returned null for {}, creating empty world", worldPath);
                world = new World("empty", "Empty World");
            } else {
                logger.info("World loaded: {}", world.getName());
            }
        } catch (Exception e) {
            logger.error("Failed to load world from {}, creating empty world: {}", worldPath, e.getMessage(), e);
            world = new World("empty", "Empty World");
        }
        
        window = new Window(world.getName(), settings.getWindowWidth(), settings.getWindowHeight(), settings.isFullscreen());
        window.init();

        renderer = new Renderer();
        renderer.init();

        inputHandler = new InputHandler();
        inputHandler.init(window.getWindowHandle());

        mouseInput = new MouseInput();
        mouseInput.init(window.getWindowHandle());

        menuSystem = new MenuSystem();
        menuSystem.initialize();
        
        textRenderer = new TextRenderer();

        player = new Player();
        Environment currentEnv = world.getCurrentEnvironment();
        if (currentEnv != null) {
            player.setPosition(currentEnv.getSpawnPoint());
            // Preload models and textures for the current environment
            try {
                renderer.preloadModels(currentEnv);
            } catch (Exception e) {
                logger.warn("Failed to preload models for environment {}: {}", currentEnv.getName(), e.getMessage());
            }
        } else {
            logger.warn("No current environment found in world");
        }

        mouseInput.captureMouse();

        lastTime = System.nanoTime();
        running = true;

        logger.info("Engine initialized successfully");
    }

    public void run() {
        while (running && !window.shouldClose()) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;
            lastTime = currentTime;
            
            // Update FPS calculation
            updateFPS(deltaTime);

            glfwPollEvents();
            update(deltaTime);
            render();
            window.swapBuffers();

            if (shouldExit()) {
                running = false;
            }
        }
    }

    private void update(float deltaTime) {
        if (inputHandler.isEscapePressed()) {
            menuSystem.toggle();
            if (menuSystem.isVisible()) {
                mouseInput.releaseMouse();
                logger.info("Menu opened");
            } else {
                mouseInput.captureMouse();
                logger.info("Menu closed");
            }
        }

        if (menuSystem.isVisible()) {
            handleMenuInput();
        } else {
            mouseInput.update();
            updatePlayerMovement(deltaTime);
        }

        Environment currentEnv = world.getCurrentEnvironment();
        if (currentEnv != null) {
            currentEnv.update(deltaTime);
            checkPortalTriggers(deltaTime);
        }
    }

    private void handleMenuInput() {
        if (inputHandler.isKeyPressed(GLFW_KEY_UP)) {
            menuSystem.navigateUp();
        }
        if (inputHandler.isKeyPressed(GLFW_KEY_DOWN)) {
            menuSystem.navigateDown();
        }
        if (inputHandler.isKeyPressed(GLFW_KEY_ENTER)) {
            int selected = menuSystem.getSelectedOption();
            handleMenuSelection(selected);
        }
    }

    private void handleMenuSelection(int option) {
        switch (option) {
            case 0 -> {
                menuSystem.hide();
                mouseInput.captureMouse();
                logger.info("Resuming game");
            }
            case 1 -> {
                logger.info("Exiting game");
                running = false;
            }
        }
    }

    private void updatePlayerMovement(float deltaTime) {
        boolean forward = inputHandler.isKeyDown(GLFW_KEY_W);
        boolean backward = inputHandler.isKeyDown(GLFW_KEY_S);
        boolean left = inputHandler.isKeyDown(GLFW_KEY_A);
        boolean right = inputHandler.isKeyDown(GLFW_KEY_D);
        boolean up = inputHandler.isKeyDown(GLFW_KEY_SPACE);
        boolean down = inputHandler.isKeyDown(GLFW_KEY_LEFT_SHIFT);

        float mouseDeltaX = mouseInput.getDeltaX();
        float mouseDeltaY = mouseInput.getDeltaY();

        player.update(deltaTime, forward, backward, left, right, up, down, mouseDeltaX, mouseDeltaY);
    }

    private void checkPortalTriggers(float deltaTime) {
        Environment currentEnv = world.getCurrentEnvironment();
        if (currentEnv == null) return;

        // Update portal cooldown
        if (portalCooldownTimer > 0) {
            portalCooldownTimer -= deltaTime;
            return;
        }

        Vector3f playerPos = player.getPosition();
        
        for (Portal portal : currentEnv.getPortals()) {
            if (portal.isPlayerInTrigger(playerPos)) {
                logger.info("Player entered portal: {} -> {}", portal.getId(), portal.getTargetEnvironmentId());
                
                // Transition to the target environment
                world.transitionTo(portal.getTargetEnvironmentId(), portal.getTargetSpawnPointId());
                
                // Get the spawn point for the new environment
                world.getSpawnPointForTransition(portal.getTargetEnvironmentId(), portal.getTargetSpawnPointId())
                    .ifPresent(spawnPoint -> {
                        player.setPosition(spawnPoint);
                        logger.info("Player teleported to spawn point: {}", spawnPoint);
                    });
                
                // Preload models for the new environment
                Environment newEnv = world.getCurrentEnvironment();
                if (newEnv != null) {
                    try {
                        renderer.preloadModels(newEnv);
                    } catch (Exception e) {
                        logger.warn("Failed to preload models for environment {}: {}",
                                   newEnv.getName(), e.getMessage());
                    }
                }
                
                // Set portal cooldown to prevent rapid triggering
                portalCooldownTimer = PORTAL_COOLDOWN_TIME;
                
                // Only process one portal per frame to avoid rapid transitions
                break;
            }
        }
    }

    private void render() {
        Environment currentEnv = world.getCurrentEnvironment();
        if (currentEnv != null) {
            renderer.render(currentEnv, player, window.getWidth(), window.getHeight());
        }

        if (menuSystem.isVisible()) {
            menuSystem.render(window.getWidth(), window.getHeight());
        }
        
        // Render FPS if enabled
        Settings settings = Settings.getInstance();
        if (settings.isShowFPS()) {
            renderFPS();
        }
    }
    
    private void updateFPS(float deltaTime) {
        frameCount++;
        fpsTimeAccumulator += deltaTime;
        
        // Update FPS every second
        if (fpsTimeAccumulator >= 1.0) {
            currentFPS = frameCount / fpsTimeAccumulator;
            frameCount = 0;
            fpsTimeAccumulator = 0.0;
        }
    }
    
    private void renderFPS() {
        String fpsText = String.format("FPS: %.1f", currentFPS);
        float[] whiteColor = {1.0f, 1.0f, 1.0f};
        
        // Position in upper-right corner with some padding
        float x = window.getWidth() - textRenderer.getTextWidth(fpsText, 0.5f) - 20;
        float y = 30;
        
        textRenderer.renderText(fpsText, x, y, 0.5f, whiteColor, window.getWidth(), window.getHeight());
    }

    private boolean shouldExit() {
        return !running;
    }

    public void cleanup() {
        logger.info("Cleaning up engine...");

        if (renderer != null) {
            renderer.cleanup();
        }
        if (textRenderer != null) {
            textRenderer.cleanup();
        }
        if (menuSystem != null) {
            menuSystem.cleanup();
        }
        if (window != null) {
            window.cleanup();
        }

        logger.info("Engine cleanup complete");
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }
}
