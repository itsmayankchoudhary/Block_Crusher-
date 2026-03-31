# PHASE 4: Core Game Mechanics

This phase implements the essential gameplay components step‑by‑step. Each sub‑step includes:

1. **Explanation** – What the component does and how it fits into the game.
2. **Code** – The relevant JavaScript code (already integrated into `game.js`).
3. **Test Instructions** – How to verify the component works correctly.
4. **Expected Output** – What you should observe after testing.

---

## STEP 4.1: Paddle

### Explanation
The paddle is the player‑controlled rectangle at the bottom of the canvas. It moves horizontally in response to keyboard arrows or mouse movement, and it must stay within the left/right boundaries of the game area. The paddle is used to bounce the ball upward.

### Code
Located in `game.js`:

```javascript
// Paddle properties (lines 27‑35)
const paddle = {
    x: canvas.width / 2 - 50,
    y: canvas.height - 20,
    width: 100,
    height: 15,
    speed: 8,
    color: '#0095DD',
    movingLeft: false,
    movingRight: false
};

// Drawing function (lines 139‑146)
function drawPaddle() {
    ctx.beginPath();
    ctx.rect(paddle.x, paddle.y, paddle.width, paddle.height);
    ctx.fillStyle = paddle.color;
    ctx.fill();
    ctx.closePath();
}

// Update paddle position based on keyboard input (lines 189‑197)
function updatePaddle() {
    if (paddle.movingLeft && paddle.x > 0) {
        paddle.x -= paddle.speed;
    }
    if (paddle.movingRight && paddle.x + paddle.width < canvas.width) {
        paddle.x += paddle.speed;
    }
}

// Keyboard event listeners (lines 160‑182)
document.addEventListener('keydown', (e) => {
    switch (e.key) {
        case 'ArrowLeft': paddle.movingLeft = true; break;
        case 'ArrowRight': paddle.movingRight = true; break;
    }
});
document.addEventListener('keyup', (e) => {
    switch (e.key) {
        case 'ArrowLeft': paddle.movingLeft = false; break;
        case 'ArrowRight': paddle.movingRight = false; break;
    }
});

// Mouse movement (lines 184‑191)
canvas.addEventListener('mousemove', (e) => {
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const mouseX = (e.clientX - rect.left) * scaleX;
    paddle.x = mouseX - paddle.width / 2;
    // Clamp to canvas boundaries
    if (paddle.x < 0) paddle.x = 0;
    if (paddle.x + paddle.width > canvas.width) paddle.x = canvas.width - paddle.width;
});
```

### Test Instructions
1. Open `index.html` in a browser.
2. Without starting the game, move the paddle using:
   - **Left/Right Arrow keys** – paddle should move left/right and stop at canvas edges.
   - **Mouse** – move cursor over the canvas; paddle should follow horizontally, staying within bounds.
3. Verify that the paddle is drawn as a blue rectangle at the bottom of the canvas.

### Expected Output
- Paddle moves smoothly with keyboard and mouse.
- Paddle never goes beyond the left or right edge of the canvas.
- Visual representation matches the defined dimensions and color.

---

## STEP 4.2: Ball

### Explanation
The ball is a circle that moves continuously after being launched. It bounces off the walls (left, right, top) and loses a life when it falls below the paddle (bottom). Before launch, the ball is attached to the paddle.

### Code
```javascript
// Ball properties (lines 18‑26)
const ball = {
    x: canvas.width / 2,
    y: canvas.height - 30,
    radius: 8,
    dx: 5,
    dy: -5,
    speed: 5,
    color: '#FF6600'
};

// Drawing function (lines 132‑138)
function drawBall() {
    ctx.beginPath();
    ctx.arc(ball.x, ball.y, ball.radius, 0, Math.PI * 2);
    ctx.fillStyle = ball.color;
    ctx.fill();
    ctx.closePath();
}

// Ball update and collision (lines 222‑279)
function updateBall() {
    if (!state.ballLaunched) {
        // Ball follows paddle before launch
        ball.x = paddle.x + paddle.width / 2;
        ball.y = paddle.y - ball.radius;
        return;
    }

    ball.x += ball.dx;
    ball.y += ball.dy;

    // Wall collisions
    if (ball.x + ball.radius > canvas.width || ball.x - ball.radius < 0) {
        ball.dx = -ball.dx;
    }
    if (ball.y - ball.radius < 0) {
        ball.dy = -ball.dy;
    }
    if (ball.y + ball.radius > canvas.height) {
        loseLife();
        return;
    }

    // Paddle collision (see STEP 4.4)
    // Brick collision (see STEP 4.4)
}
```

### Test Instructions
1. Open the game and click **START GAME**.
2. Observe that the ball is initially resting on the paddle (orange circle).
3. Press **SPACEBAR** or click the canvas to launch the ball.
4. Verify that the ball moves independently in a diagonal direction.
5. Check wall bouncing:
   - Ball should reflect when hitting left/right walls.
   - Ball should reflect when hitting the top wall.
   - Ball should disappear and cause a life loss when hitting the bottom (below paddle).

### Expected Output
- Ball launches and moves continuously.
- Ball bounces correctly off the three walls.
- Ball loss triggers life deduction and resets ball to paddle.

---

## STEP 4.3: Bricks

### Explanation
Bricks are rectangular blocks arranged in a grid at the top of the canvas. Each brick has a color, position, and a `visible` property. When hit by the ball, the brick becomes invisible and the player earns points.

### Code
```javascript
// Brick grid configuration (lines 37‑56)
const brickRowCount = 5;
const brickColumnCount = 9;
const brickWidth = 70;
const brickHeight = 20;
const brickPadding = 10;
const brickOffsetTop = 60;
const brickOffsetLeft = 35;
const bricks = [];
const brickColors = ['#FF5252', '#FF9800', '#FFEB3B', '#4CAF50', '#2196F3'];

// Initialize bricks
for (let c = 0; c < brickColumnCount; c++) {
    bricks[c] = [];
    for (let r = 0; r < brickRowCount; r++) {
        bricks[c][r] = {
            x: c * (brickWidth + brickPadding) + brickOffsetLeft,
            y: r * (brickHeight + brickPadding) + brickOffsetTop,
            width: brickWidth,
            height: brickHeight,
            color: brickColors[r],
            visible: true,
            points: 10
        };
    }
}

// Drawing function (lines 148‑162)
function drawBricks() {
    for (let c = 0; c < brickColumnCount; c++) {
        for (let r = 0; r < brickRowCount; r++) {
            const brick = bricks[c][r];
            if (brick.visible) {
                ctx.beginPath();
                ctx.rect(brick.x, brick.y, brick.width, brick.height);
                ctx.fillStyle = brick.color;
                ctx.fill();
                ctx.closePath();
            }
        }
    }
}
```

### Test Instructions
1. Open the game and look at the canvas.
2. Confirm that a grid of 5 rows × 9 columns of colored bricks appears at the top.
3. Start the game and direct the ball toward a brick.
4. Observe that the brick disappears upon collision and the score increases by 10 points.

### Expected Output
- Brick grid rendered with correct colors and spacing.
- Bricks disappear when hit by the ball.
- Score increments accordingly.

---

## STEP 4.4: Collision Detection

### Explanation
Collision detection determines when the ball intersects with the paddle or a brick. For simplicity, we use Axis‑Aligned Bounding Box (AABB) detection for bricks and a circle‑rectangle test for the paddle. When a collision is detected, the ball’s direction is updated and game state changes (score, brick visibility).

### Code
```javascript
// Paddle collision (lines 248‑268)
if (
    ball.y + ball.radius > paddle.y &&
    ball.y - ball.radius < paddle.y + paddle.height &&
    ball.x + ball.radius > paddle.x &&
    ball.x - ball.radius < paddle.x + paddle.width
) {
    const hitPos = (ball.x - paddle.x) / paddle.width;
    ball.dx = (hitPos - 0.5) * 10;
    ball.dy = -Math.abs(ball.dy);
    ball.y = paddle.y - ball.radius; // prevent sticking
}

// Brick collision (lines 270‑288)
for (let c = 0; c < brickColumnCount; c++) {
    for (let r = 0; r < brickRowCount; r++) {
        const brick = bricks[c][r];
        if (brick.visible) {
            if (
                ball.x + ball.radius > brick.x &&
                ball.x - ball.radius < brick.x + brick.width &&
                ball.y + ball.radius > brick.y &&
                ball.y - ball.radius < brick.y + brick.height
            ) {
                brick.visible = false;
                ball.dy = -ball.dy;
                addScore(brick.points);
            }
        }
    }
}
```

### Test Instructions
1. Start the game and launch the ball.
2. **Paddle collision**: Let the ball hit the paddle.
   - Ball should bounce upward with an angle that depends on where it hit the paddle.
   - Ball should not get stuck inside the paddle.
3. **Brick collision**: Aim the ball at a brick.
   - Brick should disappear instantly.
   - Ball should bounce away (usually downward, but direction may vary).
   - Score should increase by 10 points.

### Expected Output
- Ball bounces predictably off the paddle.
- Bricks break upon contact and score updates.
- No visual glitches or multiple collisions in a single frame.

---

## STEP 4.5: Game Logic

### Explanation
Game logic includes scoring, lives, game‑over conditions, and level progression. The score increases when bricks are broken; lives decrease when the ball falls below the paddle. The game ends when lives reach zero, and the player can restart.

### Code
```javascript
// Game state (lines 10‑16)
const state = {
    score: 0,
    lives: 3,
    level: 1,
    highScore: localStorage.getItem('blockCrusherHighScore') || 0,
    isRunning: false,
    isPaused: false,
    soundEnabled: true,
    ballLaunched: false
};

// Add points (lines 290‑300)
function addScore(points) {
    state.score += points;
    scoreElement.textContent = state.score;
    if (state.score > state.highScore) {
        state.highScore = state.score;
        highScoreElement.textContent = state.highScore;
        localStorage.setItem('blockCrusherHighScore', state.highScore);
    }
}

// Lose a life (lines 302‑312)
function loseLife() {
    state.lives--;
    livesElement.textContent = state.lives;
    if (state.lives <= 0) {
        gameOver();
    } else {
        resetBall();
    }
}

// Game over (lines 314‑320)
function gameOver() {
    state.isRunning = false;
    alert(`Game Over! Your score: ${state.score}`);
    resetGame();
}

// Reset game (lines 322‑340)
function resetGame() {
    state.score = 0;
    state.lives = 3;
    state.level = 1;
    state.ballLaunched = false;
    scoreElement.textContent = state.score;
    livesElement.textContent = state.lives;
    levelElement.textContent = state.level;
    resetBall();
    // Reset bricks
    for (let c = 0; c < brickColumnCount; c++) {
        for (let r = 0; r < brickRowCount; r++) {
            bricks[c][r].visible = true;
        }
    }
}
```

### Test Instructions
1. Start the game and break a few bricks.
   - Verify that the score increases.
2. Let the ball fall below the paddle three times.
   - After each fall, lives should decrease (3 → 2 → 1 → 0).
   - On the third fall, an alert “Game Over!” should appear.
3. Click **RESTART** button.
   - Score, lives, and bricks should reset to initial values.
   - High score should persist across sessions (stored in `localStorage`).

### Expected Output
- Score updates correctly.
- Lives decrease and game ends after three losses.
- Restart functionality works.
- High score is saved and displayed.

---

## Summary
All core game mechanics are now implemented and testable. The game is fully playable with paddle movement, ball physics, brick breaking, collision detection, scoring, and lives.

**Next Phase**: PHASE 5 – Game Loop Implementation (optimizing the main loop for smooth 60 FPS performance).