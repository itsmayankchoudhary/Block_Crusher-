# Phase 10: Scene Management

## STEP 10: Organizing the Game into States

### 1. Goal
* Implement a **scene manager** that can switch between different game states (menu, level, pause, game over).
* Each scene has its own **update**, **render**, and **event handling**.
* Support **scene stacking** (e.g., pause menu on top of the game level).
* Add smooth **transitions** (fade‑in, fade‑out) between scenes.
* Keep the architecture clean and decoupled.

### 2. Concept

#### What is a Scene?
A scene is a self‑contained unit of gameplay logic and rendering. Examples:
* **Main menu** – buttons, background, music.
* **Game level** – player, enemies, physics, HUD.
* **Pause screen** – dimmed background with menu options.
* **Credits screen** – scrolling text.

#### Scene Manager Responsibilities
* Hold a **stack** of scenes (the topmost scene is the active one).
* Forward **update** and **render** calls to the active scene(s).
* Handle **scene transitions** (push, pop, replace).
* Manage scene **lifecycle** (enter, exit, pause, resume).

#### Scene Stack vs Single Scene
* **Stack** – allows overlays (e.g., pause menu on top of game). The scene below may be paused or still updated (depending on design).
* **Single** – simpler, only one scene active at a time.

We’ll implement a stack because it’s more flexible.

### 3. Implementation

#### Step 10.1 – Define the Scene Interface
Create `src/main/java/engine/scenes/Scene.java`:

```java
package engine.scenes;

public interface Scene {
    /** Called when the scene becomes the top‑most scene. */
    void onEnter();
    
    /** Called when the scene is no longer the top‑most scene (but may still be in the stack). */
    void onPause();
    
    /** Called when the scene becomes the top‑most scene again after being paused. */
    void onResume();
    
    /** Called when the scene is removed from the stack (cleanup). */
    void onExit();
    
    /** Update the scene logic. */
    void update(float deltaTime);
    
    /** Render the scene. */
    void render();
    
    /** Handle input events (optional). */
    void processInput();
}
```

#### Step 10.2 – Implement the Scene Manager
Create `src/main/java/engine/scenes/SceneManager.java`:

```java
package engine.scenes;

import java.util.Stack;

public class SceneManager {
    private Stack<Scene> sceneStack = new Stack<>();
    
    public void push(Scene scene) {
        if (!sceneStack.isEmpty()) {
            sceneStack.peek().onPause();
        }
        sceneStack.push(scene);
        scene.onEnter();
    }
    
    public void pop() {
        if (sceneStack.isEmpty()) return;
        Scene oldScene = sceneStack.pop();
        oldScene.onExit();
        if (!sceneStack.isEmpty()) {
            sceneStack.peek().onResume();
        }
    }
    
    public void replace(Scene scene) {
        pop();
        push(scene);
    }
    
    public Scene peek() {
        return sceneStack.isEmpty() ? null : sceneStack.peek();
    }
    
    public void update(float deltaTime) {
        if (!sceneStack.isEmpty()) {
            sceneStack.peek().update(deltaTime);
        }
    }
    
    public void render() {
        // Render from bottom to top? Usually only the top scene is visible,
        // but we can render all scenes for overlays.
        for (Scene scene : sceneStack) {
            scene.render();
        }
    }
    
    public void processInput() {
        if (!sceneStack.isEmpty()) {
            sceneStack.peek().processInput();
        }
    }
}
```

#### Step 10.3 – Create Concrete Scenes
**MainMenuScene** – a simple menu with a background and a “Start Game” button.

Create `src/main/java/engine/scenes/MainMenuScene.java`:

```java
package engine.scenes;

import engine.Input;
import static org.lwjgl.glfw.GLFW.*;

public class MainMenuScene implements Scene {
    private boolean startRequested = false;
    
    @Override public void onEnter() {
        System.out.println("Main menu entered.");
    }
    
    @Override public void onPause() {}
    @Override public void onResume() {}
    @Override public void onExit() {}
    
    @Override public void update(float deltaTime) {
        if (startRequested) {
            // In a real game we’d ask the scene manager to switch to the game scene
        }
    }
    
    @Override public void render() {
        // Draw a background, title, and a “Press SPACE to start” text
        // (We’ll implement proper UI rendering later)
    }
    
    @Override public void processInput() {
        if (Input.isSpacePressed()) {
            startRequested = true;
        }
    }
}
```

**GameScene** – the actual gameplay scene that uses our ECS and physics.

Create `src/main/java/engine/scenes/GameScene.java`:

```java
package engine.scenes;

import engine.ecs.*;
import engine.graphics.SpriteBatch;

public class GameScene implements Scene {
    private EntityManager entityManager;
    private PhysicsSystem physicsSystem;
    private SpriteBatch spriteBatch;
    
    @Override public void onEnter() {
        System.out.println("Game scene entered.");
        entityManager = new EntityManager();
        physicsSystem = new PhysicsSystem();
        spriteBatch = new SpriteBatch();
        spriteBatch.init(800, 600);
        
        // Create player entity etc.
    }
    
    @Override public void onPause() {
        System.out.println("Game paused.");
    }
    
    @Override public void onResume() {
        System.out.println("Game resumed.");
    }
    
    @Override public void onExit() {
        System.out.println("Game scene exited.");
        // Cleanup entities, textures, etc.
    }
    
    @Override public void update(float deltaTime) {
        physicsSystem.process(entityManager, deltaTime);
        // other systems
    }
    
    @Override public void render() {
        spriteBatch.begin();
        // Draw entities
        spriteBatch.end();
    }
    
    @Override public void processInput() {
        // Handle player input
    }
}
```

#### Step 10.4 – Integrate Scene Manager into Main
Replace the direct game loop with scene‑manager‑driven updates.

In `Main.java`:

```java
private static SceneManager sceneManager;

// In main():
sceneManager = new SceneManager();
sceneManager.push(new MainMenuScene());

// In update():
sceneManager.update(1.0f / 60.0f);
sceneManager.processInput();

// In render():
sceneManager.render();
```

#### Step 10.5 – Scene Transitions (Optional)
Add a transition effect (e.g., fade to black) between scenes. Create a `TransitionScene` that sits on top of the stack and renders a semi‑transparent overlay while performing an animation.

Example: `FadeTransition` that fades out the current scene, swaps the underlying scene, then fades in.

#### Step 10.6 – Pause Menu as an Overlay
Demonstrate scene stacking by pushing a `PauseScene` onto the stack while the `GameScene` is still underneath (but paused). The pause scene can be popped to resume the game.

### 4. Code
New files:
* `src/main/java/engine/scenes/Scene.java`
* `src/main/java/engine/scenes/SceneManager.java`
* `src/main/java/engine/scenes/MainMenuScene.java`
* `src/main/java/engine/scenes/GameScene.java`
* `src/main/java/engine/scenes/PauseScene.java` (optional)

Modified files:
* `src/main/java/engine/Main.java`

### 5. Output
* The game starts with a main menu screen.
* Pressing SPACE switches to the game scene (with a simple transition if implemented).
* The game scene runs the physics and renders entities.
* Pressing ESC pushes a pause scene on top; the game scene is paused but still visible in the background.
* The pause menu can be closed (pop) to resume the game.

### 6. Common Errors
* **Memory leak** – Scenes that are popped must release their resources in `onExit()`. Watch out for textures, sounds, and OpenGL objects.
* **Input propagation** – If you have multiple scenes in the stack, decide whether input should be processed by all scenes or only the top one. Usually only the top scene receives input.
* **Rendering order** – Overlay scenes should be rendered after the base scene, but with transparency. Ensure proper blending is enabled.
* **Delta‑time during pause** – When the game is paused, `update()` should not advance simulation. The scene manager can skip calling `update` on paused scenes (those below the top). Our simple implementation updates only the top scene; that’s fine.

---
**Next Step**: [Phase 11: Audio System](phase11.md) – we’ll add sound effects and background music using OpenAL.
