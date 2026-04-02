package engine;

import engine.core.GameLoop;
import engine.graphics.Renderer;
import engine.input.Input;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private static long window;
    private static int frameCount = 0;
    private static long lastFpsTime = 0;
    private static Renderer renderer;
    
    public static void main(String[] args) {
        System.out.println("Game Engine V1 – Starting game loop...");
        
        // Initialize GLFW and create window (same as before)
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        
        window = glfwCreateWindow(800, 600, "Game Engine Window", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }
        
        var vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
            window,
            (vidMode.width() - 800) / 2,
            (vidMode.height() - 600) / 2
        );
        glfwShowWindow(window);
        
        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);  // VSync on
        
        // Register input callbacks
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                Input.setKeyState(key, true);
            } else if (action == GLFW_RELEASE) {
                Input.setKeyState(key, false);
            }
        });

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (action == GLFW_PRESS) {
                Input.setMouseButtonState(button, true);
            } else if (action == GLFW_RELEASE) {
                Input.setMouseButtonState(button, false);
            }
        });

        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            // Convert from screen coordinates (0,0 = top‑left) to OpenGL coordinates (0,0 = bottom‑left)
            int[] height = new int[1];
            glfwGetWindowSize(window, null, height);
            Input.setMousePosition(xpos, height[0] - ypos);
        });
        
        // Initialize our renderer
        renderer = new Renderer();
        renderer.init();
        
        // Create the game loop
        GameLoop loop = new GameLoop(
            Main::update,
            Main::render,
            60,
            60
        );
        
        lastFpsTime = System.nanoTime();
        loop.start();
        
        // Cleanup
        renderer.cleanup();
        glfwDestroyWindow(window);
        glfwTerminate();
        System.out.println("Game loop stopped. Engine shutdown.");
    }
    
    private static void update() {
        float speed = 0.01f; // pixels per update (NDC units)
        float dx = 0.0f, dy = 0.0f;
        
        if (Input.isRightPressed()) dx += speed;
        if (Input.isLeftPressed())  dx -= speed;
        if (Input.isUpPressed())    dy += speed;
        if (Input.isDownPressed())  dy -= speed;
        
        renderer.setOffset(dx, dy);
    }
    
    private static void render() {
        frameCount++;
        long currentTime = System.nanoTime();
        if (currentTime - lastFpsTime >= 1_000_000_000) {
            System.out.println("FPS: " + frameCount);
            frameCount = 0;
            lastFpsTime = currentTime;
        }
        
        // Draw the triangle
        renderer.render();
        
        // Poll events and swap buffers
        glfwPollEvents();
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true);
        }
        glfwSwapBuffers(window);
    }
}