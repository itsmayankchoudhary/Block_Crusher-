# Block Crusher (Brick Breaker) - Requirements Specification

## PHASE 1: Requirement Analysis

### 1. Game Objective
The player controls a paddle at the bottom of the screen, bouncing a ball to break a grid of bricks at the top. The goal is to destroy all bricks without letting the ball fall below the paddle. Each broken brick scores points. The player has a limited number of lives; losing a ball deducts one life. The game ends when all bricks are cleared (win) or when lives reach zero (lose).

### 2. Core Features

#### 2.1 Paddle
- **Movement**: Horizontal movement controlled by keyboard (left/right arrow keys) and/or mouse.
- **Boundaries**: Paddle cannot move beyond the left/right edges of the game area.
- **Visual**: Rectangular shape with configurable width, height, and color.
- **Collision**: Ball bounces off the paddle with angle variation based on where it hits.

#### 2.2 Ball
- **Movement**: Constant speed with a direction vector (dx, dy). Moves independently after launch.
- **Launch**: Initially attached to the paddle; launched with spacebar or mouse click.
- **Bouncing**: Reflects off walls (left, right, top) and the paddle. Bottom wall results in ball loss (life deduction).
- **Physics**: Simple linear motion; no gravity or friction.

#### 2.3 Bricks
- **Grid Layout**: Multiple rows and columns of rectangular bricks.
- **Attributes**: Each brick has a position, size, color, and hit points (default: 1 hit to destroy).
- **Destruction**: When hit by the ball, the brick disappears (or reduces hit points). Player earns points.
- **Rendering**: Distinct colors per row for visual appeal.

#### 2.4 Scoring System
- **Points**: Each brick awards a fixed number of points (e.g., 10). Different brick types may award different points.
- **Display**: Current score shown on screen, updated in real‑time.
- **High Score**: Optional persistence across sessions (local storage).

#### 2.5 Lives System
- **Initial Lives**: 3 lives.
- **Life Loss**: Ball passes below the paddle → lose one life, reset ball position to paddle.
- **Game Over**: When lives reach zero, display “Game Over” screen with final score.
- **Life Display**: Visual indicator (e.g., heart icons or number) on screen.

#### 2.6 Game States
- **START**: Welcome screen with instructions and a “Start” button.
- **PLAYING**: Active gameplay.
- **PAUSE**: Pause game with “Pause” button or key (P).
- **GAME_OVER**: Show final score and “Restart” button.
- **WIN**: Show congratulations message when all bricks cleared.

### 3. Optional Features (Stretch Goals)

#### 3.1 Levels System
- Multiple levels with increasing difficulty (more bricks, faster ball, smaller paddle).
- Level progression after clearing all bricks.
- Level design defined via configuration (JSON or arrays).

#### 3.2 Power‑Ups
- Special bricks drop power‑ups that fall downward; catching them with paddle activates effect.
- Examples:
  - **Extra Ball**: Adds another ball to play.
  - **Enlarge Paddle**: Increases paddle width for a limited time.
  - **Slow Ball**: Reduces ball speed temporarily.
  - **Sticky Paddle**: Ball sticks to paddle until released.

#### 3.3 Sound Effects & Music
- Background music loop.
- Sound effects for collisions, brick break, life loss, power‑up collection, etc.
- Mute/unmute toggle.

#### 3.4 Visual Enhancements
- Particle effects when a brick breaks.
- Screen shake on powerful hits.
- Smooth animations for paddle and ball.

### 4. Target Platform
- **Primary**: Web browsers (desktop and mobile).
- **Technology**: HTML5 Canvas + Vanilla JavaScript (ES6+).
- **No external libraries** (to keep the project beginner‑friendly and lightweight).
- **Responsive**: Canvas scales to fit different screen sizes while maintaining aspect ratio.

### 5. Language & Framework
- **HTML5**: Structure and canvas element.
- **CSS3**: Styling for UI elements (score panel, buttons, etc.).
- **JavaScript (ES6+)**: Game logic, rendering, input handling.
- **No game engines** (e.g., Phaser, Three.js) to demonstrate fundamental concepts.

### 6. Functional Requirements
1. The game shall render a paddle that moves horizontally via keyboard and mouse input.
2. The game shall render a ball that moves continuously and bounces off walls, the paddle, and bricks.
3. The game shall display a grid of bricks that disappear when hit by the ball.
4. The game shall update and display the player’s score in real‑time.
5. The game shall track and display the player’s remaining lives.
6. The game shall detect collision between the ball and bricks/paddle/walls accurately.
7. The game shall provide a start screen, pause functionality, and game‑over screen.
8. The game shall run at a consistent frame rate (60 FPS) using `requestAnimationFrame`.

### 7. Non‑Functional Requirements
1. **Performance**: Smooth animation with no visible lag or frame drops on modern browsers.
2. **Usability**: Intuitive controls (keyboard/mouse) and clear visual feedback.
3. **Maintainability**: Code shall be modular, well‑commented, and follow clean coding practices.
4. **Cross‑browser compatibility**: Works on Chrome, Firefox, Edge, Safari (latest versions).
5. **Responsiveness**: Adapts to different screen sizes (desktop, tablet, mobile) without breaking gameplay.
6. **Accessibility**: Basic keyboard navigation and screen‑reader friendly text for UI elements.

### 8. Constraints
- No external libraries (except for optional sound library if needed).
- Code must be beginner‑friendly with explanatory comments.
- All assets (images, sounds) must be either generated programmatically or freely licensed.

---

**Next Step**: PHASE 2 – System Design (breakdown of components, architecture diagram, module responsibilities).