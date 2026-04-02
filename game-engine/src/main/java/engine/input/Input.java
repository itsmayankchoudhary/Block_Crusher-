package engine.input;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
    private static final int MAX_KEYS = 512;
    private static final int MAX_MOUSE_BUTTONS = 16;
    
    private static boolean[] keyPressed = new boolean[MAX_KEYS];
    private static boolean[] mousePressed = new boolean[MAX_MOUSE_BUTTONS];
    private static double mouseX, mouseY;
    
    // Callback setters – to be called from GLFW callbacks
    public static void setKeyState(int key, boolean pressed) {
        if (key >= 0 && key < MAX_KEYS) {
            keyPressed[key] = pressed;
        }
    }
    
    public static void setMouseButtonState(int button, boolean pressed) {
        if (button >= 0 && button < MAX_MOUSE_BUTTONS) {
            mousePressed[button] = pressed;
        }
    }
    
    public static void setMousePosition(double x, double y) {
        mouseX = x;
        mouseY = y;
    }
    
    // Polling queries
    public static boolean isKeyPressed(int key) {
        if (key < 0 || key >= MAX_KEYS) return false;
        return keyPressed[key];
    }
    
    public static boolean isMouseButtonPressed(int button) {
        if (button < 0 || button >= MAX_MOUSE_BUTTONS) return false;
        return mousePressed[button];
    }
    
    public static double getMouseX() { return mouseX; }
    public static double getMouseY() { return mouseY; }
    
    // Helper for common keys
    public static boolean isUpPressed()    { return isKeyPressed(GLFW_KEY_UP)    || isKeyPressed(GLFW_KEY_W); }
    public static boolean isDownPressed()  { return isKeyPressed(GLFW_KEY_DOWN)  || isKeyPressed(GLFW_KEY_S); }
    public static boolean isLeftPressed()  { return isKeyPressed(GLFW_KEY_LEFT)  || isKeyPressed(GLFW_KEY_A); }
    public static boolean isRightPressed() { return isKeyPressed(GLFW_KEY_RIGHT) || isKeyPressed(GLFW_KEY_D); }
    public static boolean isSpacePressed() { return isKeyPressed(GLFW_KEY_SPACE); }
}