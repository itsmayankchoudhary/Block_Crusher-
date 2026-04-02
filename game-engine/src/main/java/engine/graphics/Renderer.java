package engine.graphics;

import static org.lwjgl.opengl.GL33.*;

public class Renderer {
    private Shader shader;
    private int vao;
    private int vbo;
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    
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
    
    public void setOffset(float dx, float dy) {
        offsetX = dx;
        offsetY = dy;
    }
    
    public void render() {
        // Clear the screen with dark gray
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        // Use our shader
        shader.use();
        
        // Set offset uniform
        int offsetLoc = glGetUniformLocation(shader.getId(), "uOffset");
        glUniform2f(offsetLoc, offsetX, offsetY);
        
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