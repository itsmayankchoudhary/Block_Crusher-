// Block Crusher - Brick Breaker Game
// Main game JavaScript file

// ============================================
// GLOBAL VARIABLES & CONSTANTS
// ============================================

const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const scoreElement = document.getElementById('score');
const livesElement = document.getElementById('lives');
const levelElement = document.getElementById('level');
const highScoreElement = document.getElementById('high-score');
const startBtn = document.getElementById('startBtn');
const pauseBtn = document.getElementById('pauseBtn');
const restartBtn = document.getElementById('restartBtn');
const soundBtn = document.getElementById('soundBtn');

// Game state
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

// Ball properties
const ball = {
    x: canvas.width / 2,
    y: canvas.height - 30,
    radius: 8,
    dx: 5,
    dy: -5,
    speed: 5,
    color: '#FF6600'
};

// Paddle properties
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

// Brick grid properties
const brickRowCount = 5;
const brickColumnCount = 9;
const brickWidth = 70;
const brickHeight = 20;
const brickPadding = 10;
const brickOffsetTop = 60;
const brickOffsetLeft = 35;
const bricks = [];

// Colors for brick rows
const brickColors = ['#FF5252', '#FF9800', '#FFEB3B', '#4CAF50', '#2196F3'];

// Initialize brick grid
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

// ============================================
// DRAWING FUNCTIONS
// ============================================

/**
 * Draw the ball on the canvas
 */
function drawBall() {
    ctx.beginPath();
    ctx.arc(ball.x, ball.y, ball.radius, 0, Math.PI * 2);
    ctx.fillStyle = ball.color;
    ctx.fill();
    ctx.closePath();
}

/**
 * Draw the paddle on the canvas
 */
function drawPaddle() {
    ctx.beginPath();
    ctx.rect(paddle.x, paddle.y, paddle.width, paddle.height);
    ctx.fillStyle = paddle.color;
    ctx.fill();
    ctx.closePath();
}

/**
 * Draw all bricks that are visible
 */
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

/**
 * Draw the game UI (score, lives, etc.)
 */
function drawUI() {
    // Score display is already handled by HTML elements
    // Additional on‑canvas text can be added here
}

/**
 * Clear the canvas
 */
function clearCanvas() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
}

/**
 * Draw everything (called each frame)
 */
function draw() {
    clearCanvas();
    drawBricks();
    drawPaddle();
    drawBall();
    drawUI();
}

// ============================================
// INPUT HANDLING
// ============================================

document.addEventListener('keydown', (e) => {
    switch (e.key) {
        case 'ArrowLeft':
            paddle.movingLeft = true;
            break;
        case 'ArrowRight':
            paddle.movingRight = true;
            break;
        case ' ':
            if (!state.ballLaunched) {
                state.ballLaunched = true;
            }
            break;
        case 'p':
        case 'P':
            togglePause();
            break;
    }
});

document.addEventListener('keyup', (e) => {
    switch (e.key) {
        case 'ArrowLeft':
            paddle.movingLeft = false;
            break;
        case 'ArrowRight':
            paddle.movingRight = false;
            break;
    }
});

canvas.addEventListener('mousemove', (e) => {
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const mouseX = (e.clientX - rect.left) * scaleX;
    paddle.x = mouseX - paddle.width / 2;
    // Keep paddle within canvas
    if (paddle.x < 0) paddle.x = 0;
    if (paddle.x + paddle.width > canvas.width) paddle.x = canvas.width - paddle.width;
});

canvas.addEventListener('click', () => {
    if (!state.ballLaunched) {
        state.ballLaunched = true;
    }
});

// ============================================
// GAME LOGIC & UPDATES
// ============================================

/**
 * Update paddle position based on keyboard input
 */
function updatePaddle() {
    if (paddle.movingLeft && paddle.x > 0) {
        paddle.x -= paddle.speed;
    }
    if (paddle.movingRight && paddle.x + paddle.width < canvas.width) {
        paddle.x += paddle.speed;
    }
}

/**
 * Update ball position and handle collisions
 */
function updateBall() {
    if (!state.ballLaunched) {
        // Ball follows paddle before launch
        ball.x = paddle.x + paddle.width / 2;
        ball.y = paddle.y - ball.radius;
        return;
    }

    ball.x += ball.dx;
    ball.y += ball.dy;

    // Wall collision (left/right)
    if (ball.x + ball.radius > canvas.width || ball.x - ball.radius < 0) {
        ball.dx = -ball.dx;
    }
    // Ceiling collision
    if (ball.y - ball.radius < 0) {
        ball.dy = -ball.dy;
    }
    // Floor collision (lose life)
    if (ball.y + ball.radius > canvas.height) {
        loseLife();
        return;
    }

    // Paddle collision
    if (
        ball.y + ball.radius > paddle.y &&
        ball.y - ball.radius < paddle.y + paddle.height &&
        ball.x + ball.radius > paddle.x &&
        ball.x - ball.radius < paddle.x + paddle.width
    ) {
        // Adjust bounce angle based on where the ball hits the paddle
        const hitPos = (ball.x - paddle.x) / paddle.width;
        // Normalized vector: -0.5 to +0.5, scaled by 10
        ball.dx = (hitPos - 0.5) * 10;
        // Ensure upward direction and keep speed consistent
        ball.dy = -Math.abs(ball.dy);
        // Move ball above paddle to prevent sticking
        ball.y = paddle.y - ball.radius;
    }

    // Brick collision (simple AABB)
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
            // Level completion check
            if (checkAllBricksCleared()) {
                levelUp();
            }
        }
    }
}

/**
 * Add points to score
 * @param {number} points - Points to add
 */
function addScore(points) {
    state.score += points;
    scoreElement.textContent = state.score;
    if (state.score > state.highScore) {
        state.highScore = state.score;
        highScoreElement.textContent = state.highScore;
        localStorage.setItem('blockCrusherHighScore', state.highScore);
    }
}

/**
 * Check if all bricks have been cleared
 * @returns {boolean} True if no brick is visible
 */
function checkAllBricksCleared() {
    for (let c = 0; c < brickColumnCount; c++) {
        for (let r = 0; r < brickRowCount; r++) {
            if (bricks[c][r].visible) return false;
        }
    }
    return true;
}

/**
 * Advance to next level: increase level, speed, reset bricks
 */
function levelUp() {
    state.level++;
    levelElement.textContent = state.level;
    // Increase ball speed by 10%
    ball.dx *= 1.1;
    ball.dy *= 1.1;
    // Reset ball to paddle
    resetBall();
    // Regenerate bricks
    for (let c = 0; c < brickColumnCount; c++) {
        for (let r = 0; r < brickRowCount; r++) {
            bricks[c][r].visible = true;
        }
    }
    // Optional: show message
    alert(`Level ${state.level - 1} cleared! Starting level ${state.level}`);
}

/**
 * Deduct a life and reset ball
 */
function loseLife() {
    state.lives--;
    livesElement.textContent = state.lives;
    if (state.lives <= 0) {
        gameOver();
    } else {
        resetBall();
    }
}

/**
 * Reset ball to paddle
 */
function resetBall() {
    ball.x = canvas.width / 2;
    ball.y = canvas.height - 30;
    ball.dx = 5;
    ball.dy = -5;
    state.ballLaunched = false;
}

/**
 * Game over logic
 */
function gameOver() {
    state.isRunning = false;
    alert(`Game Over! Your score: ${state.score}`);
    resetGame();
}

/**
 * Reset the entire game
 */
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

/**
 * Toggle pause state
 */
function togglePause() {
    state.isPaused = !state.isPaused;
    if (state.isPaused) {
        pauseBtn.innerHTML = '<i class="fas fa-play"></i> RESUME';
    } else {
        pauseBtn.innerHTML = '<i class="fas fa-pause"></i> PAUSE';
    }
}

// ============================================
// GAME LOOP
// ============================================

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

// ============================================
// EVENT LISTENERS FOR BUTTONS
// ============================================

startBtn.addEventListener('click', () => {
    if (!state.isRunning) {
        state.isRunning = true;
        state.isPaused = false;
        startBtn.disabled = true;
        startBtn.style.opacity = '0.6';
        lastTime = performance.now();
        requestAnimationFrame(gameLoop);
    }
});

pauseBtn.addEventListener('click', togglePause);

restartBtn.addEventListener('click', () => {
    resetGame();
    state.isRunning = false;
    state.isPaused = false;
    startBtn.disabled = false;
    startBtn.style.opacity = '1';
    pauseBtn.innerHTML = '<i class="fas fa-pause"></i> PAUSE';
    draw();
});

soundBtn.addEventListener('click', () => {
    state.soundEnabled = !state.soundEnabled;
    soundBtn.innerHTML = state.soundEnabled
        ? '<i class="fas fa-volume-up"></i> SOUND ON'
        : '<i class="fas fa-volume-mute"></i> SOUND OFF';
});

// ============================================
// INITIALIZATION
// ============================================

/**
 * Initialize game: set UI values and draw initial frame
 */
function init() {
    scoreElement.textContent = state.score;
    livesElement.textContent = state.lives;
    levelElement.textContent = state.level;
    highScoreElement.textContent = state.highScore;
    draw();
}

// Start everything when page loads
window.onload = init;