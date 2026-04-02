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