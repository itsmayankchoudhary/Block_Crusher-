# PHASE 7: Advanced Features (Optional)

This phase introduces optional enhancements that make the game more engaging and replayable. We have implemented a basic **levels system**; other features (sound, power‑ups) are described as blueprints for future expansion.

## 1. Levels System – Implemented

### Objective
Increase difficulty after clearing all bricks, encouraging players to improve their skills.

### Implementation
Two new functions were added to `game.js`:

#### `checkAllBricksCleared()`
- Iterates through the brick grid and returns `true` if no brick is visible.
- Called after each brick collision check.

#### `levelUp()`
- Increments `state.level` and updates the UI.
- Increases ball speed by 10% (`ball.dx *= 1.1`, `ball.dy *= 1.1`).
- Resets the ball to the paddle (via `resetBall()`).
- Regenerates all bricks (sets `visible = true`).
- Shows an alert announcing the new level (can be replaced with a canvas message).

### Integration
Inside `updateBall()`, after the brick‑collision loop:

```javascript
// Level completion check
if (checkAllBricksCleared()) {
    levelUp();
}
```

### Testing the Levels System
1. Start the game and break all bricks.
2. After the last brick disappears, an alert should appear: “Level 1 cleared! Starting level 2”.
3. Verify that:
   - The level counter in the stats panel increases.
   - The ball moves faster (noticeable increase in speed).
   - All bricks are restored.
   - The ball resets to the paddle.
4. Repeat for subsequent levels.

## 2. Increasing Difficulty – Extended Design

Beyond speed increase, you can introduce:

- **More bricks** – add extra rows/columns each level.
- **Smaller paddle** – reduce paddle width every few levels.
- **Faster ball** – exponential speed multiplier.
- **Indestructible bricks** – require multiple hits.

These can be added by modifying `levelUp()` and the brick‑generation logic.

## 3. Sound Effects – Blueprint

### Objective
Provide auditory feedback for collisions, brick breaks, life loss, and level completion.

### Implementation Steps
1. **Prepare audio files** (`.mp3` or `.wav`) for:
   - `brick.mp3` – brick break.
   - `paddle.mp3` – ball hits paddle.
   - `wall.mp3` – ball hits wall.
   - `life_lost.mp3` – ball falls.
   - `level_up.mp3` – level cleared.

2. **HTML Audio elements** (in `index.html`):
   ```html
   <audio id="soundBrick" src="sounds/brick.mp3" preload="auto"></audio>
   <audio id="soundPaddle" src="sounds/paddle.mp3" preload="auto"></audio>
   <!-- ... -->
   ```

3. **JavaScript helper** (in `game.js`):
   ```javascript
   function playSound(id) {
       if (!state.soundEnabled) return;
       const audio = document.getElementById(id);
       audio.currentTime = 0;
       audio.play().catch(e => console.log('Audio play failed:', e));
   }
   ```

4. **Trigger sounds** at appropriate events:
   - Brick collision → `playSound('soundBrick')`
   - Paddle collision → `playSound('soundPaddle')`
   - Wall collision → `playSound('soundWall')`
   - Life lost → `playSound('lifeLost')`
   - Level up → `playSound('levelUp')`

5. **Toggle button** – the existing “SOUND ON/OFF” button already toggles `state.soundEnabled`.

### Testing Sound
- With sound enabled, each game event should produce the corresponding sound.
- Toggling sound off should mute all audio.

## 4. Power‑Ups – Blueprint

### Objective
Special bricks drop power‑ups that fall downward; catching them with the paddle activates temporary effects.

### Design
- **Power‑up types**:
  - **Extra Ball** – adds a second ball.
  - **Enlarge Paddle** – increases paddle width for 10 seconds.
  - **Slow Ball** – reduces ball speed for 10 seconds.
  - **Sticky Paddle** – ball sticks to paddle until spacebar is pressed.

### Implementation Outline
1. **Power‑up object**:
   ```javascript
   const powerUp = {
       x: 0, y: 0, width: 30, height: 30,
       type: 'extraBall', // enum
       falling: false,
       dy: 3
   };
   ```

2. **Drop chance** – when a brick is broken, randomly decide whether to spawn a power‑up at the brick’s position.

3. **Update power‑ups** – move them downward; detect collision with paddle.

4. **Activate effect** – apply the effect for a duration, then revert.

5. **Visual feedback** – draw power‑ups as colored icons.

### Testing Power‑Ups
- Break bricks until a power‑up appears.
- Catch it with the paddle.
- Verify the effect activates and lasts for the intended duration.

## 5. Visual Effects – Suggestions

- **Paddle glow** on hit (already described in PHASE 6).
- **Brick break particles** – draw a few small rectangles that fade out.
- **Screen shake** on powerful collisions.

## 6. Testing All Advanced Features

### Test Plan
1. **Levels** – complete two full levels, confirm speed increase and brick regeneration.
2. **Sound** (if implemented) – verify each sound plays and mute works.
3. **Power‑ups** (if implemented) – spawn, catch, and confirm effect.
4. **Performance** – ensure added features do not cause frame drops.

### Expected Outcome
The game feels more dynamic, rewarding, and entertaining, with clear progression and optional audiovisual polish.

## 7. Next Step
Proceed to **PHASE 8: Testing & Debugging** – systematic validation of the entire game, bug fixes, and performance optimization.

---
*Advanced features are optional; even a subset of them significantly elevates the player experience.*