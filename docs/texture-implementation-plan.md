# Texture Implementation Plan

## Overview

This document outlines the implementation plan for adding texture support to the Java 3D simulation engine. The goal is to enable 3D models to use texture images (PNG, JPG) instead of or in addition to solid colors.

## Current State Analysis

### Existing Foundation
- **LWJGL 3.3.3** with STB library for image loading (already included in build.gradle)
- **OBJ loader** that parses texture coordinates but doesn't use them (line 79 in `ObjLoader.java`)
- **Shader system** with vertex and fragment shaders that currently only use solid colors
- **Asset management** system for caching models
- **JSON-based configuration** for world and object definitions

### Current Vertex Data Structure
- Position (3 floats) + Color (3 floats) + Normal (3 floats) = 9 floats per vertex
- Texture coordinates are parsed from OBJ files but discarded

## Implementation Strategy

### 1. Core Texture Classes

#### Texture Class
- Manages OpenGL texture objects
- Handles texture binding and cleanup
- Stores texture ID and dimensions

#### TextureLoader Class
- Uses STB library to load PNG/JPG files
- Handles SVG conversion (pre-process or runtime)
- Provides error handling and fallbacks

### 2. Graphics Pipeline Updates

#### Mesh Class Updates
- Add texture coordinate attribute (layout location 3)
- Update vertex data structure: Position (3) + Color (3) + Normal (3) + TexCoord (2) = 11 floats
- Modify VAO setup to include texture coordinates

#### Shader Updates
- **Vertex shader**: Pass through texture coordinates to fragment shader
- **Fragment shader**: Sample texture and mix with object color
- **New uniforms**: `sampler2D textureSampler`, `bool useTexture`

#### Renderer Updates
- Bind textures before rendering meshes
- Set shader uniforms for texture sampling
- Handle texture enable/disable states

### 3. Data Model Updates

#### GameObject Class
- Add `texturePath` field
- Add `hasTexture()` method
- Update constructor and setters

#### Model Class
- Add texture reference support
- Store texture path or texture object reference

#### AssetManager Class
- Extend to cache textures alongside models
- Add `getTexture()` and `loadTexture()` methods
- Implement texture cleanup

### 4. File Format Support

#### Supported Formats
- **Primary**: PNG, JPG (direct STB support)
- **Secondary**: SVG (convert to PNG at runtime or pre-process)

#### OBJ File Integration
- Already parse texture coordinates (`vt` lines)
- Need to pass UV coordinates to mesh building
- Handle missing texture coordinates gracefully

## Implementation Steps

### Phase 1: Core Infrastructure
1. **Create Texture class** - OpenGL texture management
2. **Create TextureLoader class** - STB-based image loading
3. **Update AssetManager** - Texture caching support

### Phase 2: Graphics Pipeline
4. **Update Mesh class** - Add texture coordinate support
5. **Update shaders** - Texture sampling in fragment shader
6. **Update Renderer** - Texture binding and rendering

### Phase 3: Data Integration
7. **Update GameObject class** - Add texture path field
8. **Update Model class** - Texture reference support
9. **Update ObjLoader** - Use parsed texture coordinates

### Phase 4: Configuration & Testing
10. **Update JSON schema** - Add texturePath to object definitions
11. **Update EnvironmentLoader** - Parse texture paths from JSON
12. **Test with cow assets in assets/spot** - Verify implementation works
13. **Add fallback mechanisms** - Handle texture loading failures

## Technical Details

### Vertex Data Structure
```
Before: [x, y, z, r, g, b, nx, ny, nz] (9 floats)
After:  [x, y, z, r, g, b, nx, ny, nz, u, v] (11 floats)
```

### Shader Uniforms
```glsl
uniform sampler2D textureSampler;
uniform bool useTexture;
uniform vec3 objectColor;
```

### Fragment Shader Logic
```glsl
vec4 texColor = useTexture ? texture(textureSampler, fragTexCoord) : vec4(1.0);
vec3 finalColor = mix(objectColor, texColor.rgb, texColor.a);
```

### JSON Configuration
```json
{
  "id": "cow_01",
  "type": "actor",
  "subtype": "creature",
  "name": "Spotted Cow",
  "modelPath": "assets/spot/spot_triangulated.obj",
  "texturePath": "assets/spot/spot_texture.png",
  "color": { "r": 1.0, "g": 1.0, "b": 1.0 },
  "transform": {
    "position": { "x": 0, "y": 0, "z": 0 },
    "scale": { "x": 1, "y": 1, "z": 1 }
  }
}
```

## Fallback Strategy

1. **Texture Loading Failure**: Use solid color instead
2. **Missing Texture Path**: Use solid color only
3. **Missing Texture Coordinates**: Generate default UV mapping
4. **Unsupported Format**: Log warning and use solid color

## Benefits

1. **Backward Compatibility**: Existing objects without textures continue to work
2. **Performance**: Texture caching in AssetManager prevents redundant loading
3. **Flexibility**: Mix textures with colors for tinting effects
4. **Extensibility**: Foundation for advanced texture features (normal maps, etc.)

## Testing Plan

1. **Unit Tests**: Texture loading, mesh creation with UVs
2. **Integration Tests**: Full rendering pipeline with textures
3. **Manual Tests**: Visual verification with assets/spot model
4. **Performance Tests**: Memory usage and rendering performance

## Future Enhancements

- Normal mapping
- Specular mapping
- Texture atlases
- Animated textures
- Material properties (roughness, metallic, etc.)