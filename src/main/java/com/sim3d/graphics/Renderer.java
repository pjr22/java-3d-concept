package com.sim3d.graphics;

import com.sim3d.graphics.primitives.PrimitiveFactory;
import com.sim3d.loader.AssetManager;
import com.sim3d.loader.Model;
import com.sim3d.model.Environment;
import com.sim3d.model.GameObject;
import com.sim3d.model.Player;
import com.sim3d.model.Transform;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL33.*;

public class Renderer {
    private static final Logger logger = LoggerFactory.getLogger(Renderer.class);
    
    private ShaderProgram shaderProgram;
    private Camera camera;
    private Mesh groundPlane;
    private Map<String, Mesh> primitiveMeshes;
    private Vector3f lightDirection;
    private AssetManager assetManager;
    private Texture grassTexture;

    public Renderer() {
        this.camera = new Camera();
        this.primitiveMeshes = new HashMap<>();
        this.lightDirection = new Vector3f(-0.5f, -1.0f, -0.3f).normalize();
        this.assetManager = AssetManager.getInstance();
    }

    public void init() {
        logger.info("Initializing renderer...");
        
        shaderProgram = ShaderProgram.loadFromResources("shaders/vertex.glsl", "shaders/fragment.glsl");

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        primitiveMeshes.put("cube", PrimitiveFactory.createCube(new Vector3f(1, 1, 1)));
        primitiveMeshes.put("sphere", PrimitiveFactory.createSphere(new Vector3f(1, 1, 1), 16));
        primitiveMeshes.put("cylinder", PrimitiveFactory.createCylinder(new Vector3f(1, 1, 1), 16));
        primitiveMeshes.put("pyramid", PrimitiveFactory.createPyramid(new Vector3f(1, 1, 1)));
        
        logger.info("Renderer initialized with {} primitive meshes", primitiveMeshes.size());
    }

    public void render(Environment environment, Player player, int windowWidth, int windowHeight) {
        Vector3f skyColor = environment.getSkyColor();
        glClearColor(skyColor.x, skyColor.y, skyColor.z, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        camera.updateFromPlayer(player);

        shaderProgram.bind();

        float aspectRatio = (float) windowWidth / windowHeight;
        Matrix4f projectionMatrix = camera.getProjectionMatrix(aspectRatio);
        Matrix4f viewMatrix = camera.getViewMatrix();

        shaderProgram.setUniform("projection", projectionMatrix);
        shaderProgram.setUniform("view", viewMatrix);
        shaderProgram.setUniform("lightDirection", lightDirection);
        shaderProgram.setUniform("ambientStrength", 0.3f);
        shaderProgram.setUniform("textureSampler", 0);

        renderGroundPlane(environment);

        for (GameObject obj : environment.getObjects()) {
            renderGameObject(obj);
        }

        shaderProgram.unbind();
    }

    private void renderGroundPlane(Environment environment) {
        if (groundPlane != null) {
            groundPlane.cleanup();
        }

        Vector3f bounds = environment.getBounds();
        Vector3f groundColor = environment.getGroundColor();
        
        // Create a textured plane with tiling (scale factor controls how many times texture repeats)
        float textureScale = 10.0f; // Texture will repeat every 10 units
        groundPlane = PrimitiveFactory.createTexturedPlane(bounds.x * 2, bounds.z * 2, groundColor, textureScale);

        Matrix4f modelMatrix = new Matrix4f().identity();
        shaderProgram.setUniform("model", modelMatrix);
        shaderProgram.setUniform("objectColor", groundColor);

        // Load and bind grass texture
        if (grassTexture == null) {
            grassTexture = TextureLoader.load("textures/grass_tile.jpg");
        }
        
        if (grassTexture != null) {
            grassTexture.bind(0);
            shaderProgram.setUniform("useTexture", true);
            groundPlane.render();
            grassTexture.unbind();
        } else {
            // Fallback to untextured rendering if texture loading fails
            shaderProgram.setUniform("useTexture", false);
            groundPlane.render();
        }
    }

    private void renderGameObject(GameObject obj) {
        Transform transform = obj.getTransform();
        Vector3f pos = transform.getPosition();
        Vector3f rot = transform.getRotation();
        Vector3f scale = transform.getScale();

        Matrix4f modelMatrix = new Matrix4f()
            .identity()
            .translate(pos)
            .rotateX((float) Math.toRadians(rot.x))
            .rotateY((float) Math.toRadians(rot.y))
            .rotateZ((float) Math.toRadians(rot.z))
            .scale(scale);

        shaderProgram.setUniform("model", modelMatrix);
        shaderProgram.setUniform("objectColor", obj.getColor());

        if (obj.hasCustomModel()) {
            renderCustomModel(obj);
        } else {
            renderPrimitive(obj);
        }
    }

    private void renderCustomModel(GameObject obj) {
        String modelPath = obj.getModelPath();
        Model model = assetManager.getModel(modelPath);
        
        if (model != null) {
            // Handle texture binding
            boolean useTexture = false;
            Texture texture = null;
            if (model.hasTexture()) {
                texture = model.getTexture();
                if (texture != null) {
                    texture.bind(0);
                    useTexture = true;
                }
            }
            
            shaderProgram.setUniform("useTexture", useTexture);
            model.render(useTexture);
            
            if (useTexture && texture != null) {
                texture.unbind();
            }
        } else {
            renderPrimitive(obj);
        }
    }

    private void renderPrimitive(GameObject obj) {
        String modelType = obj.getModelType();
        Mesh mesh = primitiveMeshes.get(modelType);

        if (mesh == null) {
            mesh = primitiveMeshes.get("cube");
        }

        if (mesh != null) {
            // Primitives don't use textures and don't have texture coordinates
            shaderProgram.setUniform("useTexture", false);
            mesh.render();
        }
    }

    public void preloadModels(Environment environment) {
        logger.info("Preloading models for environment: {}", environment.getName());
        
        for (GameObject obj : environment.getObjects()) {
            if (obj.hasCustomModel()) {
                String modelPath = obj.getModelPath();
                String texturePath = obj.getTexturePath();
                
                if (!assetManager.hasModel(modelPath)) {
                    assetManager.loadModel(modelPath, texturePath);
                }
                // Note: Texture is loaded automatically by loadModel() when texturePath is provided
            }
        }
    }

    public void cleanup() {
        logger.info("Cleaning up renderer...");
        
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
        if (groundPlane != null) {
            groundPlane.cleanup();
        }
        if (grassTexture != null) {
            grassTexture.cleanup();
        }
        for (Mesh mesh : primitiveMeshes.values()) {
            mesh.cleanup();
        }
        primitiveMeshes.clear();
        
        assetManager.cleanup();
        
        logger.info("Renderer cleanup complete");
    }

    public Camera getCamera() {
        return camera;
    }

    public void setLightDirection(Vector3f direction) {
        this.lightDirection.set(direction).normalize();
    }
    
    public AssetManager getAssetManager() {
        return assetManager;
    }
}
