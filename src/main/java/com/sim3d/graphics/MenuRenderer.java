package com.sim3d.graphics;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.*;

public class MenuRenderer {
    private ShaderProgram shaderProgram;
    private int vao, vbo;
    private TextRenderer textRenderer;
    
    public MenuRenderer() {
        setupShaders();
        setupGeometry();
        textRenderer = new TextRenderer();
    }
    
    private void setupShaders() {
        shaderProgram = ShaderProgram.loadFromResources("shaders/ui_vertex.glsl", "shaders/ui_fragment.glsl");
    }
    
    private void setupGeometry() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        
        // Position (2 floats) + TexCoord (2 floats) = 4 floats per vertex
        glBufferData(GL_ARRAY_BUFFER, 6 * 4 * Float.BYTES, GL_DYNAMIC_DRAW);
        
        // Position attribute
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // TexCoord attribute
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    public void renderMenuBackground(int windowWidth, int windowHeight) {
        // Save current OpenGL state
        boolean depthTestEnabled = glIsEnabled(GL_DEPTH_TEST);
        boolean cullFaceEnabled = glIsEnabled(GL_CULL_FACE);
        boolean blendEnabled = glIsEnabled(GL_BLEND);
        
        // Set up state for UI rendering
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        shaderProgram.bind();
        
        // Set up orthographic projection for 2D rendering
        Matrix4f projection = new Matrix4f().ortho2D(0, windowWidth, windowHeight, 0);
        shaderProgram.setUniform("projection", projection);
        shaderProgram.setUniform("textColor", new org.joml.Vector3f(0.1f, 0.1f, 0.1f));
        shaderProgram.setUniform("useTexture", 0.0f); // Don't use texture for background
        
        // Create semi-transparent background overlay
        float[] vertices = {
            // Position     // TexCoord (unused for background)
            0, 0,          0, 0,
            windowWidth, 0, 0, 0,
            0, windowHeight, 0, 0,
            
            windowWidth, 0, 0, 0,
            windowWidth, windowHeight, 0, 0,
            0, windowHeight, 0, 0
        };
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        glBindVertexArray(0);
        shaderProgram.unbind();
        
        // Restore OpenGL state
        if (!blendEnabled) glDisable(GL_BLEND);
        if (depthTestEnabled) glEnable(GL_DEPTH_TEST);
        if (cullFaceEnabled) glEnable(GL_CULL_FACE);
    }
    
    public void renderMenuPanel(int windowWidth, int windowHeight) {
        // Save current OpenGL state
        boolean depthTestEnabled = glIsEnabled(GL_DEPTH_TEST);
        boolean cullFaceEnabled = glIsEnabled(GL_CULL_FACE);
        boolean blendEnabled = glIsEnabled(GL_BLEND);
        
        // Set up state for UI rendering
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        shaderProgram.bind();
        
        // Set up orthographic projection for 2D rendering
        Matrix4f projection = new Matrix4f().ortho2D(0, windowWidth, windowHeight, 0);
        shaderProgram.setUniform("projection", projection);
        shaderProgram.setUniform("textColor", new org.joml.Vector3f(0.2f, 0.2f, 0.3f));
        shaderProgram.setUniform("useTexture", 0.0f); // Don't use texture for panel
        
        // Calculate panel dimensions and position (centered)
        float panelWidth = 400;
        float panelHeight = 200;
        float panelX = (windowWidth - panelWidth) / 2;
        float panelY = (windowHeight - panelHeight) / 2;
        
        float[] vertices = {
            // Position     // TexCoord (unused for panel)
            panelX, panelY,                    0, 0,
            panelX + panelWidth, panelY,       0, 0,
            panelX, panelY + panelHeight,      0, 0,
            
            panelX + panelWidth, panelY,       0, 0,
            panelX + panelWidth, panelY + panelHeight, 0, 0,
            panelX, panelY + panelHeight,      0, 0
        };
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        glBindVertexArray(0);
        shaderProgram.unbind();
        
        // Restore OpenGL state
        if (!blendEnabled) glDisable(GL_BLEND);
        if (depthTestEnabled) glEnable(GL_DEPTH_TEST);
        if (cullFaceEnabled) glEnable(GL_CULL_FACE);
    }
    
    public void renderMenuText(String text, float x, float y, float scale, float[] color) {
        textRenderer.renderText(text, x, y, scale, color); // Use backward compatibility method
    }
    
    public void renderMenuOptions(java.util.List<String> options, int selectedIndex, int windowWidth, int windowHeight) {
        float startY = (windowHeight - (options.size() * 80)) / 2;
        float scale = 3.0f; // Increased scale for better visibility
        
        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            float textWidth = textRenderer.getTextWidth(option, scale);
            float x = (windowWidth - textWidth) / 2;
            float y = startY + i * 80;
            
            // Highlight selected option
            if (i == selectedIndex) {
                float[] selectedColor = {1.0f, 1.0f, 0.0f}; // Yellow for selected
                textRenderer.renderText("> " + option + " <", x - 40, y, scale, selectedColor, windowWidth, windowHeight);
            } else {
                float[] normalColor = {1.0f, 1.0f, 1.0f}; // White for normal
                textRenderer.renderText(option, x, y, scale, normalColor, windowWidth, windowHeight);
            }
        }
    }
    
    public void renderMenuTitle(String title, int windowWidth, int windowHeight) {
        float scale = 4.0f; // Larger scale for title
        float textWidth = textRenderer.getTextWidth(title, scale);
        float x = (windowWidth - textWidth) / 2;
        float y = windowHeight / 2 - 150;
        
        float[] titleColor = {1.0f, 1.0f, 1.0f}; // White for title
        textRenderer.renderText(title, x, y, scale, titleColor, windowWidth, windowHeight);
    }
    
    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
        if (textRenderer != null) {
            textRenderer.cleanup();
        }
        if (vao != 0) {
            glDeleteVertexArrays(vao);
        }
        if (vbo != 0) {
            glDeleteBuffers(vbo);
        }
    }
}