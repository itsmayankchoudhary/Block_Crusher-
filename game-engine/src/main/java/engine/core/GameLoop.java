package engine.core;

/**
 * A fixed‑timestep game loop that separates update and render.
 */
public class GameLoop {
    private final Runnable update;
    private final Runnable render;
    private final float updateInterval;  // seconds per update
    private boolean running;
    private long previousTime;
    private float accumulator;
    
    /**
     * @param update Called every updateInterval seconds.
     * @param render Called as often as possible (capped by targetFps).
     * @param updatesPerSecond How many times per second update is called.
     * @param targetFps Maximum frames per second (0 = uncapped).
     */
    public GameLoop(Runnable update, Runnable render, int updatesPerSecond, int targetFps) {
        this.update = update;
        this.render = render;
        this.updateInterval = 1.0f / updatesPerSecond;
        this.running = false;
        this.previousTime = 0;
        this.accumulator = 0.0f;
    }
    
    public void start() {
        if (running) return;
        running = true;
        previousTime = System.nanoTime();
        runLoop();
    }
    
    public void stop() {
        running = false;
    }
    
    private void runLoop() {
        while (running) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000.0f; // seconds
            previousTime = currentTime;
            
            // Prevent spiral of death
            if (deltaTime > 0.25f) deltaTime = 0.25f;
            
            accumulator += deltaTime;
            
            // Fixed‑step updates
            while (accumulator >= updateInterval) {
                update.run();
                accumulator -= updateInterval;
            }
            
            // Render (with interpolation if we wanted smoothness)
            render.run();
            
            // Simple FPS cap (optional)
            // sleepRemainingTime(deltaTime);
        }
    }
    
    // Optional: method to cap FPS by sleeping
    private void sleepRemainingTime(float deltaTime) {
        float targetFrameTime = 1.0f / 60.0f; // 60 FPS
        if (deltaTime < targetFrameTime) {
            try {
                Thread.sleep((long) ((targetFrameTime - deltaTime) * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}