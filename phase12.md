# Phase 12: Debugging Tools

## STEP 12: Building Developer‑Friendly Diagnostics

### 1. Goal
* Create a **logging system** with different severity levels (info, warning, error).
* Display an **in‑game debug overlay** showing FPS, entity count, memory usage, etc.
* Implement **debug drawing** – visualise collision boxes, raycasts, and vectors.
* Add **performance profiling** – measure time spent in each system.
* Provide **cheat commands** (e.g., god mode, spawn entities) via a console.

### 2. Concept

#### Why Debug Tools?
During development you need to see what the engine is doing internally. Console prints are not enough; you need real‑time visual feedback.

* **Logging** – persists to a file for post‑mortem analysis.
* **Overlay** – shows live metrics without interfering with gameplay.
* **Debug drawing** – renders shapes (lines, boxes, points) that are removed in release builds.
* **Profiling** – identifies performance bottlenecks.

#### Conditional Compilation
Debug features should be compiled out in release builds for performance. We can use a static `DEBUG` flag or rely on Java’s `assert` statements.

### 3. Implementation

#### Step 12.1 – Logging System
Create `src/main/java/engine/debug/Logger.java`:

```java
package engine.debug;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public enum Level { DEBUG, INFO, WARN, ERROR }
    
    private static Level minLevel = Level.DEBUG;
    private static PrintWriter fileWriter;
    
    static {
        try {
            fileWriter = new PrintWriter(new FileWriter("engine.log", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void setMinLevel(Level level) {
        minLevel = level;
    }
    
    private static void log(Level level, String message) {
        if (level.ordinal() < minLevel.ordinal()) return;
        
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy‑MM‑dd HH:mm:ss.SSS"));
        String line = String.format("[%s] [%s] %s", timestamp, level, message);
        
        // Console
        System.out.println(line);
        
        // File
        if (fileWriter != null) {
            fileWriter.println(line);
            fileWriter.flush();
        }
    }
    
    public static void debug(String message) { log(Level.DEBUG, message); }
    public static void info(String message)  { log(Level.INFO, message); }
    public static void warn(String message)  { log(Level.WARN, message); }
    public static void error(String message) { log(Level.ERROR, message); }
    
    public static void close() {
        if (fileWriter != null) fileWriter.close();
    }
}
```

Use it throughout the engine:

```java
Logger.info("Game loop started");
Logger.warn("Texture missing, using fallback");
Logger.error("Failed to create OpenAL context");
```

#### Step 12.2 – Debug Overlay
Create `src/main/java/engine/debug/DebugOverlay.java`:

```java
package engine.debug;

import static org.lwjgl.opengl.GL33.*;

import engine.graphics.Shader;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class DebugOverlay {
    private static final int MAX_LINES = 1000;
    private Shader shader;
    private int vao, vbo;
    private List<Float> lineVertices = new ArrayList<>();
    private List<String> textLines = new ArrayList<>();
    
    public void init() {
        shader = new Shader(
            "src/main/resources/shaders/debug.vert",
            "src/main/resources/shaders/debug.frag"
        );
        
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, MAX_LINES * 6 * 2 * Float.BYTES, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
    }
    
    /** Adds a line to be drawn this frame. */
    public void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b) {
        lineVertices.add(x1); lineVertices.add(y1);
        lineVertices.add(x2); lineVertices.add(y2);
        // Store color per line (simplified – we could use a separate buffer)
    }
    
    /** Adds a rectangle outline. */
    public void drawRect(float x, float y, float w, float h, float r, float g, float b) {
        drawLine(x, y, x + w, y, r, g, b);
        drawLine(x + w, y, x + w, y + h, r, g, b);
        drawLine(x + w, y + h, x, y + h, r, g, b);
        drawLine(x, y + h, x, y, r, g, b);
    }
    
    /** Adds text to the overlay (requires a font renderer – we’ll implement later). */
    public void drawText(String text, float x, float y) {
        textLines.add(text + "|" + x + "|" + y);
    }
    
    /** Renders all debug geometry and text. */
    public void render(Matrix4f projection) {
        if (lineVertices.isEmpty() && textLines.isEmpty()) return;
        
        // Draw lines
        float[] vertices = new float[lineVertices.size()];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = lineVertices.get(i);
        }
        
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        
        shader.use();
        shader.setUniform("uProjection", projection);
        shader.setUniform("uColor", 1.0f, 0.0f, 0.0f); // red lines
        
        glBindVertexArray(vao);
        glDrawArrays(GL_LINES, 0, vertices.length / 2);
        glBindVertexArray(0);
        
        // Draw text (placeholder)
        // In a real implementation we’d use a bitmap font and a separate renderer.
        
        // Clear for next frame
        lineVertices.clear();
        textLines.clear();
    }
    
    public void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        shader.cleanup();
    }
}
```

Create simple debug shaders (`debug.vert` and `debug.frag`) that just pass through positions and a uniform color.

#### Step 12.3 – Integrate Overlay with Main
In `Main.java`, add a static `DebugOverlay` instance and call its render method after the game’s render.

```java
private static DebugOverlay debugOverlay;

// In main():
debugOverlay = new DebugOverlay();
debugOverlay.init();

// In render(), after game rendering:
debugOverlay.drawText("FPS: " + frameCount, 10, 30);
debugOverlay.drawText("Entities: " + entityCount, 10, 50);
// Draw collision boxes of all entities
for (Entity e : entities) {
    debugOverlay.drawRect(e.x, e.y, e.width, e.height, 0, 1, 0);
}
debugOverlay.render(projectionMatrix);
```

#### Step 12.4 – Performance Profiling
Create a simple `Profiler` class that measures time between sections.

```java
package engine.debug;

import java.util.HashMap;
import java.util.Map;

public class Profiler {
    private static Map<String, Long> startTimes = new HashMap<>();
    private static Map<String, Long> accumulated = new HashMap<>();
    
    public static void start(String section) {
        startTimes.put(section, System.nanoTime());
    }
    
    public static void end(String section) {
        long end = System.nanoTime();
        long start = startTimes.getOrDefault(section, end);
        accumulated.merge(section, end - start, Long::sum);
    }
    
    public static void printResults() {
        accumulated.forEach((section, nanos) -> {
            double ms = nanos / 1_000_000.0;
            Logger.debug(String.format("%s: %.2f ms", section, ms));
        });
        accumulated.clear();
    }
}
```

Use it in game loop:

```java
Profiler.start("Physics");
physicsSystem.process(deltaTime);
Profiler.end("Physics");

Profiler.start("Rendering");
render();
Profiler.end("Rendering");

// Every second, print results
if (currentTime - lastProfileTime >= 1_000_000_000) {
    Profiler.printResults();
    lastProfileTime = currentTime;
}
```

#### Step 12.5 – Console Commands
Create a `Console` class that reads lines from standard input (or a dedicated GUI) and executes commands like “spawn enemy”, “godmode on”, “setgravity 0”.

This is optional but valuable for rapid testing.

### 4. Code
New files:
* `src/main/java/engine/debug/Logger.java`
* `src/main/java/engine/debug/DebugOverlay.java`
* `src/main/java/engine/debug/Profiler.java`
* `src/main/java/engine/debug/Console.java` (optional)
* `src/main/resources/shaders/debug.vert`
* `src/main/resources/shaders/debug.frag`

Modified files:
* `src/main/java/engine/Main.java`
* `src/main/java/engine/ecs/PhysicsSystem.java` (add profiling calls)
* `src/main/java/engine/graphics/SpriteBatch.java` (add profiling)

### 5. Output
* A log file `engine.log` is created in the working directory with timestamps and severity levels.
* The game window shows an overlay with FPS, entity count, and other stats.
* Collision boxes are drawn as green wireframes around entities.
* Every second the console prints time spent in each system (physics, rendering, etc.).
* The developer can type commands in the terminal to manipulate the game state.

### 6. Common Errors
* **Performance overhead** – Debug drawing and profiling add CPU/GPU cost. Ensure they can be disabled completely in release builds (e.g., with a compile‑time flag).
* **Z‑fighting** – Debug lines may be obscured by game geometry. Use a separate render pass with depth testing disabled or adjust polygon offset.
* **Log file permissions** – The game may not have write permission in the installation directory. Write logs to the user’s home directory instead.
* **Too many debug lines** – The `MAX_LINES` limit may be exceeded if you draw thousands of lines per frame. Either increase the limit or cull off‑screen lines.

---
**Next Step**: [Phase 13: Build a Complete Game](phase13.md) – we’ll use our engine to create a small but complete game (e.g., a block‑breaker or a simple platformer) and package it for distribution.
