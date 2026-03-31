# Block Crusher (Brick Breaker) - System Design

## PHASE 2: System Design

### 1. Component Breakdown

The game is structured into six core modules, each with a distinct responsibility.

#### 1.1 Game Engine
- **Responsibility**: Orchestrates the overall game flow, manages state transitions (START, PLAYING, PAUSE, etc.), and coordinates between modules.
- **Key Functions**:
  - `init()` – Initialize all subsystems.
  - `start()` – Begin gameplay.
  - `pause()` / `resume()` – Toggle pause state.
  - `gameOver()` – Handle end‑of‑game logic.
  - `reset()` – Reset game to initial state.

#### 1.2 Rendering Module
- **Responsibility**: Draws all visual elements on the HTML5 Canvas.
- **Key Functions**:
  - `clearCanvas()` – Clear the canvas before each frame.
  - `drawPaddle(paddle)` – Render the paddle.
  - `drawBall(ball)` – Render the ball.
  - `drawBricks(brickGrid)` – Render all bricks.
  - `drawUI(score, lives)` – Display score, lives, and other UI elements.
  - `drawText()` – Draw text messages (e.g., “Game Over”, “Level Complete”).

#### 1.3 Input Handler
- **Responsibility**: Captures user input (keyboard, mouse, touch) and translates it into game actions.
- **Key Functions**:
  - `bindEvents()` – Attach event listeners.
  - `handleKeyDown(e)`, `handleKeyUp(e)` – Process keyboard input.
  - `handleMouseMove(e)` – Update paddle position based on mouse.
  - `handleClick(e)` – Launch ball, click buttons.
  - `getPaddleDirection()` – Return current movement direction (-1, 0, +1).

#### 1.4 Physics Engine
- **Responsibility**: Updates the positions of moving objects (ball) and applies basic motion laws.
- **Key Functions**:
  - `updateBall(ball, deltaTime)` – Move ball according to its velocity.
  - `applyWallCollision(ball, canvas)` – Bounce ball off top, left, and right walls.
  - `checkBottomBoundary(ball, canvas)` – Detect ball loss.
  - `calculateReflection(normal)` – Compute new direction after collision.

#### 1.5 Collision Detection
- **Responsibility**: Detects intersections between the ball and other game objects (paddle, bricks).
- **Key Functions**:
  - `ballVsPaddle(ball, paddle)` – Determine if ball hits paddle and return hit position.
  - `ballVsBrick(ball, brick)` – Check collision with a single brick.
  - `ballVsBrickGrid(ball, brickGrid)` – Iterate through brick grid and find collisions.
  - `resolveCollision(type, object)` – Adjust ball velocity and trigger side effects (brick break, score increment).

#### 1.6 Score & Game State Manager
- **Responsibility**: Tracks score, lives, level, and other meta‑data.
- **Key Functions**:
  - `addPoints(points)` – Increase score.
  - `loseLife()` – Decrement lives, check game over.
  - `nextLevel()` – Advance to next level, reset ball and paddle, generate new bricks.
  - `saveHighScore()` – Store high score in `localStorage`.
  - `getGameState()` – Return current state (score, lives, level).

### 2. Game Loop Design

The game uses a **fixed‑timestep game loop** inside `requestAnimationFrame` for smooth, consistent updates.

```
function gameLoop(timestamp) {
  // Calculate time delta
  delta = timestamp - lastTime;
  lastTime = timestamp;

  // Update game state
  update(delta);

  // Render everything
  render();

  // Schedule next frame
  requestAnimationFrame(gameLoop);
}
```

#### 2.1 Update Phase (`update(delta)`)
- Move paddle according to input.
- Update ball position using physics.
- Detect collisions and resolve them.
- Update score, lives, and check win/lose conditions.
- Handle power‑up movement and effects.

#### 2.2 Render Phase (`render()`)
- Clear canvas.
- Draw background (if any).
- Draw bricks, paddle, ball.
- Draw UI (score, lives, level).
- Draw any transient effects (particles, messages).

### 3. Data Structures

#### 3.1 Ball Object
```javascript
const ball = {
  x: canvas.width / 2,
  y: canvas.height - 30,
  radius: 8,
  dx: 5,          // velocity in x direction (pixels per frame)
  dy: -5,         // velocity in y direction
  speed: 5,       // magnitude of velocity (optional)
  color: '#ff6600'
};
```

#### 3.2 Paddle Object
```javascript
const paddle = {
  x: canvas.width / 2 - 50,
  y: canvas.height - 20,
  width: 100,
  height: 15,
  speed: 8,
  color: '#0095dd',
  movingLeft: false,
  movingRight: false
};
```

#### 3.3 Brick Grid
Represented as a 2D array of brick objects:
```javascript
const brickRowCount = 5;
const brickColumnCount = 9;
const bricks = [];

for (let c = 0; c < brickColumnCount; c++) {
  bricks[c] = [];
  for (let r = 0; r < brickRowCount; r++) {
    bricks[c][r] = {
      x: c * (brickWidth + brickPadding) + brickOffsetLeft,
      y: r * (brickHeight + brickPadding) + brickOffsetTop,
      width: brickWidth,
      height: brickHeight,
      color: '#0095dd',
      visible: true,
      points: 10
    };
  }
}
```

#### 3.4 Game State Object
```javascript
const gameState = {
  score: 0,
  lives: 3,
  level: 1,
  isPaused: false,
  isGameOver: false,
  isWin: false,
  ballLaunched: false
};
```

### 4. Architecture Diagram (Text‑Based)

```
┌─────────────────────────────────────────────────────────────┐
│                    HTML Document (index.html)               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                     Canvas Element                    │  │
│  └──────────────────────────────────────────────────────┘  │
│                              │                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                  JavaScript (game.js)                 │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │  │
│  │  │ Game Engine │  │  Rendering  │  │   Input     │  │  │
│  │  │             │◀─▶│   Module    │◀─▶│  Handler    │  │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │  │
│  │         │                │                │          │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │  │
│  │  │  Physics    │  │  Collision  │  │ Score &     │  │  │
│  │  │   Engine    │◀─▶│ Detection   │◀─▶│ Game State  │  │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                              │                              │
│                    CSS (style.css)                         │
└─────────────────────────────────────────────────────────────┘
```

**Data Flow**:
1. User input (keyboard/mouse) → Input Handler → Game Engine.
2. Game Engine updates Physics Engine and Collision Detection.
3. Physics Engine updates ball position; Collision Detection detects hits.
4. Score & Game State Manager updates score/lives.
5. Rendering Module reads current state and draws everything on Canvas.
6. Loop repeats at 60 FPS.

### 5. Module Responsibilities Summary

| Module | Responsibility | Key Data | Dependencies |
|--------|----------------|----------|--------------|
| **Game Engine** | Overall control, state transitions | `gameState` | All other modules |
| **Rendering Module** | Visual representation | Canvas context, game objects | Game Engine, Game State |
| **Input Handler** | Capture user input | Keyboard/mouse events | Game Engine, Paddle |
| **Physics Engine** | Movement and basic physics | Ball, Paddle | Game Engine, Collision Detection |
| **Collision Detection** | Detect and resolve collisions | Ball, Paddle, Bricks | Physics Engine, Game State |
| **Score & Game State** | Track score, lives, level | `gameState` | Game Engine, Rendering Module |

### 6. Next Steps

- **PHASE 3**: Project Setup – Create folder structure, `index.html`, `style.css`, `game.js` skeleton.
- **PHASE 4**: Core Game Mechanics – Implement paddle, ball, bricks, collision, and scoring step‑by‑step.

---
*This design ensures separation of concerns, making the codebase modular, testable, and easy to extend.*