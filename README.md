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

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Inspired by classic Atari Breakout and Arkanoid games.
- Thanks to MDN Web Docs for excellent Canvas tutorials.

---
*Project created as part of a step‑by‑step game development tutorial.*