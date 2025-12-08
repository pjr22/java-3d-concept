package com.sim3d.graphics;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class Mesh {
    private int vaoId;
    private int vboId;
    private int eboId;
    private int vertexCount;
    private boolean hasTextureCoords;

    public Mesh(float[] vertices, int[] indices) {
        this(vertices, indices, false);
    }

    public Mesh(float[] vertices, int[] indices, boolean hasTextureCoords) {
        vertexCount = indices.length;
        this.hasTextureCoords = hasTextureCoords;

        FloatBuffer vertexBuffer = null;
        IntBuffer indexBuffer = null;
        
        try {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            vboId = glGenBuffers();
            vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
            vertexBuffer.put(vertices).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

            eboId = glGenBuffers();
            indexBuffer = MemoryUtil.memAllocInt(indices.length);
            indexBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

            int stride = hasTextureCoords ? 11 * Float.BYTES : 9 * Float.BYTES;

            // Position attribute (location = 0)
            glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
            glEnableVertexAttribArray(0);

            // Color attribute (location = 1)
            glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);

            // Normal attribute (location = 2)
            glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 6 * Float.BYTES);
            glEnableVertexAttribArray(2);

            // Texture coordinate attribute (location = 3) - only enable if texture coordinates are available
            if (hasTextureCoords) {
                glVertexAttribPointer(3, 2, GL_FLOAT, false, stride, 9 * Float.BYTES);
                glEnableVertexAttribArray(3);
            }

            glBindVertexArray(0);
        } finally {
            if (vertexBuffer != null) {
                MemoryUtil.memFree(vertexBuffer);
            }
            if (indexBuffer != null) {
                MemoryUtil.memFree(indexBuffer);
            }
        }
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        if (hasTextureCoords) {
            glDisableVertexAttribArray(3); // Only disable texture coordinate attribute if it was enabled
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public boolean hasTextureCoords() {
        return hasTextureCoords;
    }
}
