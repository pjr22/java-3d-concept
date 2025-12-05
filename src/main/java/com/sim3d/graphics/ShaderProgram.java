package com.sim3d.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL33.*;

public class ShaderProgram {
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private final Map<String, Integer> uniformLocations = new HashMap<>();

    public ShaderProgram(String vertexCode, String fragmentCode) {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Could not create shader program");
        }

        vertexShaderId = createShader(vertexCode, GL_VERTEX_SHADER);
        fragmentShaderId = createShader(fragmentCode, GL_FRAGMENT_SHADER);

        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error linking shader program: " + glGetProgramInfoLog(programId));
        }

        glDetachShader(programId, vertexShaderId);
        glDetachShader(programId, fragmentShaderId);

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE) {
            System.err.println("Warning validating shader program: " + glGetProgramInfoLog(programId));
        }
    }

    public static ShaderProgram loadFromResources(String vertexPath, String fragmentPath) {
        String vertexCode = loadResource(vertexPath);
        String fragmentCode = loadResource(fragmentPath);
        return new ShaderProgram(vertexCode, fragmentCode);
    }

    private static String loadResource(String path) {
        try (InputStream is = ShaderProgram.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + path, e);
        }
    }

    private int createShader(String code, int type) {
        int shaderId = glCreateShader(type);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader. Type: " + type);
        }

        glShaderSource(shaderId, code);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error compiling shader: " + glGetShaderInfoLog(shaderId));
        }

        return shaderId;
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            if (vertexShaderId != 0) {
                glDeleteShader(vertexShaderId);
            }
            if (fragmentShaderId != 0) {
                glDeleteShader(fragmentShaderId);
            }
            glDeleteProgram(programId);
        }
    }

    private int getUniformLocation(String name) {
        return uniformLocations.computeIfAbsent(name, n -> {
            int location = glGetUniformLocation(programId, n);
            if (location < 0) {
                System.err.println("Warning: uniform '" + n + "' not found in shader");
            }
            return location;
        });
    }

    public void setUniform(String name, Matrix4f value) {
        int location = getUniformLocation(name);
        if (location >= 0) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer buffer = stack.mallocFloat(16);
                value.get(buffer);
                glUniformMatrix4fv(location, false, buffer);
            }
        }
    }

    public void setUniform(String name, Vector3f value) {
        int location = getUniformLocation(name);
        if (location >= 0) {
            glUniform3f(location, value.x, value.y, value.z);
        }
    }

    public void setUniform(String name, float value) {
        int location = getUniformLocation(name);
        if (location >= 0) {
            glUniform1f(location, value);
        }
    }

    public void setUniform(String name, int value) {
        int location = getUniformLocation(name);
        if (location >= 0) {
            glUniform1i(location, value);
        }
    }
}
