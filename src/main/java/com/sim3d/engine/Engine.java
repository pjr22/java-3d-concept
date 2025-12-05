package com.sim3d.engine;

import com.sim3d.graphics.Renderer;
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
    private InputHandler inputHandler;
    private MouseInput mouseInput;
    private MenuSystem menuSystem;
    private World world;
    private Player player;
    private boolean running;

    private long lastTime;

    public void init() {
        logger.info("Initializing engine...");

        window = new Window("3D Simulation Engine", 1280, 720);
        window.init();

        renderer = new Renderer();
        renderer.init();

        inputHandler = new InputHandler();
        inputHandler.init(window.getWindowHandle());

        mouseInput = new MouseInput();
        mouseInput.init(window.getWindowHandle());

        menuSystem = new MenuSystem();

        WorldLoader worldLoader = new WorldLoader();
        try {
            world = worldLoader.loadWorld(WORLD_PATH);
            logger.info("World loaded: {}", world.getName());
        } catch (Exception e) {
            logger.warn("Failed to load world from {}, creating empty world: {}", WORLD_PATH, e.getMessage());
            world = new World("empty", "Empty World");
        }

        player = new Player();
        Environment currentEnv = world.getCurrentEnvironment();
        if (currentEnv != null) {
            player.setPosition(currentEnv.getSpawnPoint());
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
            menuSystem.render();
        }
    }

    private boolean shouldExit() {
        return !running;
    }

    public void cleanup() {
        logger.info("Cleaning up engine...");

        if (renderer != null) {
            renderer.cleanup();
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
