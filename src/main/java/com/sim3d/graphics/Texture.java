package com.sim3d.graphics;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    private final int textureId;
    private final int width;
    private final int height;
    private final String path;

    public Texture(int width, int height, ByteBuffer data, String path) {
        this(width, height, data, path, 4); // Default to 4 channels (RGBA)
    }
    
    public Texture(int width, int height, ByteBuffer data, String path, int channels) {
        this.path = path;
        this.width = width;
        this.height = height;
        this.textureId = glGenTextures();
        
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        // Set texture parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        
        // Determine format based on channels
        int format = GL_RGBA;
        int internalFormat = GL_RGBA;
        if (channels == 3) {
            format = GL_RGB;
            internalFormat = GL_RGB;
        } else if (channels == 1) {
            format = GL_RED;
            internalFormat = GL_RED;
        }
        
        // Upload texture data
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, data);
        glGenerateMipmap(GL_TEXTURE_2D);
        
        // Unbind texture
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void bind(int textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void cleanup() {
        glDeleteTextures(textureId);
    }

    public int getTextureId() {
        return textureId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getPath() {
        return path;
    }
}