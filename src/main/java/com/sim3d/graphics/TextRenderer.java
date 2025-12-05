package com.sim3d.graphics;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBTruetype.*;

public class TextRenderer {
    private ShaderProgram shaderProgram;
    private int vao, vbo;
    private int fontTexture;
    private ByteBuffer fontBuffer;
    private STBTTPackedchar.Buffer charData;
    
    private static final int BITMAP_WIDTH = 512;
    private static final int BITMAP_HEIGHT = 512;
    private static final int FIRST_CHAR = 32;  // ASCII space
    private static final int CHAR_COUNT = 96;  // ASCII 32-127
    private static final float FONT_HEIGHT = 48.0f;
    
    public TextRenderer() {
        setupShaders();
        setupGeometry();
        loadFont();
    }
    
    private void setupShaders() {
        shaderProgram = ShaderProgram.loadFromResources("shaders/ui_vertex.glsl", "shaders/ui_fragment.glsl");
    }
    
    private void setupGeometry() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        
        // Allocate buffer for a batch of quads (6 vertices per char, 4 floats per vertex)
        glBufferData(GL_ARRAY_BUFFER, 256 * 6 * 4 * Float.BYTES, GL_DYNAMIC_DRAW);
        
        // Position attribute (2 floats)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // TexCoord attribute (2 floats)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    private void loadFont() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("Roboto.ttf");
            if (is == null) {
                throw new RuntimeException("Roboto.ttf not found in resources");
            }
            
            byte[] fontBytes = is.readAllBytes();
            fontBuffer = BufferUtils.createByteBuffer(fontBytes.length);
            fontBuffer.put(fontBytes);
            fontBuffer.flip();
            is.close();
            
            createFontAtlas();
            
        } catch (Exception e) {
            throw new RuntimeException("Font loading failed: " + e.getMessage(), e);
        }
    }
    
    private void createFontAtlas() {
        charData = STBTTPackedchar.malloc(CHAR_COUNT);
        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_WIDTH * BITMAP_HEIGHT);
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTPackContext packContext = STBTTPackContext.malloc(stack);
            
            if (!stbtt_PackBegin(packContext, bitmap, BITMAP_WIDTH, BITMAP_HEIGHT, 0, 1, 0)) {
                throw new RuntimeException("Failed to initialize font packer");
            }
            
            stbtt_PackSetOversampling(packContext, 2, 2);
            
            if (!stbtt_PackFontRange(packContext, fontBuffer, 0, FONT_HEIGHT, FIRST_CHAR, charData)) {
                throw new RuntimeException("Failed to pack font range");
            }
            
            stbtt_PackEnd(packContext);
        }
        
        fontTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, fontTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, BITMAP_WIDTH, BITMAP_HEIGHT, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    public void renderText(String text, float x, float y, float scale, float[] color, int windowWidth, int windowHeight) {
        if (text == null || text.isEmpty()) return;
        
        boolean depthTestEnabled = glIsEnabled(GL_DEPTH_TEST);
        boolean cullFaceEnabled = glIsEnabled(GL_CULL_FACE);
        boolean blendEnabled = glIsEnabled(GL_BLEND);
        
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        shaderProgram.bind();
        
        Matrix4f projection = new Matrix4f().ortho2D(0, windowWidth, windowHeight, 0);
        shaderProgram.setUniform("projection", projection);
        shaderProgram.setUniform("textColor", new org.joml.Vector3f(color[0], color[1], color[2]));
        shaderProgram.setUniform("useTexture", 1.0f);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fontTexture);
        shaderProgram.setUniform("textTexture", 0);
        
        glBindVertexArray(vao);
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xPos = stack.floats(x);
            FloatBuffer yPos = stack.floats(y + FONT_HEIGHT * scale);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            
            FloatBuffer vertexData = stack.mallocFloat(text.length() * 6 * 4);
            int vertexCount = 0;
            
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                
                if (c < FIRST_CHAR || c >= FIRST_CHAR + CHAR_COUNT) {
                    continue;
                }
                
                float xBefore = xPos.get(0);
                stbtt_GetPackedQuad(charData, BITMAP_WIDTH, BITMAP_HEIGHT, c - FIRST_CHAR, xPos, yPos, quad, false);
                float xAfter = xPos.get(0);
                
                // Apply scale by adjusting positions
                float advance = (xAfter - xBefore) * scale;
                xPos.put(0, xBefore + advance);
                
                float x0 = x + (quad.x0() - x) * scale;
                float y0 = y + (quad.y0() - y) * scale;
                float x1 = x + (quad.x1() - x) * scale;
                float y1 = y + (quad.y1() - y) * scale;
                
                float s0 = quad.s0();
                float t0 = quad.t0();
                float s1 = quad.s1();
                float t1 = quad.t1();
                
                // First triangle
                vertexData.put(x0).put(y0).put(s0).put(t0);
                vertexData.put(x1).put(y0).put(s1).put(t0);
                vertexData.put(x0).put(y1).put(s0).put(t1);
                
                // Second triangle
                vertexData.put(x1).put(y0).put(s1).put(t0);
                vertexData.put(x1).put(y1).put(s1).put(t1);
                vertexData.put(x0).put(y1).put(s0).put(t1);
                
                vertexCount += 6;
                
                // Update x position for next character
                x = xPos.get(0);
            }
            
            if (vertexCount > 0) {
                vertexData.flip();
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferSubData(GL_ARRAY_BUFFER, 0, vertexData);
                glBindBuffer(GL_ARRAY_BUFFER, 0);
                
                glDrawArrays(GL_TRIANGLES, 0, vertexCount);
            }
        }
        
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        shaderProgram.unbind();
        
        if (!blendEnabled) glDisable(GL_BLEND);
        if (depthTestEnabled) glEnable(GL_DEPTH_TEST);
        if (cullFaceEnabled) glEnable(GL_CULL_FACE);
    }
    
    public float getTextWidth(String text, float scale) {
        if (text == null || text.isEmpty()) return 0;
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xPos = stack.floats(0);
            FloatBuffer yPos = stack.floats(0);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c >= FIRST_CHAR && c < FIRST_CHAR + CHAR_COUNT) {
                    stbtt_GetPackedQuad(charData, BITMAP_WIDTH, BITMAP_HEIGHT, c - FIRST_CHAR, xPos, yPos, quad, false);
                }
            }
            
            return xPos.get(0) * scale;
        }
    }
    
    public float getTextHeight(float scale) {
        return FONT_HEIGHT * scale;
    }
    
    public void renderText(String text, float x, float y, float scale, float[] color) {
        renderText(text, x, y, scale, color, 1920, 1080);
    }
    
    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
        if (vao != 0) {
            glDeleteVertexArrays(vao);
        }
        if (vbo != 0) {
            glDeleteBuffers(vbo);
        }
        if (fontTexture != 0) {
            glDeleteTextures(fontTexture);
        }
        if (charData != null) {
            charData.free();
        }
    }
}
