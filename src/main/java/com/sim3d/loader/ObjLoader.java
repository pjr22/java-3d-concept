package com.sim3d.loader;

import com.sim3d.graphics.Mesh;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ObjLoader {
    private static final Logger logger = LoggerFactory.getLogger(ObjLoader.class);

    private ObjLoader() {}

    public static Model load(String resourcePath) {
        logger.debug("Loading OBJ model from resource: {}", resourcePath);
        try (InputStream is = ObjLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                logger.warn("Resource not found: {}", resourcePath);
                return null;
            }
            return parseObj(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)), resourcePath);
        } catch (IOException e) {
            logger.warn("Failed to load OBJ resource: {}", resourcePath, e);
            return null;
        }
    }

    public static Model loadFromFile(String filePath) {
        logger.debug("Loading OBJ model from file: {}", filePath);
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            return parseObj(reader, filePath);
        } catch (IOException e) {
            logger.warn("Failed to load OBJ file: {}", filePath, e);
            return null;
        }
    }

    private static Model parseObj(BufferedReader reader, String sourcePath) throws IOException {
        List<Vector3f> positions = new ArrayList<>();
        List<float[]> texCoords = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();

        List<MeshData> meshDataList = new ArrayList<>();
        MeshData currentMesh = new MeshData("default");

        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] tokens = line.split("\\s+");
            if (tokens.length == 0) continue;

            try {
                switch (tokens[0]) {
                    case "v" -> {
                        if (tokens.length >= 4) {
                            positions.add(new Vector3f(
                                Float.parseFloat(tokens[1]),
                                Float.parseFloat(tokens[2]),
                                Float.parseFloat(tokens[3])
                            ));
                        }
                    }
                    case "vt" -> {
                        if (tokens.length >= 3) {
                            texCoords.add(new float[] {
                                Float.parseFloat(tokens[1]),
                                Float.parseFloat(tokens[2])
                            });
                        }
                    }
                    case "vn" -> {
                        if (tokens.length >= 4) {
                            normals.add(new Vector3f(
                                Float.parseFloat(tokens[1]),
                                Float.parseFloat(tokens[2]),
                                Float.parseFloat(tokens[3])
                            ).normalize());
                        }
                    }
                    case "f" -> parseFace(tokens, positions, normals, currentMesh, lineNumber);
                    case "o", "g" -> {
                        if (tokens.length >= 2) {
                            if (!currentMesh.faces.isEmpty()) {
                                meshDataList.add(currentMesh);
                            }
                            currentMesh = new MeshData(tokens[1]);
                            logger.debug("New object/group: {}", tokens[1]);
                        }
                    }
                    case "mtllib" -> {
                        if (tokens.length >= 2) {
                            logger.debug("Material library referenced (not yet supported): {}", tokens[1]);
                        }
                    }
                    case "usemtl" -> {
                        if (tokens.length >= 2) {
                            logger.debug("Material used (not yet supported): {}", tokens[1]);
                        }
                    }
                    case "s" -> {}
                    default -> logger.trace("Ignoring OBJ directive: {}", tokens[0]);
                }
            } catch (NumberFormatException e) {
                logger.warn("Parse error at line {}: {}", lineNumber, line);
            }
        }

        if (!currentMesh.faces.isEmpty()) {
            meshDataList.add(currentMesh);
        }

        if (meshDataList.isEmpty()) {
            logger.warn("No mesh data found in OBJ file: {}", sourcePath);
            return null;
        }

        List<Mesh> meshes = new ArrayList<>();
        Vector3f boundingMin = new Vector3f(Float.MAX_VALUE);
        Vector3f boundingMax = new Vector3f(-Float.MAX_VALUE);

        for (MeshData meshData : meshDataList) {
            float[] vertices = buildVertexArray(meshData, positions, normals, boundingMin, boundingMax);
            int[] indices = buildIndexArray(meshData.faces.size() * 3);
            meshes.add(new Mesh(vertices, indices));
        }

        String modelName = extractModelName(sourcePath);
        logger.info("Loaded OBJ model '{}' with {} mesh(es)", modelName, meshes.size());

        return new Model(modelName, meshes, boundingMin, boundingMax);
    }

    private static void parseFace(String[] tokens, List<Vector3f> positions, List<Vector3f> normals,
                                   MeshData meshData, int lineNumber) {
        if (tokens.length < 4) {
            logger.warn("Face with less than 3 vertices at line {}", lineNumber);
            return;
        }

        List<int[]> faceVertices = new ArrayList<>();
        for (int i = 1; i < tokens.length; i++) {
            int[] indices = parseFaceVertex(tokens[i], positions.size(), normals.size());
            if (indices != null) {
                faceVertices.add(indices);
            }
        }

        if (faceVertices.size() >= 3) {
            for (int i = 1; i < faceVertices.size() - 1; i++) {
                meshData.faces.add(new Face(
                    faceVertices.get(0),
                    faceVertices.get(i),
                    faceVertices.get(i + 1)
                ));
            }
        }
    }

    private static int[] parseFaceVertex(String token, int posCount, int normCount) {
        String[] parts = token.split("/", -1);
        int[] result = new int[] { -1, -1, -1 };

        try {
            if (parts.length >= 1 && !parts[0].isEmpty()) {
                result[0] = resolveIndex(Integer.parseInt(parts[0]), posCount);
            }
            if (parts.length >= 2 && !parts[1].isEmpty()) {
                result[1] = Integer.parseInt(parts[1]) - 1;
            }
            if (parts.length >= 3 && !parts[2].isEmpty()) {
                result[2] = resolveIndex(Integer.parseInt(parts[2]), normCount);
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return result;
    }

    private static int resolveIndex(int index, int size) {
        if (index < 0) {
            return size + index;
        }
        return index - 1;
    }

    private static float[] buildVertexArray(MeshData meshData, List<Vector3f> positions,
                                             List<Vector3f> normals, Vector3f boundingMin, Vector3f boundingMax) {
        List<Float> vertexList = new ArrayList<>();

        for (Face face : meshData.faces) {
            Vector3f faceNormal = null;
            if (face.v1[2] < 0 || face.v2[2] < 0 || face.v3[2] < 0) {
                faceNormal = calculateFaceNormal(
                    positions.get(face.v1[0]),
                    positions.get(face.v2[0]),
                    positions.get(face.v3[0])
                );
            }

            addVertex(vertexList, positions, normals, face.v1, faceNormal, boundingMin, boundingMax);
            addVertex(vertexList, positions, normals, face.v2, faceNormal, boundingMin, boundingMax);
            addVertex(vertexList, positions, normals, face.v3, faceNormal, boundingMin, boundingMax);
        }

        float[] result = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            result[i] = vertexList.get(i);
        }
        return result;
    }

    private static void addVertex(List<Float> vertexList, List<Vector3f> positions,
                                   List<Vector3f> normals, int[] indices, Vector3f faceNormal,
                                   Vector3f boundingMin, Vector3f boundingMax) {
        Vector3f pos = positions.get(indices[0]);
        boundingMin.min(pos);
        boundingMax.max(pos);

        vertexList.add(pos.x);
        vertexList.add(pos.y);
        vertexList.add(pos.z);

        vertexList.add(1.0f);
        vertexList.add(1.0f);
        vertexList.add(1.0f);

        Vector3f normal;
        if (indices[2] >= 0 && indices[2] < normals.size()) {
            normal = normals.get(indices[2]);
        } else if (faceNormal != null) {
            normal = faceNormal;
        } else {
            normal = new Vector3f(0, 1, 0);
        }

        vertexList.add(normal.x);
        vertexList.add(normal.y);
        vertexList.add(normal.z);
    }

    private static Vector3f calculateFaceNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        Vector3f edge1 = new Vector3f(v2).sub(v1);
        Vector3f edge2 = new Vector3f(v3).sub(v1);
        return edge1.cross(edge2).normalize();
    }

    private static int[] buildIndexArray(int count) {
        int[] indices = new int[count];
        for (int i = 0; i < count; i++) {
            indices[i] = i;
        }
        return indices;
    }

    private static String extractModelName(String path) {
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        String filename = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private static class MeshData {
        final String name;
        final List<Face> faces = new ArrayList<>();

        MeshData(String name) {
            this.name = name;
        }
    }

    private record Face(int[] v1, int[] v2, int[] v3) {}
}
