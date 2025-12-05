# Java 3D Simulation Engine

A cross-platform 3D simulation/game engine implemented in Java 21, featuring JSON-defined worlds, interconnected environments, and real-time rendering.

## Table of Contents

- [Requirements](#requirements)
- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Core Concepts](#core-concepts)
- [Configuration](#configuration)
- [JSON Configuration](#json-configuration)
- [Building and Running](#building-and-running)
- [Controls](#controls)
- [Platform Support](#platform-support)

## Requirements

### Functional Requirements

1. **World Model**: A container for multiple interconnected 3D environments
2. **3D Environments**: Support for both outdoor and indoor environments
3. **Environment Transitions**: Seamless navigation between environments via portals/gateways
4. **Object System**: Support for various object types:
   - **Actors**: Intelligent entities (creatures, NPCs) with autonomous behavior
   - **Static Objects**: Natural features (trees, rocks, flowers)
   - **Interactive Objects**: Tools (shovel, key, fishing rod)
   - **Containers**: Storage objects (chest, crate, suitcase)
5. **Player/Observer**: First-person camera with keyboard/mouse controls
6. **JSON Configuration**: World and environment definitions loaded from JSON files
7. **Menu System**: ESC key triggers pause menu with exit option

### Technical Requirements

- **Language**: Java 21
- **Build System**: Gradle with Groovy DSL
- **Graphics**: LWJGL 3 (Lightweight Java Game Library) with OpenGL
- **Platforms**: Windows 11, macOS, Linux, Raspberry Pi (x64 and arm64)

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application Layer                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │   Engine    │  │   Input     │  │      Menu System        │  │
│  │  (Game Loop)│  │  Handler    │  │                         │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                        Rendering Layer                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │  Renderer   │  │   Camera    │  │    Shader Manager       │  │
│  │             │  │             │  │                         │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                         Model Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │    World    │  │ Environment │  │    GameObject           │  │
│  │             │  │  (Indoor/   │  │  (Actor/Static/         │  │
│  │             │  │   Outdoor)  │  │   Container)            │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                        Data Layer                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │ JSON Loader │  │   Mesh      │  │    Resource Manager     │  │
│  │             │  │   Loader    │  │                         │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Component Descriptions

| Component | Description |
|-----------|-------------|
| **Engine** | Main game loop handling update/render cycles at 60 FPS |
| **Input Handler** | Processes keyboard/mouse input for player movement and actions |
| **Menu System** | Overlay UI for pause menu and game options |
| **Renderer** | OpenGL-based 3D rendering pipeline |
| **Camera** | First-person camera with position, rotation, and projection |
| **Shader Manager** | Compiles and manages GLSL shaders |
| **World** | Root container for all environments |
| **Environment** | 3D space containing objects, with bounds and portals |
| **GameObject** | Base class for all renderable entities |
| **JSON Loader** | Parses world/environment configuration files |
| **Resource Manager** | Caches textures, meshes, and other assets |

## Project Structure

```
java_3d_concept/
├── build.gradle                    # Gradle build configuration
├── settings.gradle                 # Gradle settings
├── README.md                       # This file
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── sim3d/
│       │           ├── Main.java                    # Application entry point
│       │           ├── engine/
│       │           │   ├── Engine.java              # Main game loop
│       │           │   ├── Settings.java            # Application settings management
│       │           │   ├── Window.java              # GLFW window management
│       │           │   └── Timer.java               # Frame timing
│       │           ├── graphics/
│       │           │   ├── Renderer.java            # OpenGL rendering
│       │           │   ├── Camera.java              # First-person camera
│       │           │   ├── ShaderProgram.java       # GLSL shader handling
│       │           │   ├── Mesh.java                # 3D mesh data
│       │           │   └── primitives/
│       │           │       ├── Cube.java            # Cube mesh generator
│       │           │       ├── Plane.java           # Plane mesh generator
│       │           │       └── Sphere.java          # Sphere mesh generator
│       │           ├── input/
│       │           │   ├── InputHandler.java        # Keyboard/mouse input
│       │           │   └── MouseInput.java          # Mouse movement tracking
│       │           ├── model/
│       │           │   ├── World.java               # World container
│       │           │   ├── Environment.java         # Base environment class
│       │           │   ├── OutdoorEnvironment.java  # Outdoor specialization
│       │           │   ├── IndoorEnvironment.java   # Indoor specialization
│       │           │   ├── Portal.java              # Environment transition
│       │           │   ├── GameObject.java          # Base object class
│       │           │   ├── Actor.java               # Intelligent entities
│       │           │   ├── StaticObject.java        # Inanimate objects
│       │           │   ├── Container.java           # Storage objects
│       │           │   ├── Player.java              # Player/observer
│       │           │   └── Transform.java           # Position/rotation/scale
│       │           ├── loader/
│       │           │   ├── WorldLoader.java         # JSON world parser
│       │           │   ├── EnvironmentLoader.java   # JSON environment parser
│       │           │   ├── ObjLoader.java           # Wavefront OBJ model loader
│       │           │   ├── Model.java               # Loaded 3D model container
│       │           │   └── AssetManager.java        # Model caching and management
│       │           └── ui/
│       │               ├── MenuSystem.java          # Menu management
│       │               └── PauseMenu.java           # ESC pause menu
│       └── resources/
│           ├── settings.json                      # Application configuration
│           ├── logback.xml                       # Logging configuration
│           ├── shaders/
│           │   ├── vertex.glsl                      # Vertex shader
│           │   └── fragment.glsl                    # Fragment shader
│           ├── models/                              # 3D model assets
│           │   ├── rabbit.obj                       # Rabbit model
│           │   ├── tree.obj                         # Tree model
│           │   ├── rock.obj                         # Rock model
│           │   ├── cabin.obj                        # Cabin model
│           │   ├── table.obj                        # Table model
│           │   └── chair.obj                        # Chair model
│           └── worlds/
│               ├── demo_world.json                  # Demo world definition
│               ├── outdoor_forest.json              # Outdoor environment
│               └── indoor_cabin.json                # Indoor environment
└── docs/
    └── json-schema.md                               # JSON schema documentation
```

## Configuration

The application uses two main configuration files:

### Application Settings (settings.json)

The `settings.json` file contains application-wide configuration that can be customized by users:

```json
{
  "window": {
    "fullscreen": false,
    "width": 1280,
    "height": 720
  },
  "logLevel": "info"
}
```

#### Settings Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `window.fullscreen` | boolean | false | Start application in fullscreen mode |
| `window.width` | integer | 1280 | Window width in pixels (when not fullscreen) |
| `window.height` | integer | 720 | Window height in pixels (when not fullscreen) |
| `logLevel` | string | "info" | Logging level: "trace", "debug", "info", "warn", "error" |

#### Logging Levels

- **trace**: Most verbose logging, includes all trace messages
- **debug**: Debug information useful for development (includes debug messages)
- **info**: General information about application operation (default)
- **warn**: Warning messages for potential issues
- **error**: Error messages only

The application first loads default settings from `src/main/resources/settings.json`, then overrides them with user settings from the root `settings.json` file if it exists.

### Logging Configuration (logback.xml)

The `logback.xml` file configures the logging framework. It uses the `log.level` property from settings to control the logging level for the `com.sim3d` package:

```xml
<logger name="com.sim3d" level="${log.level:-INFO}" />
```

This allows dynamic configuration of logging levels without modifying the logging configuration file.

## Core Concepts

### World

The `World` is the top-level container that holds all environments. It manages:
- Registry of all environments
- The currently active environment
- Environment transitions via portals
- Global game state

### Environments

Environments are 3D spaces where gameplay occurs:

| Type                   | Description                     | Characteristics                          |
|------------------------|---------------------------------|------------------------------------------|
| **OutdoorEnvironment** | Open areas like forests, fields | Sky rendering, terrain, natural lighting |
| **IndoorEnvironment**  | Enclosed spaces like buildings  | Walls, ceiling, artificial lighting      |

### Game Objects

All entities in an environment inherit from `GameObject`:

```
GameObject (abstract)
├── Actor (abstract)
│   ├── Creature       # Animals, monsters
│   └── NPC            # Non-player characters
├── StaticObject
│   ├── NaturalFeature # Trees, rocks, flowers
│   └── Tool           # Shovel, key, fishing rod
└── Container
    ├── Chest
    ├── Crate
    └── StorageBin
```

### Portals

Portals connect environments. When the player enters a portal's trigger zone, they transition to the linked environment at the specified spawn point.

## JSON Configuration

### World Definition (demo_world.json)

```json
{
  "id": "demo_world",
  "name": "Demo World",
  "startEnvironment": "outdoor_forest",
  "environments": [
    "outdoor_forest.json",
    "indoor_cabin.json"
  ]
}
```

### Environment Definition

```json
{
  "id": "outdoor_forest",
  "type": "outdoor",
  "name": "Forest Clearing",
  "bounds": {
    "width": 100.0,
    "height": 50.0,
    "depth": 100.0
  },
  "spawnPoint": { "x": 0.0, "y": 1.0, "z": 0.0 },
  "objects": [...],
  "portals": [...]
}
```

### Object Definition

```json
{
  "id": "tree_01",
  "type": "static",
  "subtype": "natural_feature",
  "name": "Oak Tree",
  "model": "pyramid",
  "modelPath": "models/tree.obj",
  "transform": {
    "position": { "x": 10.0, "y": 0.0, "z": 5.0 },
    "rotation": { "x": 0.0, "y": 45.0, "z": 0.0 },
    "scale": { "x": 1.0, "y": 1.0, "z": 1.0 }
  },
  "color": { "r": 0.2, "g": 0.6, "b": 0.2 }
}
```

### Object Fields

| Field | Required | Description |
|-------|----------|-------------|
| `id` | Yes | Unique identifier for the object |
| `type` | Yes | Object type: `static`, `actor`, or `container` |
| `subtype` | No | Category: `natural_feature`, `furniture`, `creature`, etc. |
| `name` | No | Display name |
| `model` | No | Fallback primitive: `cube`, `sphere`, `pyramid`, `cylinder` |
| `modelPath` | No | Path to OBJ model file (e.g., `models/tree.obj`) |
| `transform` | No | Position, rotation, and scale |
| `color` | No | RGB color applied to the model |

## 3D Models

### Supported Formats

The engine currently supports **Wavefront OBJ** format (`.obj` files).

### Model Loading

Models are specified in JSON using the `modelPath` field:

```json
{
  "id": "rabbit_01",
  "modelPath": "models/rabbit.obj",
  "model": "sphere",
  "color": { "r": 0.8, "g": 0.7, "b": 0.6 }
}
```

- If `modelPath` is specified and the file exists, the OBJ model is loaded
- If loading fails, the engine falls back to the primitive specified in `model`
- The `color` field applies a uniform color to the entire model

### Creating Custom Models

1. Create your model in any 3D software (Blender, Maya, etc.)
2. Export as Wavefront OBJ format
3. Place the `.obj` file in `src/main/resources/models/`
4. Reference it in JSON: `"modelPath": "models/your_model.obj"`

### OBJ Format Support

The OBJ loader supports:
- Vertex positions (`v x y z`)
- Vertex normals (`vn x y z`)
- Texture coordinates (`vt u v`) - parsed but not yet rendered
- Triangular and polygon faces (`f v1//vn1 v2//vn2 v3//vn3`)
- Multiple objects/groups (`o name`, `g name`)
- Negative indices (relative to end of list)

### Included Models

| Model | File | Description |
|-------|------|-------------|
| Rabbit | `models/rabbit.obj` | Low-poly rabbit with body, head, ears, tail |
| Tree | `models/tree.obj` | Pine tree with trunk and foliage cones |
| Rock | `models/rock.obj` | Irregular boulder shape |
| Cabin | `models/cabin.obj` | Simple house with walls and roof |
| Table | `models/table.obj` | Rectangular table with legs |
| Chair | `models/chair.obj` | Chair with seat, back, and legs |

## Building and Running

### Prerequisites

- Java 21 JDK
- Gradle 8.x (or use included wrapper)

### Build Commands

```bash
# Build the project
./gradlew build

# Run the application
./gradlew run

# Create distribution
./gradlew distZip
```

### Running from JAR

```bash
java -jar build/libs/java_3d_concept.jar
```

## Controls

| Input | Action |
|-------|--------|
| **W** | Move forward |
| **S** | Move backward |
| **A** | Strafe left |
| **D** | Strafe right |
| **Space** | Move up (fly mode) |
| **Left Shift** | Move down (fly mode) |
| **Mouse Move** | Look around |
| **ESC** | Toggle pause menu |
| **Enter** | Confirm menu selection |
| **Arrow Keys** | Navigate menu |

## Platform Support

| Platform | Architecture | Status |
|----------|--------------|--------|
| Windows 11 | x64 | ✅ Supported |
| Windows 11 | arm64 | ✅ Supported |
| macOS | x64 | ✅ Supported |
| macOS | arm64 (Apple Silicon) | ✅ Supported |
| Linux | x64 | ✅ Supported |
| Linux | arm64 | ✅ Supported |
| Raspberry Pi OS | arm64 | ✅ Supported |

LWJGL 3 provides native binaries for all supported platforms. The Gradle build automatically includes the appropriate natives based on the runtime platform.

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| LWJGL | 3.3.3 | OpenGL, GLFW, STB bindings |
| JOML | 1.10.5 | Math library (vectors, matrices) |
| Gson | 2.10.1 | JSON parsing |
| SLF4J + Logback | 2.0.9 | Logging |

## Future Enhancements

- [ ] Texture loading and mapping
- [ ] Collision detection
- [ ] Actor AI and pathfinding
- [ ] Sound system
- [ ] Save/load game state
- [ ] Multiplayer support
- [ ] Physics engine integration

## License

MIT License - See LICENSE file for details.
