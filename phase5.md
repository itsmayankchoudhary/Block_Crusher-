# Phase 5: Input System

## STEP 5: Making the Engine Respond to the Player

### 1. Goal
* Capture **keyboard** and **mouse** input.
* Implement **polling** (check key state each frame) and **event‑driven** (callbacks) approaches.
* Move the triangle with arrow keys.
* Display mouse coordinates on the screen (optional).
* Understand input abstraction for later game‑object control.

### 2. Concept

#### Polling vs Event‑Driven
* **Polling** – Every frame we ask “is the A key currently pressed?” Simple, works for continuous movement (like holding an arrow key).  
  Example: `if (keyPressed(KEY_RIGHT)) player.x += speed;`
* **Event‑driven** – We register a callback that fires **once** when a key is pressed or released. Perfect for one‑shot actions (jump, shoot).  
  Example: `onKeyPress(KEY_SPACE, () -> player.jump());`

For our engine we’ll support **both**:
* A **polling‑based** `Input` class that stores the current state of every key.
* **GLFW callbacks** that update that state whenever a key changes.

#### Keyboard Scancodes vs Keycodes
* **Keycode** – The logical key (e.g., `GLFW_KEY_A`) – same across keyboards.
* **Scancode** – The physical position of the key on the keyboard – differs between layouts (AZERTY vs QWERTY).
We’ll use **keycodes** because they’re simpler and portable enough for our needs.

#### Mouse Input
* **Position** – `(x, y)` in screen coordinates (0,0 = top‑left on Windows, bottom‑left in OpenGL – we’ll convert).
* **Buttons** – Left, right, middle – treated just like keyboard keys.

### 3. Implementation

#### Step 5.1 – Create the Input Class
Create `src/main/java/engine/input/Input.java`:

```java
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
```

#### Step 5.2 – Register GLFW Callbacks in Main.java
We need to attach GLFW key and mouse callbacks to update our `Input` class. Add these methods to `Main.java` **before** starting the game loop.

Insert the following right after `glfwMakeContextCurrent(window)` and before `renderer.init()`:

```java
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
```

#### Step 5.3 – Move the Triangle with Arrow Keys
We need to make the triangle’s position mutable. Let’s modify `Renderer` to store a translation offset and update it based on input.

First, add a `translate` vector to `Renderer`:

```java
private float offsetX = 0.0f;
private float offsetY = 0.0f;

public void setOffset(float dx, float dy) {
    offsetX = dx;
    offsetY = dy;
}
```

Update the vertex shader to apply the offset. Change `simple.vert` to:

```glsl
#version 330 core

layout (location = 0) in vec2 aPos;
uniform vec2 uOffset;  // New uniform

void main() {
    gl_Position = vec4(aPos + uOffset, 0.0, 1.0);
}
```

Then in `Renderer.render()`, set the uniform before drawing:

```java
shader.use();
int offsetLoc = glGetUniformLocation(shader.getId(), "uOffset");
glUniform2f(offsetLoc, offsetX, offsetY);
```

Now, in `Main.update()` (which is called 60 times per second), read the arrow keys and adjust the offset.

Add a static `Renderer` reference in `Main` (already there) and call:

```java
private static void update() {
    float speed = 0.01f; // pixels per update (NDC units)
    float dx = 0.0f, dy = 0.0f;
    
    if (Input.isRightPressed()) dx += speed;
    if (Input.isLeftPressed())  dx -= speed;
    if (Input.isUpPressed())    dy += speed;
    if (Input.isDownPressed())  dy -= speed;
    
    renderer.setOffset(dx, dy);
}
```

#### Step 5.4 – Display Mouse Coordinates (Optional)
Add a debug line in `Main.render()` that prints the mouse position every second.

```java
// Inside render(), after FPS logging
if (currentTime - lastMouseTime >= 1_000_000_000) {
    System.out.printf("Mouse: (%.0f, %.0f)%n", Input.getMouseX(), Input.getMouseY());
    lastMouseTime = currentTime;
}
```

### 4. Code
New files:
* `src/main/java/engine/input/Input.java`

Modified files:
* `src/main/java/engine/Main.java` – added callbacks.
* `src/main/java/engine/graphics/Renderer.java` – added offset and uniform.
* `src/main/resources/shaders/simple.vert` – added `uOffset` uniform.

### 5. Output
* The red triangle should **move smoothly** when you press arrow keys (or WASD).
* The triangle should stop moving when you release the keys.
* The terminal should still show FPS, plus mouse coordinates every second (if you added the debug line).
* The mouse position printed should reflect the cursor location inside the window.

### 6. Common Errors
* **Triangle doesn’t move** – Check that the uniform location is found (`offsetLoc != -1`). Print shader compilation logs.
* **Input lag** – Ensure you’re polling in `update()` (60 Hz) not just in `render()` (variable).
* **Mouse coordinates upside down** – Remember that OpenGL’s origin is bottom‑left, while GLFW’s default is top‑left. The conversion we did (`height[0] - ypos`) fixes this.
* **Keys not detected** – Verify that the GLFW key callback is registered **before** the game loop starts.

---
**Next Step**: [Phase 6: Entity Component System (ECS)](phase6.md) – we’ll replace the hard‑coded triangle with a flexible ECS that can manage thousands of game objects.
