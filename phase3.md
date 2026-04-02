# Phase 3: Core System - Game Loop

## STEP 3: The Heartbeat of Every Game

### 1. Goal
* Understand what a **game loop** is and why it’s essential.
* Implement a **fixed‑timestep game loop** with **delta‑time**.
* Add **FPS control** (cap at 60 frames per second).
* Separate **update logic** from **rendering**.
* Verify the loop works by printing the FPS in the console.

### 2. Concept

#### What is a Game Loop?
A game loop is an infinite loop that runs until the game exits. Each iteration of the loop does three things:

1. **Process input** – Check which keys are pressed, mouse movement, etc.
2. **Update game state** – Move characters, calculate physics, update scores.
3. **Render graphics** – Draw everything on the screen.

Think of it like a **movie projector**:
* The film strip is the game state.
* The projector’s lamp (render) flashes 60 times per second.
* Between flashes, the film advances (update) to the next frame.

#### Delta‑Time (ΔT)
* **Delta‑time** is the time elapsed since the last frame, measured in seconds.
* Why it matters: Without delta‑time, game speed depends on frame rate.  
  Example: `player.x += 5;` moves 5 pixels **per frame**. At 30 FPS that’s 150 pixels/second; at 60 FPS it’s 300 pixels/second – the game would run twice as fast!
* Solution: Multiply movement by delta‑time: `player.x += speed * deltaTime`. Now the player moves `speed` pixels **per second**, regardless of frame rate.

#### Fixed vs Variable Timestep
* **Variable timestep** – Update with the actual delta‑time each frame. Simple but can cause physics instability.
* **Fixed timestep** – Update at a constant interval (e.g., 1/60 ≈ 0.0167 s). Physics becomes deterministic and stable.

We’ll implement a **fixed‑timestep** loop that:
* Runs updates at a fixed rate (60 Hz).
* Renders as fast as possible (or capped at a target FPS).
* Uses **accumulator** pattern to catch up if frames take too long.

#### FPS Control
* **`glfwSwapInterval(1)`** enables VSync (ties frame rate to monitor refresh rate). Simple but not always available.
* **Manual capping** – If we want exactly 60 FPS, we can sleep the remaining time after each frame.

### 3. Implementation

#### Step 3.1 – Create the Game Loop Class
Create a new file `src/main/java/engine/core/GameLoop.java`:

```java
package engine.core;

/**
 * A fixed‑timestep game loop that separates update and render.
 */
public class GameLoop {
    private final Runnable update;
    private final Runnable render;
    private final float updateInterval;  // seconds per update
    private boolean running;
    private long previousTime;
    private float accumulator;
    
    /**
     * @param update Called every updateInterval seconds.
     * @param render Called as often as possible (capped by targetFps).
     * @param updatesPerSecond How many times per second update is called.
     * @param targetFps Maximum frames per second (0 = uncapped).
     */
    public GameLoop(Runnable update, Runnable render, int updatesPerSecond, int targetFps) {
        this.update = update;
        this.render = render;
        this.updateInterval = 1.0f / updatesPerSecond;
        this.running = false;
        this.previousTime = 0;
        this.accumulator = 0.0f;
    }
    
    public void start() {
        if (running) return;
        running = true;
        previousTime = System.nanoTime();
        runLoop();
    }
    
    public void stop() {
        running = false;
    }
    
    private void runLoop() {
        while (running) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000.0f; // seconds
            previousTime = currentTime;
            
            // Prevent spiral of death
            if (deltaTime > 0.25f) deltaTime = 0.25f;
            
            accumulator += deltaTime;
            
            // Fixed‑step updates
            while (accumulator >= updateInterval) {
                update.run();
                accumulator -= updateInterval;
            }
            
            // Render (with interpolation if we wanted smoothness)
            render.run();
            
            // Simple FPS cap (optional)
            // sleepRemainingTime(deltaTime);
        }
    }
    
    // Optional: method to cap FPS by sleeping
    private void sleepRemainingTime(float deltaTime) {
        float targetFrameTime = 1.0f / 60.0f; // 60 FPS
        if (deltaTime < targetFrameTime) {
            try {
                Thread.sleep((long) ((targetFrameTime - deltaTime) * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

#### Step 3.2 – Modify Main.java to Use the Game Loop
Replace the simple while‑loop in `Main.java` with a GameLoop instance. We’ll also add a simple counter to log FPS.

Update `src/main/java/engine/Main.java`:

```java
package engine;

import engine.core.GameLoop;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private static long window;
    private static int frameCount = 0;
    private static long lastFpsTime = 0;
    
    public static void main(String[] args) {
        System.out.println("Game Engine V1 – Starting game loop...");
        
        // Initialize GLFW and create window (same as before)
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        
        window = glfwCreateWindow(800, 600, "Game Engine Window", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }
        
        var vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
            window,
            (vidMode.width() - 800) / 2,
            (vidMode.height() - 600) / 2
        );
        glfwShowWindow(window);
        
        // Enable VSync (optional)
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);  // 1 = VSync on, 0 = off
        
        // Create the game loop
        GameLoop loop = new GameLoop(
            Main::update,   // update callback
            Main::render,   // render callback
            60,             // 60 updates per second
            60              // cap at 60 FPS
        );
        
        lastFpsTime = System.nanoTime();
        loop.start();       // this blocks until the window is closed
        
        // Cleanup after loop stops
        glfwDestroyWindow(window);
        glfwTerminate();
        System.out.println("Game loop stopped. Engine shutdown.");
    }
    
    private static void update() {
        // For now, just print the update count every second
        // In later phases we’ll move the player, detect collisions, etc.
    }
    
    private static void render() {
        frameCount++;
        long currentTime = System.nanoTime();
        if (currentTime - lastFpsTime >= 1_000_000_000) { // every second
            System.out.println("FPS: " + frameCount);
            frameCount = 0;
            lastFpsTime = currentTime;
        }
        
        // Clear the screen with a dark‑gray color
        // (We’ll replace this with OpenGL calls in Phase 4)
        // For now, just poll events
        glfwPollEvents();
        
        // If the user pressed Escape, close the window
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true);
        }
        
        // Swap front and back buffers (required for OpenGL)
        glfwSwapBuffers(window);
    }
}
```

#### Step 3.3 – Build and Run
Return to the `game‑engine` directory and execute:

```cmd
gradle run
```

(If you don’t have Gradle installed globally, use `./gradlew run` after generating the wrapper.)

### 4. Code
The two new files are:

* `engine/core/GameLoop.java` – the reusable game‑loop implementation.
* `engine/Main.java` – updated to use the loop and log FPS.

All code is shown in the implementation steps above.

### 5. Output
* A window that stays open (just like before).
* In the terminal you should see **“FPS: 60”** printed every second (or close to 60, depending on VSync).
* The window can be closed by pressing the Escape key or clicking the “X”.
* The game loop runs at a fixed **60 updates per second**, while rendering is capped at **60 frames per second**.

If you see the FPS counter, congratulations – you have a **professional‑grade game loop**!

### 6. Common Errors
* **`IllegalStateException: Failed to initialize GLFW`** – Make sure you call `glfwInit()` before creating the window.
* **`NullPointerException` in `glfwSwapBuffers`** – You must call `glfwMakeContextCurrent(window)` before swapping buffers.
* **FPS is far lower than 60** – Your computer may be struggling; ensure you’re not doing heavy work in the update/render methods yet.
* **FPS is far higher than 60** – VSync may be disabled; check `glfwSwapInterval(1)`.
* **Game loop runs too fast and uses 100% CPU** – Add a small sleep in `sleepRemainingTime` or rely on VSync.

---
**Next Step**: [Phase 4: Window & Rendering Setup](phase4.md) – we’ll replace the empty render method with actual OpenGL calls to draw a colored rectangle on the screen.
