# PHASE 8: Testing & Debugging

## Objective
Systematically validate the game’s correctness, performance, and robustness. Identify and fix bugs, edge cases, and performance issues.

## 1. Test Categories

### 1.1 Functional Testing
Verify each game mechanic works as specified.

### 1.2 UI/UX Testing
Ensure the interface is responsive, intuitive, and free of visual defects.

### 1.3 Performance Testing
Confirm smooth gameplay with no frame drops or memory leaks.

### 1.4 Cross‑browser Testing
Check compatibility across different browsers.

## 2. Test Cases & Procedures

### 2.1 Core Gameplay

| Test Case | Procedure | Expected Result |
|-----------|-----------|-----------------|
| **TC1 – Paddle Movement** | Press LEFT/RIGHT arrows; move mouse over canvas. | Paddle moves horizontally, stays within canvas bounds. |
| **TC2 – Ball Launch** | Press SPACEBAR or click canvas after start. | Ball detaches from paddle and moves independently. |
| **TC3 – Wall Collision** | Let ball hit left, right, and top walls. | Ball bounces correctly (reflects). |
| **TC4 – Floor Collision** | Let ball fall below paddle. | Life decreases by 1; ball resets to paddle. |
| **TC5 – Paddle Collision** | Direct ball to hit paddle. | Ball bounces upward with angle variation; does not stick. |
| **TC6 – Brick Collision** | Break a single brick. | Brick disappears; score increases by 10. |
| **TC7 – Multiple Bricks** | Break several bricks in succession. | Each brick disappears, score updates cumulatively. |
| **TC8 – Level Completion** | Break all bricks. | Alert appears, level increments, ball speeds up, bricks regenerate. |
| **TC9 – Game Over** | Lose 3 lives. | “Game Over” alert appears; game resets after acknowledgment. |
| **TC10 – Restart** | Click RESTART button after game over. | Score, lives, bricks, ball reset to initial state. |

### 2.2 UI Components

| Test Case | Procedure | Expected Result |
|-----------|-----------|-----------------|
| **TC11 – Score Display** | Break bricks. | Score updates in real‑time. |
| **TC12 – Lives Display** | Lose a life. | Lives counter decreases. |
| **TC13 – Level Display** | Complete a level. | Level counter increments. |
| **TC14 – High Score** | Exceed previous high score. | High score updates and persists after page refresh. |
| **TC15 – Button States** | Click START, PAUSE, RESTART, SOUND. | Buttons perform their described actions; visual feedback on hover. |
| **TC16 – Responsive Layout** | Resize browser window to mobile size. | UI adapts (stats stack, buttons full‑width, canvas scales). |

### 2.3 Edge Cases & Bug Hunting

| Test Case | Procedure | Expected Result |
|-----------|-----------|-----------------|
| **TC17 – Ball Stuck** | Repeatedly bounce ball between paddle and bricks. | Ball never gets stuck inside objects. |
| **TC18 – Rapid Paddle Movement** | Mash arrow keys while ball is near paddle. | No visual glitches; collision detection remains accurate. |
| **TC19 – Consecutive Brick Hits** | Ball hits two bricks in the same frame. | Both bricks break; score increments twice. |
| **TC20 – Paddle at Edge** | Place paddle at extreme left/right; launch ball toward edge. | Ball bounces normally; no out‑of‑bounds artifacts. |
| **TC21 – Pause During Collision** | Pause the game the moment ball hits a brick. | Game freezes; after resume, collision resolves correctly. |
| **TC22 – Background Tab** | Switch to another tab for 10 seconds, then return. | Game continues smoothly; no large time‑delta jumps. |
| **TC23 – Sound Toggle** | Toggle sound on/off during gameplay. | No errors; sound state persists. |

### 2.4 Performance

| Test Case | Procedure | Expected Result |
|-----------|-----------|-----------------|
| **TC24 – Frame Rate** | Open browser’s performance monitor, play for 30 seconds. | Steady 60 FPS (or native refresh rate) with no noticeable drops. |
| **TC25 – Memory Usage** | Monitor memory in developer tools; play for 5 minutes. | No continuous memory increase (no leaks). |
| **TC26 – CPU Load** | Observe CPU usage; game should not cause excessive load. | CPU usage stays reasonable (< 30% on typical hardware). |

## 3. Automated Checks

Run the following commands in the project root to catch syntax and style issues:

```bash
# Syntax validation
node -c game.js

# (Optional) ESLint if configured
# npx eslint game.js
```

Expected: No errors.

## 4. Cross‑Browser Compatibility

Test the game in the following browsers (latest versions):

- Chrome
- Firefox
- Edge
- Safari (if available)

**Checklist:**
- [ ] Canvas renders correctly.
- [ ] Keyboard/mouse inputs work.
- [ ] UI styling appears consistent.
- [ ] No console errors.

## 5. Debugging Tips

If a test fails:

1. **Open the browser’s developer console** (F12) – look for red error messages.
2. **Common issues and fixes:**

   - **Ball passes through paddle** – refine collision detection in `updateBall()`.
   - **Bricks not disappearing** – verify `brick.visible` is set to `false` and `drawBricks()` skips invisible bricks.
   - **Score not updating** – ensure `addScore()` is called and updates the DOM element.
   - **Game loop stutters** – add delta‑time clamping and cap maximum delta.
   - **High score not saving** – check `localStorage` permissions and JSON serialization.

3. **Use `console.log()`** strategically to trace variable values (e.g., ball position, collision flags).

## 6. Regression Testing

After any bug fix, re‑run the relevant test cases to ensure no new bugs were introduced.

## 7. Expected Outcome

All test cases pass, and the game runs flawlessly across target browsers with smooth performance.

## 8. Next Step

Proceed to **PHASE 9: Final Delivery** – packaging the complete game, writing final documentation, and providing run instructions.

---
*Thorough testing is crucial for a professional‑quality game. Do not skip this phase.*