# Texture Best Practices

This guide explains how to correctly create and apply textures to 3D models in this engine.

## Quick Start

To add a textured object, specify both `modelPath` and `texturePath` in your JSON configuration:

```json
{
  "id": "my_object",
  "modelPath": "assets/mymodel/model_triangulated.obj",
  "texturePath": "assets/mymodel/texture.png",
  "color": { "r": 1.0, "g": 1.0, "b": 1.0 }
}
```

## OBJ File Requirements

Your OBJ file **must** include:

| Element | Syntax | Description |
|---------|--------|-------------|
| Vertices | `v x y z` | 3D vertex positions |
| Texture coordinates | `vt u v` | UV coordinates in [0,1] range |
| Normals | `vn x y z` | Vertex normals for lighting |
| Faces with UV indices | `f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3` | Triangular faces referencing all three |

### Use Triangulated Meshes

**Critical:** Always use triangulated (tessellated) meshes, not:
- Control meshes (low-poly base meshes for subdivision surfaces)
- Quad meshes (may work but triangulated is more reliable)
- N-gon faces (polygons with more than 4 vertices)

If your model appears "chunky" or low-poly, you're likely using a control mesh instead of the triangulated version.

### Example: Spot Cow Model Files

| File | Purpose | Use? |
|------|---------|------|
| `spot_control_mesh.obj` | Low-poly Catmull-Clark control mesh | ❌ **DO NOT USE** |
| `spot_triangulated.obj` | Fully tessellated mesh with ~5000 triangles | ✅ **Use this** |
| `spot_quadrangulated.obj` | Quad version (not triangulated) | ⚠️ Works but not preferred |
| `spot_texture.png` | UV texture map | ✅ **Use this** |

## Exporting from Blender

1. Select your model
2. **File → Export → Wavefront (.obj)**
3. Enable these export options:
   - ✅ **Include UVs** (critical!)
   - ✅ **Triangulate Faces**
   - ✅ **Write Normals**
4. Ensure UV coordinates are in the `[0,1]` range

### UV Mapping Tips

- Unwrap your model's UVs before exporting
- Ensure no overlapping UVs unless intentional
- Keep UV islands within the 0-1 texture space
- Use UV editor to verify mapping before export

## Texture Image Requirements

| Requirement | Details |
|-------------|---------|
| **Format** | PNG (preferred) or JPG |
| **Dimensions** | Power of 2 recommended: 256×256, 512×512, 1024×1024, 2048×2048 |
| **Layout** | UV-unwrapped layout matching your model's UVs |
| **Color space** | sRGB |

### Why Power-of-2 Dimensions?

- Better GPU compatibility
- Automatic mipmap generation works correctly
- Optimal memory usage
- Required by some older hardware

## JSON Configuration

### Basic Textured Object

```json
{
  "id": "cow_01",
  "type": "actor",
  "subtype": "creature",
  "name": "Spotted Cow",
  "model": "sphere",
  "modelPath": "assets/spot/spot_triangulated.obj",
  "texturePath": "assets/spot/spot_texture.png",
  "transform": {
    "position": { "x": 8, "y": 0.5, "z": 12 },
    "scale": { "x": 0.5, "y": 0.5, "z": 0.5 }
  },
  "color": { "r": 1.0, "g": 1.0, "b": 1.0 }
}
```

### Color Tinting

The `color` field multiplies with the texture:
- `{ "r": 1.0, "g": 1.0, "b": 1.0 }` — No tint, original texture colors
- `{ "r": 1.0, "g": 0.8, "b": 0.8 }` — Slight red tint
- `{ "r": 0.5, "g": 0.5, "b": 0.5 }` — Darkened texture

## Common Problems and Solutions

### Model Appears Chunky/Low-Poly

**Cause:** Using a control mesh instead of triangulated mesh.

**Solution:** Use the triangulated or tessellated version of your model (e.g., `spot_triangulated.obj` instead of `spot_control_mesh.obj`).

### Texture Not Appearing (Solid Color Only)

**Cause:** OBJ file missing texture coordinates or face format incorrect.

**Solution:** 
- Ensure OBJ has `vt` lines with UV coordinates
- Ensure faces use format `f v/vt/vn` (not `f v//vn`)
- Re-export from 3D software with "Include UVs" enabled

### Texture Appears Stretched or Distorted

**Cause:** Poor UV mapping or UV coordinates outside [0,1] range.

**Solution:**
- Review UV layout in your 3D software
- Ensure UVs are within 0-1 bounds
- Re-unwrap problem areas

### Texture Appears Inside-Out or Flipped

**Cause:** Normals facing wrong direction or V coordinate needs flipping.

**Solution:**
- Recalculate normals in your 3D software (facing outward)
- The engine automatically flips textures vertically for OpenGL compatibility

### Memory Error When Loading Large Models

**Cause:** Very high-poly models with many vertices.

**Solution:** The engine uses heap allocation for large meshes. If you still encounter issues, consider:
- Reducing polygon count
- Splitting into multiple smaller meshes
- Increasing JVM heap size with `-Xmx` flag

## File Organization

Recommended structure for textured assets:

```
src/main/resources/
├── assets/
│   └── mymodel/
│       ├── model_triangulated.obj
│       └── texture.png
├── models/
│   └── (simple untextured models)
└── worlds/
    └── my_world.json
```

## Supported Texture Formats

| Format | Extension | Notes |
|--------|-----------|-------|
| PNG | `.png` | Preferred; supports transparency |
| JPEG | `.jpg`, `.jpeg` | Good for photos; no transparency |
| SVG | `.svg` | Limited support; creates placeholder texture |

## Performance Tips

1. **Use appropriate texture sizes** — Don't use 4096×4096 for a small object
2. **Reuse textures** — Multiple objects can share the same texture file
3. **Texture atlases** — Combine multiple small textures into one larger texture
4. **Mipmap filtering** — Enabled by default for better quality at distance
