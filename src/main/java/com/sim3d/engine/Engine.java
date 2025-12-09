package com.sim3d.engine;

import com.sim3d.graphics.Renderer;
import com.sim3d.graphics.TextRenderer;
import com.sim3d.input.InputHandler;
import com.sim3d.input.MouseInput;
import com.sim3d.loader.WorldLoader;
import com.sim3d.model.Environment;
import com.sim3d.model.Player;
import com.sim3d.model.World;
import com.sim3d.ui.MenuSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;

public class Engine {
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);
    private static final String WORLD_PATH = "worlds/demo_world.json";

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

    public void init() {
        logger.info("Initializing engine...");

        Settings settings = Settings.getInstance();
        window = new Window("3D Simulation Engine", settings.getWindowWidth(), settings.getWindowHeight(), settings.isFullscreen());
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

        WorldLoader worldLoader = new WorldLoader();
        try {
            world = worldLoader.loadWorld(WORLD_PATH);
            if (world == null) {
                logger.warn("World loader returned null for {}, creating empty world", WORLD_PATH);
                world = new World("empty", "Empty World");
            } else {
                logger.info("World loaded: {}", world.getName());
            }
        } catch (Exception e) {
            logger.error("Failed to load world from {}, creating empty world: {}", WORLD_PATH, e.getMessage(), e);
            world = new World("empty", "Empty World");
        }

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
