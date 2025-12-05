package com.sim3d.graphics;

import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class Mesh {
    private int vaoId;
    private int vboId;
    private int eboId;
    private int vertexCount;

    public Mesh(float[] vertices, int[] indices) {
        vertexCount = indices.length;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            vboId = glGenBuffers();
            FloatBuffer vertexBuffer = stack.mallocFloat(vertices.length);
            vertexBuffer.put(vertices).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

            eboId = glGenBuffers();
            IntBuffer indexBuffer = stack.mallocInt(indices.length);
            indexBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

            int stride = 9 * Float.BYTES;

            glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);

            glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 6 * Float.BYTES);
            glEnableVertexAttribArray(2);

            glBindVertexArray(0);
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

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
