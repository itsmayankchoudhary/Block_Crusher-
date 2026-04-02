# Phase 13: Build a Complete Game

## STEP 13: Putting Everything Together – “Block Breaker”

### 1. Goal
* Use our engine to build a fully playable **2D block‑breaker** game (like Breakout/Arkanoid).
* Implement **paddle movement**, **ball physics**, **brick collision**, **scoring**, and **lives**.
* Add **game states** (start, play, game over) using the scene manager.
* Package the game into a standalone **executable JAR** that can be distributed.
* Demonstrate that the engine is production‑ready.

### 2. Concept

#### Game Design
* **Paddle** – moves horizontally with arrow keys, reflects the ball.
* **Ball** – moves with constant speed, bounces off walls, paddle, and bricks.
* **Bricks** – arranged in a grid; each hit destroys the brick and increments the score.
* **Lives** – start with 3 lives; lose a life when the ball falls below the paddle.
* **Win condition** – destroy all bricks.

#### Engine Features We’ll Use
* **ECS** – each game object is an entity with components (Transform, Velocity, Collision, Render).
* **PhysicsSystem** – handles movement and AABB collisions.
* **InputSystem** – reads arrow keys to move the paddle.
* **SceneManager** – manages title screen, gameplay, and game‑over screen.
* **AssetManager** – loads textures for paddle, ball, bricks.
* **AudioManager** – plays bounce sounds and background music.
* **DebugOverlay** – optional, shows hitboxes and FPS.

### 3. Implementation

#### Step 13.1 – Define Game Components
Create new component classes in `engine.ecs`:

**`BallComponent`** – marks the ball entity (optional, for quick lookup).

```java
package engine.ecs;

public class BallComponent implements Component { }
```

**`PaddleComponent`** – marks the paddle.

```java
package engine.ecs;

public class PaddleComponent implements Component { }
```

**`BrickComponent`** – holds brick strength (1‑hit kill) and score value.

```java
package engine.ecs;

public class BrickComponent implements Component {
    public int strength = 1;
    public int score = 100;
}
```

#### Step 13.2 – Create the Game Scene
Extend `GameScene` (from Phase 10) to set up the block‑breaker world.

In `src/main/java/engine/scenes/BlockBreakerScene.java`:

```java
package engine.scenes;

import engine.ecs.*;
import engine.graphics.SpriteBatch;
import engine.graphics.Texture;
import engine.assets.AssetManager;

public class BlockBreakerScene extends GameScene {
    private int ballEntity, paddleEntity;
    private int[] brickEntities;
    private int score = 0;
    private int lives = 3;
    
    @Override
    public void onEnter() {
        super.onEnter(); // initializes entityManager, physicsSystem, spriteBatch
        
        AssetManager assets = new AssetManager();
        Texture paddleTex = assets.loadTexture("paddle", "resources/paddle.png").getTexture();
        Texture ballTex   = assets.loadTexture("ball",   "resources/ball.png").getTexture();
        Texture brickTex  = assets.loadTexture("brick",  "resources/brick.png").getTexture();
        
        // Create paddle
        paddleEntity = entityManager.createEntity();
        entityManager.addComponent(paddleEntity, new TransformComponent(0.0f, -0.8f));
        entityManager.addComponent(paddleEntity, new VelocityComponent());
        entityManager.addComponent(paddleEntity, new CollisionComponent(0.1f, 0.03f));
        entityManager.addComponent(paddleEntity, new PaddleComponent());
        
        // Create ball
        ballEntity = entityManager.createEntity();
        entityManager.addComponent(ballEntity, new TransformComponent(0.0f, -0.5f));
        VelocityComponent ballVel = new VelocityComponent();
        ballVel.vx = 0.01f;
        ballVel.vy = 0.01f;
        entityManager.addComponent(ballEntity, ballVel);
        entityManager.addComponent(ballEntity, new CollisionComponent(0.03f, 0.03f));
        entityManager.addComponent(ballEntity, new BallComponent());
        
        // Create bricks (3 rows, 10 columns)
        brickEntities = new int[30];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 10; col++) {
                int brick = entityManager.createEntity();
                entityManager.addComponent(brick, new TransformComponent(
                    -0.9f + col * 0.18f,  // x
                      0.5f - row * 0.1f   // y
                ));
                entityManager.addComponent(brick, new CollisionComponent(0.08f, 0.04f));
                entityManager.addComponent(brick, new BrickComponent());
                brickEntities[row * 10 + col] = brick;
            }
        }
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime); // runs physics system
        
        // Move paddle based on input
        VelocityComponent paddleVel = entityManager.getComponent(paddleEntity, VelocityComponent.class);
        paddleVel.vx = 0.0f;
        if (Input.isLeftPressed())  paddleVel.vx = -0.02f;
        if (Input.isRightPressed()) paddleVel.vx =  0.02f;
        
        // Keep paddle within screen bounds
        TransformComponent paddlePos = entityManager.getComponent(paddleEntity, TransformComponent.class);
        paddlePos.x = Math.max(-0.9f, Math.min(0.9f, paddlePos.x));
        
        // Check for ball out of bounds (below paddle)
        TransformComponent ballPos = entityManager.getComponent(ballEntity, TransformComponent.class);
        if (ballPos.y < -1.0f) {
            lives--;
            resetBall();
            if (lives <= 0) {
                // Game over
                sceneManager.replace(new GameOverScene(score));
            }
        }
        
        // Check win condition
        boolean allBricksGone = true;
        for (int brick : brickEntities) {
            if (entityManager.hasComponent(brick, BrickComponent.class)) {
                allBricksGone = false;
                break;
            }
        }
        if (allBricksGone) {
            sceneManager.replace(new VictoryScene(score));
        }
    }
    
    private void resetBall() {
        TransformComponent ballPos = entityManager.getComponent(ballEntity, TransformComponent.class);
        ballPos.x = 0.0f;
        ballPos.y = -0.5f;
        VelocityComponent ballVel = entityManager.getComponent(ballEntity, VelocityComponent.class);
        ballVel.vx = 0.01f;
        ballVel.vy = 0.01f;
    }
    
    @Override
    public void render() {
        spriteBatch.begin();
        // Draw paddle, ball, bricks using their textures
        // (implementation depends on your SpriteBatch API)
        spriteBatch.end();
        
        // Draw score and lives via debug overlay or a proper UI system
        debugOverlay.drawText("Score: " + score, 10, 30);
        debugOverlay.drawText("Lives: " + lives, 10, 50);
    }
}
```

#### Step 13.3 – Custom Collision Responses
We need to modify the `PhysicsSystem` to handle special collisions:
* **Ball‑paddle** – reflect the ball’s Y velocity and adjust X velocity based on where the ball hit the paddle (to add directional control).
* **Ball‑brick** – destroy the brick, increase score, and reflect the ball.

Add a `BlockBreakerPhysicsSystem` that extends `PhysicsSystem` and overrides `resolveCollision`.

#### Step 13.4 – Add Sound Effects
Load and play sounds for:
* **Ball‑paddle hit** – a soft “blip”.
* **Ball‑brick hit** – a “click” or “pop”.
* **Life lost** – a sad tone.
* **Background music** – a looping chiptune.

#### Step 13.5 – Create Title and Game‑Over Scenes
Use the scene manager to switch between:
* `TitleScene` – shows “BLOCK BREAKER” and “Press SPACE to start”.
* `BlockBreakerScene` – the actual game.
* `GameOverScene` – displays final score and “Press R to restart”.
* `VictoryScene` – “You Win!”.

#### Step 13.6 – Package as Executable JAR
Update `build.gradle.kts` to create a fat JAR with all dependencies.

Add the following to `game-engine/build.gradle.kts`:

```kotlin
tasks.jar {
    manifest {
        attributes["Main-Class"] = "engine.Main"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
```

Then run `./gradlew jar` (or `gradlew.bat jar` on Windows). The JAR will be generated in `build/libs/` and can be run with:

```bash
java -jar game‑engine‑1.0‑SNAPSHOT.jar
```

### 4. Code
New files:
* `src/main/java/engine/ecs/BallComponent.java`
* `src/main/java/engine/ecs/PaddleComponent.java`
* `src/main/java/engine/ecs/BrickComponent.java`
* `src/main/java/engine/scenes/BlockBreakerScene.java`
* `src/main/java/engine/scenes/TitleScene.java`
* `src/main/java/engine/scenes/GameOverScene.java`
* `src/main/java/engine/scenes/VictoryScene.java`
* `src/main/java/engine/systems/BlockBreakerPhysicsSystem.java`

Modified files:
* `build.gradle.kts` – add JAR packaging.

### 5. Output
* A fully playable block‑breaker game with graphics, sound, and score tracking.
* The game starts with a title screen, transitions to gameplay, and ends with a win/loss screen.
* The executable JAR runs on any machine with Java 17+ installed.
* The game performs smoothly at 60 FPS with all engine features (ECS, physics, audio, debugging) integrated.

### 6. Common Errors
* **Ball gets stuck** – The collision resolution may push the ball inside another object, causing infinite bounces. Add a small epsilon or ensure the ball’s velocity is reversed correctly.
* **JAR too large** – The fat JAR includes all LWJGL native libraries for every platform. Use **platform‑specific packaging** (e.g., create separate JARs for Windows, Linux, macOS) or rely on the user installing LWJGL separately.
* **Missing resources** – Ensure texture and sound files are copied into the JAR (they should be placed in `src/main/resources` and referenced via `Class.getResource()`).
* **Game balance** – The ball speed, paddle size, and brick layout determine difficulty. Tweak these values until the game feels fun.

---
### 🎉 CONGRATULATIONS!
You have built a complete 2D game engine from scratch and used it to create a real game. The engine is modular, performant, and ready to be extended with 3D graphics, networking, or advanced AI.

### Next Steps (Beyond the Tutorial)
* **3D Rendering** – replace OpenGL 2D with a 3D pipeline (camera, lighting, models).
* **Networking** – add multiplayer support with a client‑server architecture.
* **Scripting** – embed Lua or JavaScript to allow modding.
* **Tooling** – build a level editor, particle editor, or animation editor.
* **Optimization** – implement spatial partitioning, occlusion culling, and GPU instancing.

The foundation you’ve laid is solid – now go and make your dream game!
