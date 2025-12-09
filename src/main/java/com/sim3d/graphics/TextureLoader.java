package com.sim3d.graphics;

import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.stream.Collectors;

import static org.lwjgl.stb.STBImage.*;

public class TextureLoader {
    private static final Logger logger = LoggerFactory.getLogger(TextureLoader.class);

    public static Texture load(String resourcePath) {
        logger.info("Loading texture from resource: {}", resourcePath);
        
        // Handle SVG files by converting them to a simple colored texture
        if (resourcePath.toLowerCase().endsWith(".svg")) {
            return loadSvgAsTexture(resourcePath);
        }
        
        try (InputStream is = TextureLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                logger.warn("Texture resource not found: {}", resourcePath);
                return createDefaultTexture(resourcePath);
            }
            
            logger.info("Found texture resource: {}", resourcePath);
            return loadFromInputStream(is, resourcePath);
        } catch (Exception e) {
            logger.warn("Failed to load texture resource: {}", resourcePath, e);
            return createDefaultTexture(resourcePath);
        }
    }

    private static Texture loadFromInputStream(InputStream is, String resourcePath) throws IOException {
        // Read all bytes from InputStream
        byte[] bytes = is.readAllBytes();
        logger.info("Read {} bytes from {}", bytes.length, resourcePath);
        
        // Check PNG signature
        if (bytes.length >= 8) {
            logger.info("First 8 bytes: {} {} {} {} {} {} {} {}",
                bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
        }
        
        // Create a direct ByteBuffer for STB compatibility
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes);
        buffer.flip(); // Prepare buffer for reading
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            
            // Set flip vertically on load for OpenGL coordinate system
            stbi_set_flip_vertically_on_load(true);
            
            ByteBuffer image = stbi_load_from_memory(buffer, width, height, channels, 4);
            if (image == null) {
                String error = stbi_failure_reason();
                logger.warn("Failed to load image from {}: {}", resourcePath, error);
                return createDefaultTexture(resourcePath);
            }
            
            int w = width.get();
            int h = height.get();
            int c = channels.get();
            logger.info("Successfully loaded image: {}x{} ({} original channels, forced to 4)", w, h, c);
            
            try {
                // We requested 4 channels (RGBA), so always pass 4 regardless of original
                return new Texture(w, h, image, resourcePath, 4);
            } finally {
                stbi_image_free(image);
            }
        }
    }

    private static Texture loadSvgAsTexture(String resourcePath) {
        logger.debug("Converting SVG to texture: {}", resourcePath);
        
        try (InputStream is = TextureLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                logger.warn("SVG resource not found: {}", resourcePath);
                return createDefaultTexture(resourcePath);
            }
            
            // Read SVG content and create a simple colored texture
            String svgContent = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining("\n"));
            
            // For now, create a simple colored texture based on SVG content
            // In a more advanced implementation, we could use a proper SVG library
            return createSvgBasedTexture(svgContent, resourcePath);
            
        } catch (Exception e) {
            logger.warn("Failed to load SVG texture: {}", resourcePath, e);
            return createDefaultTexture(resourcePath);
        }
    }

    private static Texture createSvgBasedTexture(String svgContent, String resourcePath) {
        logger.warn("SVG loading not fully implemented. Creating placeholder texture for: {}", resourcePath);
        
        // Create a simple 64x64 placeholder texture to avoid stack overflow
        int width = 64;
        int height = 64;
        
        // Use direct byte buffer for OpenGL compatibility
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        
        // Create a simple gradient placeholder
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Create a simple gradient pattern as placeholder
                float r = 0.7f + 0.3f * (x / (float) width);
                float g = 0.7f + 0.3f * (y / (float) height);
                float b = 0.8f;
                
                int index = (y * width + x) * 4;
                buffer.put(index, (byte) (r * 255));
                buffer.put(index + 1, (byte) (g * 255));
                buffer.put(index + 2, (byte) (b * 255));
                buffer.put(index + 3, (byte) 255); // Alpha
            }
        }
        
        // Flip buffer for OpenGL
        buffer.rewind();
        return new Texture(width, height, buffer, resourcePath);
    }

    private static Texture createDefaultTexture(String resourcePath) {
        logger.debug("Creating default texture for: {}", resourcePath);
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int width = 64;
            int height = 64;
            
            ByteBuffer buffer = stack.malloc(width * height * 4);
            
            // Create a simple white texture with a subtle pattern
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = (y * width + x) * 4;
                    
                    // Create a checkerboard pattern
                    byte value = ((x / 8) + (y / 8)) % 2 == 0 ? (byte) 255 : (byte) 200;
                    
                    buffer.put(index, value);     // R
                    buffer.put(index + 1, value); // G
                    buffer.put(index + 2, value); // B
                    buffer.put(index + 3, (byte) 255); // A
                }
            }
            
            return new Texture(width, height, buffer, resourcePath);
        }
    }

    public static void flipVertically(ByteBuffer image, int width, int height) {
        int stride = width * 4;
        ByteBuffer temp = ByteBuffer.allocate(stride);
        
        for (int y = 0; y < height / 2; y++) {
            int topOffset = y * stride;
            int bottomOffset = (height - y - 1) * stride;
            
            // Copy top row to temp
            image.position(topOffset);
            image.get(temp.array(), 0, stride);
            
            // Copy bottom row to top
            image.position(bottomOffset);
            image.get(temp.array(), stride, stride);
            image.position(topOffset);
            image.put(temp.array(), stride, stride);
            
            // Copy temp to bottom
            image.position(bottomOffset);
            image.put(temp.array(), 0, stride);
        }
        
        image.rewind();
    }
}