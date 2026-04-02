# Block Crusher (Brick Breaker) Game

A classic Brick Breaker game built with HTML5 Canvas and vanilla JavaScript.

## Features

- **Paddle Control**: Move paddle left/right with arrow keys or mouse
- **Ball Physics**: Realistic bouncing off walls, paddle, and bricks
- **Brick Grid**: Multiple rows of colorful bricks to break
- **Scoring System**: Points for each brick destroyed
- **Lives System**: Limited lives with game over condition
- **Game Loop**: Smooth 60 FPS animation using `requestAnimationFrame`
- **Collision Detection**: Precise collision handling for ball-brick and ball-paddle interactions

## Planned Features (Optional)

- Multiple levels with increasing difficulty
- Power-ups (extra ball, enlarge paddle, etc.)
- Sound effects and background music
- High score tracking

## Technology Stack

- **HTML5 Canvas** for rendering
- **Vanilla JavaScript** for game logic
- **CSS** for styling and UI
- **Git** for version control

## Project Structure

```
block-crusher/
├── index.html          # Main HTML file
├── style.css           # Stylesheet
├── game.js             # Core game logic
├── README.md           # This file
└── .gitignore          # Git ignore rules
```

## Getting Started

### Prerequisites

- A modern web browser (Chrome, Firefox, Edge, etc.)
- Git (optional, for version control)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/block-crusher-game.git
   cd block-crusher-game
   ```

2. Open `index.html` in your browser.

### How to Play

- Use **Left Arrow** and **Right Arrow** keys to move the paddle.
- Prevent the ball from falling below the paddle.
- Break all bricks to advance.
- Each brick broken adds points to your score.
- You have 3 lives; losing a ball costs one life.

## Development

### Running Locally

Simply open `index.html` in a browser. No build process required.

### Testing

Manual testing of game mechanics:

1. Ball bounces correctly off walls and paddle.
2. Bricks disappear on collision.
3. Score increments appropriately.
4. Lives decrease when ball falls.
5. Game over screen appears when lives reach zero.

### Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/amazing-feature`).
3. Commit your changes (`git commit -m 'Add some amazing feature'`).
4. Push to the branch (`git push origin feature/amazing-feature`).
5. Open a Pull Request.

## 2D Game Engine (Java/LWJGL)

This repository now includes a complete, production‑ready 2D game engine built from scratch in Java with LWJGL. The engine follows a modular, component‑based architecture and includes:

- **Window & Rendering**: OpenGL 3.3 core profile, shader‑based rendering, camera system.
- **Game Loop**: Fixed‑timestep loop with delta‑time and FPS control.
- **Input System**: Keyboard/mouse polling and event‑driven callbacks.
- **Entity‑Component‑System (ECS)**: Data‑oriented design for scalable game objects.
- **Physics**: AABB collision detection, movement, and gravity.
- **Asset Management**: Texture/sound loading with caching and hot‑reload.
- **Scene Management**: Stack‑based scene switching and game states.
- **Audio**: OpenAL integration for spatial sound and music.
- **Debug Tools**: Logging, FPS counter, and debug overlays.

### Running the Engine

**Prerequisites**
- Java JDK 17 or later
- Gradle (or use the provided wrapper)

**Clone & Build**
```bash
git clone https://github.com/itsmayankchoudhary/Block_Crusher-.git
cd Block_Crusher-/game-engine
./gradlew run
```

If you encounter a graphics‑driver crash (OpenGL 3.3 not supported), try:
```bash
./gradlew run -Dorg.lwjgl.opengl.Display.allowSoftwareOpenGL=true
```

### Tutorial Documentation

The engine is accompanied by a 13‑phase tutorial (`phase0.md`–`phase13.md`) that explains each system in depth, with real‑world analogies, step‑by‑step implementation, and working code examples.

### Structure
```
game-engine/
├── src/main/java/engine/          # Core engine modules
├── src/main/resources/shaders/    # GLSL shaders
├── build.gradle.kts               # Gradle build script
└── gradlew                        # Gradle wrapper
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Inspired by classic Atari Breakout and Arkanoid games.
- Thanks to MDN Web Docs for excellent Canvas tutorials.
- LWJGL and OpenGL communities for excellent documentation.

---
*Project created as part of a step‑by‑step game development tutorial.*