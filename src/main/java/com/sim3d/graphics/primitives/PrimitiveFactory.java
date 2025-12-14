package com.sim3d.graphics.primitives;

import com.sim3d.graphics.Mesh;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class PrimitiveFactory {

    public static Mesh createCube(Vector3f color) {
        float r = color.x, g = color.y, b = color.z;
        float[] vertices = {
            // Front face
            -0.5f, -0.5f,  0.5f,  r, g, b,  0, 0, 1,
             0.5f, -0.5f,  0.5f,  r, g, b,  0, 0, 1,
             0.5f,  0.5f,  0.5f,  r, g, b,  0, 0, 1,
            -0.5f,  0.5f,  0.5f,  r, g, b,  0, 0, 1,
            // Back face
            -0.5f, -0.5f, -0.5f,  r, g, b,  0, 0, -1,
            -0.5f,  0.5f, -0.5f,  r, g, b,  0, 0, -1,
             0.5f,  0.5f, -0.5f,  r, g, b,  0, 0, -1,
             0.5f, -0.5f, -0.5f,  r, g, b,  0, 0, -1,
            // Top face
            -0.5f,  0.5f, -0.5f,  r, g, b,  0, 1, 0,
            -0.5f,  0.5f,  0.5f,  r, g, b,  0, 1, 0,
             0.5f,  0.5f,  0.5f,  r, g, b,  0, 1, 0,
             0.5f,  0.5f, -0.5f,  r, g, b,  0, 1, 0,
            // Bottom face
            -0.5f, -0.5f, -0.5f,  r, g, b,  0, -1, 0,
             0.5f, -0.5f, -0.5f,  r, g, b,  0, -1, 0,
             0.5f, -0.5f,  0.5f,  r, g, b,  0, -1, 0,
            -0.5f, -0.5f,  0.5f,  r, g, b,  0, -1, 0,
            // Right face
             0.5f, -0.5f, -0.5f,  r, g, b,  1, 0, 0,
             0.5f,  0.5f, -0.5f,  r, g, b,  1, 0, 0,
             0.5f,  0.5f,  0.5f,  r, g, b,  1, 0, 0,
             0.5f, -0.5f,  0.5f,  r, g, b,  1, 0, 0,
            // Left face
            -0.5f, -0.5f, -0.5f,  r, g, b,  -1, 0, 0,
            -0.5f, -0.5f,  0.5f,  r, g, b,  -1, 0, 0,
            -0.5f,  0.5f,  0.5f,  r, g, b,  -1, 0, 0,
            -0.5f,  0.5f, -0.5f,  r, g, b,  -1, 0, 0,
        };

        int[] indices = {
            0, 1, 2, 2, 3, 0,       // Front
            4, 5, 6, 6, 7, 4,       // Back
            8, 9, 10, 10, 11, 8,    // Top
            12, 13, 14, 14, 15, 12, // Bottom
            16, 17, 18, 18, 19, 16, // Right
            20, 21, 22, 22, 23, 20  // Left
        };

        return new Mesh(vertices, indices);
    }

    public static Mesh createPlane(float width, float depth, Vector3f color) {
        float r = color.x, g = color.y, b = color.z;
        float hw = width / 2;
        float hd = depth / 2;

        float[] vertices = {
            -hw, 0, -hd,  r, g, b,  0, 1, 0,
             hw, 0, -hd,  r, g, b,  0, 1, 0,
             hw, 0,  hd,  r, g, b,  0, 1, 0,
            -hw, 0,  hd,  r, g, b,  0, 1, 0,
        };

        int[] indices = {
            0, 1, 2, 2, 3, 0
        };

        return new Mesh(vertices, indices);
    }

    public static Mesh createTexturedPlane(float width, float depth, Vector3f color, float textureScale) {
        float r = color.x, g = color.y, b = color.z;
        float hw = width / 2;
        float hd = depth / 2;

        // Calculate texture coordinates based on scale (for tiling)
        float uMin = 0.0f;
        float uMax = width / textureScale;
        float vMin = 0.0f;
        float vMax = depth / textureScale;

        float[] vertices = {
            -hw, 0, -hd,  r, g, b,  0, 1, 0,  uMin, vMax,
             hw, 0, -hd,  r, g, b,  0, 1, 0,  uMax, vMax,
             hw, 0,  hd,  r, g, b,  0, 1, 0,  uMax, vMin,
            -hw, 0,  hd,  r, g, b,  0, 1, 0,  uMin, vMin,
        };

        int[] indices = {
            0, 3, 2, 2, 1, 0  // Reversed winding order to make face visible from above
        };

        return new Mesh(vertices, indices, true); // true indicates hasTextureCoords
    }

    public static Mesh createPyramid(Vector3f color) {
        float r = color.x, g = color.y, b = color.z;
        float apex = 1.0f;
        float base = 0.5f;

        Vector3f frontNormal = calculateNormal(
            new Vector3f(0, apex, 0),
            new Vector3f(-base, 0, base),
            new Vector3f(base, 0, base)
        );
        Vector3f rightNormal = calculateNormal(
            new Vector3f(0, apex, 0),
            new Vector3f(base, 0, base),
            new Vector3f(base, 0, -base)
        );
        Vector3f backNormal = calculateNormal(
            new Vector3f(0, apex, 0),
            new Vector3f(base, 0, -base),
            new Vector3f(-base, 0, -base)
        );
        Vector3f leftNormal = calculateNormal(
            new Vector3f(0, apex, 0),
            new Vector3f(-base, 0, -base),
            new Vector3f(-base, 0, base)
        );

        float[] vertices = {
            // Front face
            0, apex, 0,           r, g, b,  frontNormal.x, frontNormal.y, frontNormal.z,
            -base, 0, base,       r, g, b,  frontNormal.x, frontNormal.y, frontNormal.z,
            base, 0, base,        r, g, b,  frontNormal.x, frontNormal.y, frontNormal.z,
            // Right face
            0, apex, 0,           r, g, b,  rightNormal.x, rightNormal.y, rightNormal.z,
            base, 0, base,        r, g, b,  rightNormal.x, rightNormal.y, rightNormal.z,
            base, 0, -base,       r, g, b,  rightNormal.x, rightNormal.y, rightNormal.z,
            // Back face
            0, apex, 0,           r, g, b,  backNormal.x, backNormal.y, backNormal.z,
            base, 0, -base,       r, g, b,  backNormal.x, backNormal.y, backNormal.z,
            -base, 0, -base,      r, g, b,  backNormal.x, backNormal.y, backNormal.z,
            // Left face
            0, apex, 0,           r, g, b,  leftNormal.x, leftNormal.y, leftNormal.z,
            -base, 0, -base,      r, g, b,  leftNormal.x, leftNormal.y, leftNormal.z,
            -base, 0, base,       r, g, b,  leftNormal.x, leftNormal.y, leftNormal.z,
            // Bottom face
            -base, 0, -base,      r, g, b,  0, -1, 0,
            base, 0, -base,       r, g, b,  0, -1, 0,
            base, 0, base,        r, g, b,  0, -1, 0,
            -base, 0, base,       r, g, b,  0, -1, 0,
        };

        int[] indices = {
            0, 1, 2,
            3, 4, 5,
            6, 7, 8,
            9, 10, 11,
            12, 13, 14, 14, 15, 12
        };

        return new Mesh(vertices, indices);
    }

    public static Mesh createCylinder(Vector3f color, int segments) {
        float r = color.x, g = color.y, b = color.z;
        float radius = 0.5f;
        float height = 1.0f;

        List<Float> vertexList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();

        int vertexIndex = 0;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;

            float nx1 = (float) Math.cos(angle1);
            float nz1 = (float) Math.sin(angle1);
            float nx2 = (float) Math.cos(angle2);
            float nz2 = (float) Math.sin(angle2);

            addVertex(vertexList, x1, 0, z1, r, g, b, nx1, 0, nz1);
            addVertex(vertexList, x2, 0, z2, r, g, b, nx2, 0, nz2);
            addVertex(vertexList, x2, height, z2, r, g, b, nx2, 0, nz2);
            addVertex(vertexList, x1, height, z1, r, g, b, nx1, 0, nz1);

            indexList.add(vertexIndex);
            indexList.add(vertexIndex + 1);
            indexList.add(vertexIndex + 2);
            indexList.add(vertexIndex + 2);
            indexList.add(vertexIndex + 3);
            indexList.add(vertexIndex);
            vertexIndex += 4;
        }

        addVertex(vertexList, 0, height, 0, r, g, b, 0, 1, 0);
        int topCenter = vertexIndex++;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;

            addVertex(vertexList, x1, height, z1, r, g, b, 0, 1, 0);
            addVertex(vertexList, x2, height, z2, r, g, b, 0, 1, 0);

            indexList.add(topCenter);
            indexList.add(vertexIndex);
            indexList.add(vertexIndex + 1);
            vertexIndex += 2;
        }

        addVertex(vertexList, 0, 0, 0, r, g, b, 0, -1, 0);
        int bottomCenter = vertexIndex++;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;

            addVertex(vertexList, x2, 0, z2, r, g, b, 0, -1, 0);
            addVertex(vertexList, x1, 0, z1, r, g, b, 0, -1, 0);

            indexList.add(bottomCenter);
            indexList.add(vertexIndex);
            indexList.add(vertexIndex + 1);
            vertexIndex += 2;
        }

        return new Mesh(toFloatArray(vertexList), toIntArray(indexList));
    }

    public static Mesh createSphere(Vector3f color, int segments) {
        float r = color.x, g = color.y, b = color.z;
        float radius = 0.5f;

        List<Float> vertexList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();

        int rings = segments;
        int sectors = segments;

        for (int ring = 0; ring <= rings; ring++) {
            float phi = (float) (Math.PI * ring / rings);
            float y = (float) Math.cos(phi) * radius;
            float ringRadius = (float) Math.sin(phi) * radius;

            for (int sector = 0; sector <= sectors; sector++) {
                float theta = (float) (2 * Math.PI * sector / sectors);
                float x = (float) Math.cos(theta) * ringRadius;
                float z = (float) Math.sin(theta) * ringRadius;

                float nx = x / radius;
                float ny = y / radius;
                float nz = z / radius;

                addVertex(vertexList, x, y, z, r, g, b, nx, ny, nz);
            }
        }

        for (int ring = 0; ring < rings; ring++) {
            for (int sector = 0; sector < sectors; sector++) {
                int current = ring * (sectors + 1) + sector;
                int next = current + sectors + 1;

                indexList.add(current);
                indexList.add(next);
                indexList.add(current + 1);

                indexList.add(current + 1);
                indexList.add(next);
                indexList.add(next + 1);
            }
        }

        return new Mesh(toFloatArray(vertexList), toIntArray(indexList));
    }

    private static void addVertex(List<Float> list, float x, float y, float z,
                                   float r, float g, float b,
                                   float nx, float ny, float nz) {
        list.add(x);
        list.add(y);
        list.add(z);
        list.add(r);
        list.add(g);
        list.add(b);
        list.add(nx);
        list.add(ny);
        list.add(nz);
    }

    private static Vector3f calculateNormal(Vector3f v0, Vector3f v1, Vector3f v2) {
        Vector3f edge1 = new Vector3f(v1).sub(v0);
        Vector3f edge2 = new Vector3f(v2).sub(v0);
        return edge1.cross(edge2).normalize();
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
