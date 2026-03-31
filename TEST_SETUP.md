# Testing the Current Game Setup

## Overview
This document outlines the steps to verify that the Block Crusher game is correctly set up after PHASE 3 (Project Setup). The test covers file structure, visual rendering, input handling, and basic game mechanics.

## Prerequisites
- A modern web browser (Chrome, Firefox, Edge, Safari)
- Node.js (optional, for syntax check)

## Step 1: File Structure Verification
Ensure the following files exist in the project root:

```
block-crusher/
├── index.html
├── style.css
├── game.js
├── requirements.md
├── design.md
├── .gitignore
├── README.md
├── GIT_SETUP.md
└── setup_repo.bat
```

You can check with:
```bash
dir               # Windows
ls -la            # Linux/macOS
```

## Step 2: Open the Game in a Browser
1. Double‑click `index.html` or right‑click → “Open with” → choose your browser.
2. The page should load without errors and display:
   - A styled header “BLOCK CRUSHER”
   - A stats panel showing Score (0), Lives (3), Level (1), High Score (0)
   - A black canvas with a blue paddle, orange ball, and a grid of colored bricks
   - Control buttons (START GAME, PAUSE, RESTART, SOUND ON)
   - Instructions panel

If the page appears broken, check the browser’s developer console (F12) for errors.

## Step 3: Static Rendering Test
- Verify that the canvas shows:
  - **Paddle**: A blue rectangle at the bottom.
  - **Ball**: An orange circle positioned above the paddle.
  - **Bricks**: Five rows of colored rectangles (red, orange, yellow, green, blue) arranged in a grid.
- If any element is missing, there may be a JavaScript error.

## Step 4: Input Handling Test
### Keyboard Controls
- Press **LEFT ARROW** → paddle moves left (stops at left edge).
- Press **RIGHT ARROW** → paddle moves right (stops at right edge).
- Press **SPACEBAR** → ball launches and starts moving (if not already launched).
- Press **P** → game toggles pause state (PAUSE button text changes).

### Mouse Controls
- Move mouse over the canvas → paddle follows the mouse horizontally (clamped within canvas).
- Click on the canvas → ball launches (if not already launched).

## Step 5: Game Mechanics Test
1. **Start the game** by clicking the “START GAME” button.
   - The button becomes disabled.
   - The ball remains attached to the paddle until launched.
2. **Launch the ball** with spacebar or click.
   - The ball moves independently, bouncing off walls (top, left, right).
3. **Collision with bricks**:
   - Direct the ball toward a brick; the brick should disappear upon collision.
   - The score should increase by 10 points.
4. **Collision with paddle**:
   - Let the ball hit the paddle; it should bounce back upward.
5. **Ball loss**:
   - Let the ball fall below the paddle (bottom edge).
   - A life should be deducted (Lives decrease from 3 to 2).
   - Ball resets to paddle.
6. **Game Over**:
   - Repeat ball loss until lives reach 0.
   - An alert “Game Over!” should appear.
   - The game resets (score = 0, lives = 3, bricks restored).

## Step 6: UI & Button Functions
- **PAUSE button**: Click to pause/resume the game; the button text toggles.
- **RESTART button**: Click to reset the game to initial state (score, lives, bricks).
- **SOUND ON button**: Click to toggle sound state (visual feedback only; no actual sounds yet).

## Step 7: Browser Console Check
Open Developer Tools (F12) → Console tab. Look for any red error messages. If present, note them and fix accordingly.

Common errors:
- `Uncaught TypeError: Cannot read property 'getContext' of null` → Canvas ID mismatch.
- `Uncaught SyntaxError` → JavaScript syntax error.

## Step 8: Performance Check
- The game should run smoothly at 60 FPS (no visible stuttering).
- No memory leaks (observed via Task Manager) – not required for this simple test.

## Expected Outcome
After completing the tests, you should have a fully playable brick‑breaker game with:
- Functional paddle movement
- Ball physics and collision
- Brick breaking and scoring
- Lives system
- Basic UI controls

## If Something Fails
1. Check that all three core files (`index.html`, `style.css`, `game.js`) are present and correctly linked.
2. Ensure there are no typos in variable names (e.g., `gameCanvas` vs `gamecanvas`).
3. Verify that JavaScript is enabled in your browser.
4. Compare your code with the provided `game.js` for any missing functions.

## Next Steps
Once testing is successful, proceed to **PHASE 4: Core Game Mechanics** for detailed refinement of each component (paddle, ball, bricks, collision, game logic).

---
*Test completed on: 2025‑03‑30*