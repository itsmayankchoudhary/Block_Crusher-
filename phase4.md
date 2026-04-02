# Phase 4: Window & Rendering Setup

## STEP 4: Drawing Our First Triangle

### 1. Goal
* Initialize **OpenGL** and set up a basic rendering pipeline.
* Write **vertex and fragment shaders** in GLSL.
* Create a **shader program** and compile it.
* Draw a **colored triangle** on the screen.
* Understand the difference between **normalized device coordinates** and screen pixels.

### 2. Concept

#### What is OpenGL?
OpenGL is a cross‑platform graphics API that talks directly to your GPU. It works as a **state machine** – you set certain states (like which shader is active, which buffers are bound) and then issue draw commands.

#### The Rendering Pipeline (Simplified)
1. **Vertices** – A list of 3D points (for us, 2D points with `z = 0`).
2. **Vertex Shader** – Runs once per vertex. Transforms the vertex position into **clip space**.
3. **Rasterization** – Converts the triangle into pixels (fragments).
4. **Fragment Shader** – Runs once per pixel. Determines the pixel’s color.
5. **Frame Buffer** – The final image that appears on the screen.

#### Normalized Device Coordinates (NDC)
OpenGL expects vertex coordinates in a **cube** that goes from `-1` to `+1` in each axis:
* `x = -1` → left edge of screen, `x = +1` → right edge.
* `y = -1` → bottom edge, `y = +1` → top edge.
* `z = -1` → nearest, `z = +1` → farthest (we’ll ignore `z` for 2D).

A triangle with vertices `(-0.5, -0.5)`, `(0.5, -0.5)`, `(0.0, 0.5)` will appear in the center of the window, covering half the screen.

#### Shaders
* **Vertex Shader** – Moves vertices. Our vertex shader will just pass through the coordinates (we’ll add transformations later).
* **Fragment Shader** – Colors pixels. Our fragment shader will output a solid red color.

### 3. Implementation

#### Step 4.1 – Create Shader Files
Create two text files in `src/main/resources/shaders/`:

**`simple.vert`** – Vertex shader.
```glsl
#version 330 core

layout (location = 0) in vec2 aPos;

void main() {
    gl_Position = vec4(aPos, 0.0, 1.0);
}
```

**`simple.frag`** – Fragment shader.
```glsl
#version 330 core

out vec4 FragColor;

void main() {
    FragColor = vec4(1.0, 0.0, 0.0, 1.0); // Solid red
}
```

Create the directories and files using your terminal:
```cmd
mkdir -p src\main\resources\shaders
cd src\main\resources\shaders
echo #version 330 core ... > simple.vert   (or use a text editor)
```

#### Step 4.2 – Write a Shader Loader Class
Create `src/main/java/engine/graphics/Shader.java`:

```java
package engine.graphics;

import static org.lwjgl.opengl.GL33.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Shader {
    private final int programId;
    
    public Shader(String vertexPath, String fragmentPath) {
        int vertex = compileShader(vertexPath, GL_VERTEX_SHADER);
        int fragment = compileShader(fragmentPath, GL_FRAGMENT_SHADER);
        
        programId = glCreateProgram();
        glAttachShader(programId, vertex);
        glAttachShader(programId, fragment);
        glLinkProgram(programId);
        
        // Check linking errors
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader linking failed: " + glGetProgramInfoLog(programId));
        }
        
        // Clean up individual shaders
        glDeleteShader(vertex);
        glDeleteShader(fragment);
    }
    
    private int compileShader(String path, int type) {
        String source;
        try {
            source = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException("Could not read shader file: " + path, e);
        }
        
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compilation failed: " + glGetShaderInfoLog(shader));
        }
        
        return shader;
    }
    
    public void use() {
        glUseProgram(programId);
    }
    
    public int getId() {
        return programId;
    }
}
```

#### Step 4.3 – Create a Triangle Renderer
Create `src/main/java/engine/graphics/Renderer.java`:

```java
package engine.graphics;

import static org.lwjgl.opengl.GL33.*;

public class Renderer {
    private Shader shader;
    private int vao;
    private int vbo;
    
    public void init() {
        // Load shaders
        shader = new Shader(
            "src/main/resources/shaders/simple.vert",
            "src/main/resources/shaders/simple.frag"
        );
        
        // Triangle vertices (x, y) in NDC
        float[] vertices = {
            -0.5f, -0.5f,
             0.5f, -0.5f,
             0.0f,  0.5f
        };
        
        // Create Vertex Array Object (VAO)
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        
        // Create Vertex Buffer Object (VBO)
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        
        // Specify vertex layout
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // Unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    public void render() {
        // Clear the screen with dark gray
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        // Use our shader
        shader.use();
        
        // Draw the triangle
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);
    }
    
    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        // Shader deletion is optional (will be cleaned up when program ends)
    }
}
```

#### Step 4.4 – Update Main.java to Use the Renderer
Modify `Main.java` to create and call the renderer:

```java
package engine;

import engine.core.GameLoop;
import engine.graphics.Renderer;
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
        // Still empty for now
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
```

#### Step 4.5 – Build and Run
Run the engine again:

```cmd
gradle run
```

### 4. Code
New files added:
* `src/main/resources/shaders/simple.vert`
* `src/main/resources/shaders/simple.frag`
* `src/main/java/engine/graphics/Shader.java`
* `src/main/java/engine/graphics/Renderer.java`

Modified:
* `src/main/java/engine/Main.java`

### 5. Output
* A **dark gray window** with a **red triangle** in the center.
* The triangle should be static (not moving).
* The terminal should still print the FPS (close to 60).
* Pressing **Escape** closes the window.

If you see the red triangle, congratulations – you have successfully set up **OpenGL rendering**!

### 6. Common Errors
* **`GLFW failed to create OpenGL context`** – Your GPU may not support OpenGL 3.3. Try lowering the version hints to `(3, 2)` or `(3, 0)`.
* **`Shader compilation failed`** – Check that the shader files are exactly as shown, with no extra spaces or line breaks. Ensure the file path is correct (relative to the working directory).
* **`Invalid memory access`** – You may have forgotten to call `glfwMakeContextCurrent(window)` before any OpenGL call.
* **Triangle is not visible** – Make sure the vertices are within NDC (`-1` to `+1`). Also check that `glClearColor` is not the same color as the triangle (e.g., red on red).
* **`java.nio.file.NoSuchFileException`** – The shader files are not in the expected location. Use absolute paths or load resources from classpath (advanced).

---
**Next Step**: [Phase 5: Input System](phase5.md) – we’ll capture keyboard and mouse events and move the triangle with arrow keys.
