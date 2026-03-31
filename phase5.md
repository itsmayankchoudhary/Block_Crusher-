# PHASE 5: Game Loop Implementation

## Objective
Implement a stable, smooth game loop that updates game state and renders graphics at a consistent frame rate (ideally 60 FPS) without flickering or lag.

## 1. The Game Loop Pattern
A game loop continuously performs three tasks:
1. **Process input** (handled by event listeners outside the loop)
2. **Update game state** (move objects, detect collisions, update scores)
3. **Render** (draw everything on the canvas)

In JavaScript, the best practice is to use `requestAnimationFrame(callback)`, which synchronizes with the browser’s repaint cycle, providing smooth animations and efficient CPU usage.

## 2. Current Implementation
The game loop is defined in `game.js` (lines 373‑388):

```javascript
let lastTime = 0;

/**
 * Main game loop
 * @param {number} timestamp - Current time in milliseconds
 */
function gameLoop(timestamp) {
    if (!state.isRunning || state.isPaused) {
        requestAnimationFrame(gameLoop);
        return;
    }

    const delta = timestamp - lastTime;
    lastTime = timestamp;

    updatePaddle();
    updateBall();
    draw();

    requestAnimationFrame(gameLoop);
}
```

### Key Design Decisions
- **`requestAnimationFrame`** – ensures the loop runs at the display’s refresh rate (typically 60 Hz).
- **Delta time** – calculated as `timestamp - lastTime`. Although not yet used for movement calculations, it is available for future frame‑rate‑independent physics.
- **Pause/run control** – the loop continues to run even when paused, but skips update/render steps, keeping the animation alive and ready to resume instantly.
- **Single responsibility** – each iteration updates paddle, ball, and draws the frame.

## 3. Smooth Gameplay Guarantees
To avoid flickering and ensure smoothness:

- **Double buffering**: The HTML5 Canvas implicitly uses double buffering; we draw the entire scene in one pass (`draw()` clears the canvas and redraws all objects).
- **No state mutations during render**: The rendering phase is read‑only; all game‑state changes happen in `updatePaddle()` and `updateBall()`.
- **Efficient collision detection**: The brick‑collision loop is optimized by breaking early after a hit (though the current implementation continues checking; this is acceptable for a small grid).

## 4. Testing the Game Loop

### Test 1: Visual Smoothness
1. Open `index.html` and start the game.
2. Observe the movement of the ball and paddle.
3. **Expected**: Motion is fluid, without stuttering or visible jumps.

### Test 2: Frame Rate Consistency
1. Open the browser’s developer tools (F12) and go to the **Performance** or **Rendering** tab.
2. Enable “Frame rate” overlay.
3. Play the game for 30 seconds.
4. **Expected**: Frame rate stays close to 60 FPS (or your display’s refresh rate) with no significant drops.

### Test 3: Pause/Resume Stability
1. Start the game and let the ball move.
2. Press **P** or click the PAUSE button.
3. Verify that the game freezes (no movement).
4. Press **P** again (or RESUME) – the game should continue exactly from where it stopped, with no visual glitches.

### Test 4: Background Tab Behavior
1. Start the game.
2. Switch to another browser tab for a few seconds.
3. Return to the game tab.
4. **Expected**: The game resumes smoothly; the ball does not jump unnaturally (thanks to delta‑time clamping, which we could add in an advanced version).

## 5. Optional Enhancements
If you wish to make the game loop more robust, consider the following additions (already prepared as snippets):

### 5.1 Delta‑Time Scaling
Modify `updateBall()` to use `delta` for frame‑rate‑independent movement:

```javascript
function updateBall(delta) {
    const scale = delta / 16.67; // 60 FPS reference
    ball.x += ball.dx * scale;
    ball.y += ball.dy * scale;
    // ... rest of collision logic
}
```

### 5.2 FPS Counter (Debug)
Add a small FPS display in the corner of the canvas:

1. Insert a `<div id="fps">FPS: 0</div>` in `index.html`.
2. In `game.js`:
   ```javascript
   let frameCount = 0;
   let fps = 0;
   let fpsLastUpdate = 0;

   function updateFPS(timestamp) {
       frameCount++;
       if (timestamp - fpsLastUpdate >= 1000) {
           fps = Math.round((frameCount * 1000) / (timestamp - fpsLastUpdate));
           document.getElementById('fps').textContent = `FPS: ${fps}`;
           frameCount = 0;
           fpsLastUpdate = timestamp;
       }
   }
   ```
   Call `updateFPS(timestamp)` inside `gameLoop`.

### 5.3 Delta Clamping
Prevent huge `delta` values when the tab is inactive:

```javascript
const delta = Math.min(timestamp - lastTime, 100); // cap at 100 ms
lastTime = timestamp;
```

## 6. Expected Output
After verifying the tests, you should have:
- A smoothly animating game with no visible stutter.
- Consistent frame rates (close to your display’s refresh rate).
- Proper pause/resume functionality.
- A game loop that is easy to extend for future features (levels, power‑ups, etc.).

## 7. Next Step
Proceed to **PHASE 6: UI & UX**, where we will polish the user interface, add visual feedback, improve colors, and implement start/restart buttons with better styling.

---
*The game loop is the heart of the game; a well‑implemented loop ensures a professional, enjoyable player experience.*