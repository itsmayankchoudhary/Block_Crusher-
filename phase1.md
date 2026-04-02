# Phase 1: Scope Definition

## STEP 1: Deciding What Our Engine Will Be

### 1. Goal
* Choose the **language**, **graphics API**, and **platform** for our engine.
* Define a **minimal viable feature set** for version 1.
* Set clear boundaries – what we will and won’t implement now.

### 2. Concept

#### Why Start with 2D?
* **Lower complexity** – 2D rendering uses simple rectangles and sprites; no 3D math, cameras, or complex shaders.
* **Faster iteration** – We can see results in minutes, not hours.
* **Foundation for 3D** – Most 2D concepts (game loop, input, physics) translate directly to 3D.

#### Language Choice: Java
* **Why Java?**  
  * Excellent cross‑platform support (write once, run anywhere).
  * Strong object‑oriented design encourages modular, clean code.
  * Rich ecosystem – LWJGL provides low‑level OpenGL bindings.
  * Garbage collection reduces memory‑management headaches (for now).
* **Trade‑offs**  
  * Slightly slower than C++ for real‑time simulations, but still fast enough for a 2D engine.
  * Less control over memory layout – we’ll accept this for simplicity.

#### Graphics Library: LWJGL 3
* **LWJGL** (Lightweight Java Game Library) gives us:
  * **GLFW** – Window creation and input.
  * **OpenGL** – Hardware‑accelerated rendering.
  * **OpenAL** – Audio playback.
  * **STB** – Image loading (PNG, JPEG).
* **Why not a higher‑level library like libGDX?**  
  Because we want to **learn the low‑level details** – building from scratch teaches us what those higher‑level libraries abstract away.

#### Platform: Windows / Cross‑platform
* The JVM runs on Windows, macOS, and Linux.
* We’ll write platform‑independent Java code; LWJGL handles native windowing.

### 3. Implementation

We’ll define the **feature set** for our engine’s first version.

| System | What We’ll Implement | What We’ll Skip (for now) |
|--------|----------------------|---------------------------|
| **Windowing** | A single resizable window with OpenGL context. | Full‑screen switching, multi‑window support. |
| **Game Loop** | Fixed‑timestep loop with delta‑time and FPS capping. | Variable‑timestep, multi‑threaded updates. |
| **Rendering** | Draw colored rectangles, triangles, and textured sprites. | 3D meshes, lighting, shadows, post‑processing. |
| **Input** | Keyboard (WASD, arrows, space) and mouse (position, buttons). | Gamepad/joystick, touch gestures. |
| **ECS** | Basic Entity‑Component‑System: entities can have components, systems process them. | Advanced queries, archetypes, multi‑threaded systems. |
| **Physics** | Axis‑Aligned Bounding Box (AABB) collision detection, simple movement and gravity. | Rotated rectangles, convex polygons, friction, angular velocity. |
| **Asset Management** | Load PNG textures and WAV/MP3 sounds, cache them in a hash map. | Async loading, asset pipelines, compression. |
| **Scene Management** | Switch between scenes (menu, game, pause). | Scene graphs, serialization, level editors. |
| **Audio** | Play sound effects and background music via OpenAL. | 3D spatial audio, mixing, effects. |
| **Debugging** | On‑screen FPS counter, logging to console. | Profiler, memory inspector, visual debugger. |

#### Non‑Goals for V1
1. **3D graphics** – we stay strictly in 2D.
2. **Network multiplayer** – local single‑player only.
3. **Scripting** – game logic will be written in Java, not a scripting language.
4. **Particle systems** – can be added later as an extension.
5. **UI toolkit** – we’ll draw simple text and rectangles for UI.

#### Success Criteria
Our engine will be considered “complete” for V1 when we can:
1. Open a window and draw a moving colored square.
2. Control the square with keyboard keys.
3. Detect collisions between two squares.
4. Play a sound when a collision occurs.
5. Switch between a “menu” scene and a “game” scene.
6. Display an FPS counter in the corner.

### 4. Code
No implementation code yet – this is a planning step. However, here’s the **skeleton** of our future project structure:

```java
// This is just a preview – we’ll write the real files in Phase 2.
package engine;

public class Main {
    public static void main(String[] args) {
        System.out.println("Game Engine V1 – Scope Defined!");
    }
}
```

### 5. Output
* A clear, written specification of what our engine will do.
* Agreement on technology stack: **Java + LWJGL** for a **2D cross‑platform engine**.
* A checklist of systems to build in the coming phases.

### 6. Common Errors
* **Feature creep** – Adding “just one more” system before the basics work. Stick to the list above.
* **Over‑engineering** – Designing for hypothetical future needs. Build only what V1 requires.
* **Under‑estimating complexity** – Even a “simple” collision system can take hours to get right. Allocate time accordingly.

---
**Next Step**: [Phase 2: Project Setup](phase2.md) – where we create the Gradle project, set up LWJGL dependencies, and write our first “Hello Window” program.
