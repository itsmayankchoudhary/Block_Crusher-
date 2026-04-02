# Phase 8: Rendering Engine

## STEP 8: Drawing Hundreds of Sprites Efficiently

### 1. Goal
* Replace the single‑triangle renderer with a **batch renderer** that can draw hundreds of sprites in one OpenGL call.
* Add **texture support** – load PNG images and map them onto quads.
* Implement a **camera system** that converts world coordinates to screen coordinates.
* Introduce **simple shaders** for transformations and texture sampling.
* Achieve a stable 60 FPS with at least 1000 moving sprites.

### 2. Concept

#### Why Batch Rendering?
Calling `glDrawArrays` for each sprite is extremely inefficient (**“draw‑call overhead”**).  
**Batching** means we collect all sprite vertices into a single vertex buffer and issue **one draw call** per frame. This can improve performance by 10–100×.

#### Texture Mapping
A **texture** is a 2D image loaded into GPU memory. We map texture coordinates (`u,v` between 0 and 1) to each vertex, and the fragment shader samples the texture to determine the pixel color.

#### Camera & View Matrix
The camera defines what part of the game world is visible. We’ll use a **2D orthographic projection** that maps world coordinates to screen pixels. The camera can pan, zoom, and follow the player.

#### Vertex Buffer Object (VBO) with Dynamic Data
We’ll create a VBO with `GL_DYNAMIC_DRAW` that we update every frame with the latest positions of all sprites.

### 3. Implementation

#### Step 8.1 – Create a Texture Loader
Create `src/main/java/engine/graphics/Texture.java`:

```java
package engine.graphics;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.*;

import java.nio.ByteBuffer;

public class Texture {
    private final int id;
    private final int width, height;
    
    public Texture(String path) {
        int[] w = new int[1], h = new int[1], channels = new int[1];
        ByteBuffer image = stbi_load(path, w, h, channels, 4);
        if (image == null) {
            throw new RuntimeException("Failed to load texture: " + path);
        }
        width = w[0];
        height = h[0];
        
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                     GL_RGBA, GL_UNSIGNED_BYTE, image);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        stbi_image_free(image);
    }
    
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }
    
    public int getId() { return id; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
```

#### Step 8.2 – Create a Sprite Batch Renderer
Create `src/main/java/engine/graphics/SpriteBatch.java`:

```java
package engine.graphics;

import static org.lwjgl.opengl.GL33.*;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class SpriteBatch {
    private static final int MAX_SPRITES = 1000;
    private static final int VERTICES_PER_SPRITE = 4;
    private static final int INDICES_PER_SPRITE = 6;
    
    private Shader shader;
    private int vao, vbo, ibo;
    private List<Sprite> sprites = new ArrayList<>();
    private Matrix4f projectionMatrix = new Matrix4f();
    
    public static class Sprite {
        public float x, y, width, height;
        public Texture texture;
        public float r = 1.0f, g = 1.0f, b = 1.0f, a = 1.0f;
    }
    
    public void init(int screenWidth, int screenHeight) {
        // Orthographic projection (0,0 = top‑left, screenWidth,screenHeight = bottom‑right)
        projectionMatrix.setOrtho(0, screenWidth, screenHeight, 0, -1, 1);
        
        // Load shader with support for texture and projection matrix
        shader = new Shader(
            "src/main/resources/shaders/batch.vert",
            "src/main/resources/shaders/batch.frag"
        );
        
        // Generate buffers
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ibo = glGenBuffers();
        
        glBindVertexArray(vao);
        
        // Vertex buffer (will be updated dynamically)
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, MAX_SPRITES * VERTICES_PER_SPRITE * 8 * Float.BYTES, GL_DYNAMIC_DRAW);
        
        // Vertex layout: position (2), texCoord (2), color (4)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * Float.BYTES, 2 * Float.BYTES);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, 8 * Float.BYTES, 4 * Float.BYTES);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        
        // Index buffer (static, reused for all sprites)
        int[] indices = new int[MAX_SPRITES * INDICES_PER_SPRITE];
        for (int i = 0; i < MAX_SPRITES; i++) {
            int offset = i * 4;
            indices[i * 6 + 0] = offset + 0;
            indices[i * 6 + 1] = offset + 1;
            indices[i * 6 + 2] = offset + 2;
            indices[i * 6 + 3] = offset + 2;
            indices[i * 6 + 4] = offset + 3;
            indices[i * 6 + 5] = offset + 0;
        }
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        
        glBindVertexArray(0);
    }
    
    public void begin() {
        sprites.clear();
    }
    
    public void drawSprite(float x, float y, float width, float height, Texture texture) {
        Sprite sprite = new Sprite();
        sprite.x = x;
        sprite.y = y;
        sprite.width = width;
        sprite.height = height;
        sprite.texture = texture;
        sprites.add(sprite);
    }
    
    public void end() {
        if (sprites.isEmpty()) return;
        
        // Upload vertex data for all sprites
        float[] vertexData = new float[sprites.size() * VERTICES_PER_SPRITE * 8];
        int idx = 0;
        for (Sprite sprite : sprites) {
            // Four corners of the quad
            float x1 = sprite.x, x2 = sprite.x + sprite.width;
            float y1 = sprite.y, y2 = sprite.y + sprite.height;
            float[] quad = {
                x1, y1, 0, 0, sprite.r, sprite.g, sprite.b, sprite.a, // top‑left
                x2, y1, 1, 0, sprite.r, sprite.g, sprite.b, sprite.a, // top‑right
                x2, y2, 1, 1, sprite.r, sprite.g, sprite.b, sprite.a, // bottom‑right
                x1, y2, 0, 1, sprite.r, sprite.g, sprite.b, sprite.a  // bottom‑left
            };
            System.arraycopy(quad, 0, vertexData, idx, quad.length);
            idx += quad.length;
        }
        
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexData);
        
        // Draw
        shader.use();
        shader.setUniform("uProjection", projectionMatrix);
        
        glBindVertexArray(vao);
        glActiveTexture(GL_TEXTURE0);
        // Assume all sprites use the same texture for simplicity
        sprites.get(0).texture.bind();
        glDrawElements(GL_TRIANGLES, sprites.size() * INDICES_PER_SPRITE, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
```

#### Step 8.3 – Create Batch Shaders
**`batch.vert`**:

```glsl
#version 330 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec4 aColor;

uniform mat4 uProjection;

out vec2 vTexCoord;
out vec4 vColor;

void main() {
    gl_Position = uProjection * vec4(aPos, 0.0, 1.0);
    vTexCoord = aTexCoord;
    vColor = aColor;
}
```

**`batch.frag`**:

```glsl
#version 330 core

in vec2 vTexCoord;
in vec4 vColor;

uniform sampler2D uTexture;

out vec4 FragColor;

void main() {
    FragColor = texture(uTexture, vTexCoord) * vColor;
}
```

#### Step 8.4 – Integrate SpriteBatch into Main
Replace the old `Renderer` with `SpriteBatch`. Load a texture (e.g., a PNG file) and draw multiple sprites.

In `Main.java`:

```java
private static SpriteBatch spriteBatch;
private static Texture playerTexture;
private static Texture blockTexture;

// In main():
spriteBatch = new SpriteBatch();
spriteBatch.init(800, 600);
playerTexture = new Texture("src/main/resources/textures/player.png");
blockTexture = new Texture("src/main/resources/textures/block.png");

// In render():
spriteBatch.begin();
// Draw player
spriteBatch.drawSprite(playerX, playerY, 32, 32, playerTexture);
// Draw 100 blocks
for (int i = 0; i < 100; i++) {
    spriteBatch.drawSprite(i * 40, 300, 32, 32, blockTexture);
}
spriteBatch.end();
```

#### Step 8.5 – Add a Camera
Create a `Camera` class that tracks an offset and zoom, and updates the projection matrix accordingly.

```java
package engine.graphics;

import org.joml.Matrix4f;

public class Camera {
    private float x, y;
    private float zoom = 1.0f;
    private int viewportWidth, viewportHeight;
    
    public Camera(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public Matrix4f getProjection() {
        Matrix4f projection = new Matrix4f();
        float left = x - viewportWidth / (2.0f * zoom);
        float right = x + viewportWidth / (2.0f * zoom);
        float bottom = y + viewportHeight / (2.0f * zoom);
        float top = y - viewportHeight / (2.0f * zoom);
        projection.setOrtho(left, right, bottom, top, -1, 1);
        return projection;
    }
}
```

Update `SpriteBatch` to accept a camera matrix instead of a fixed orthographic matrix.

### 4. Code
New files:
* `src/main/java/engine/graphics/Texture.java`
* `src/main/java/engine/graphics/SpriteBatch.java`
* `src/main/java/engine/graphics/Camera.java`
* `src/main/resources/shaders/batch.vert`
* `src/main/resources/shaders/batch.frag`

Modified files:
* `src/main/java/engine/Main.java`

### 5. Output
* A window filled with hundreds of textured sprites (blocks) and a player sprite.
* The sprites are drawn with a single draw call, achieving high performance.
* The camera can be moved (e.g., follow the player) by updating its position.
* The frame rate should stay close to 60 FPS even with 1000 sprites.

### 6. Common Errors
* **Texture appears black** – Ensure the texture file path is correct and the image is loaded with the correct number of channels (STBI loads 4 for RGBA). Check that `glActiveTexture` is set and the uniform `uTexture` is bound to texture unit 0.
* **Sprites appear upside down** – OpenGL’s texture coordinate origin is bottom‑left, but we’re using top‑left. Adjust the texCoords in `drawSprite` accordingly.
* **Memory leak** – Remember to delete textures and buffers when the game ends (`glDeleteTextures`, `glDeleteBuffers`).
* **Batch size limit** – The `MAX_SPRITES` constant limits how many sprites can be drawn in one batch. If you need more, split into multiple batches or increase the limit.

---
**Next Step**: [Phase 9: Asset Management](phase9.md) – we’ll build a resource manager that caches textures, sounds, and fonts, and supports hot‑reloading.
