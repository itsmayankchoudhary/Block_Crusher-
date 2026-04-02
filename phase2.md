# Phase 2: Project Setup

## STEP 2: Creating the Foundation

### 1. Goal
* Set up a **Gradle‑based Java project** with LWJGL dependencies.
* Create the **folder structure** that will house our engine modules.
* Write a **“Hello Window”** program that opens a blank GLFW window.
* Verify that the build works and the window appears.

### 2. Concept

#### Why Gradle?
* **Modern build tool** – widely used in the Java ecosystem.
* **Dependency management** – we can declare LWJGL as a dependency and Gradle will download the correct native libraries for each platform.
* **Easy to extend** – later we can add tasks for packaging, shading, or running tests.

#### LWJGL Dependencies
LWJGL is modular – we only need the modules we’ll use in V1:
* **`lwjgl‑glfw`** – window and input.
* **`lwjgl‑opengl`** – OpenGL bindings for rendering.
* **`lwjgl‑openal`** – audio playback.
* **`lwjgl‑stb`** – image loading (PNG, JPEG).

Each module requires its own **native library** (`.dll` on Windows, `.so` on Linux, `.dylib` on macOS). Gradle’s LWJGL plugin automatically downloads the correct natives for the current operating system.

#### Project Layout
We’ll adopt a **package‑by‑feature** structure inside `src/main/java/engine/`. This keeps related code together and makes the engine easier to navigate.

```
game‑engine/
├── build.gradle.kts          # Gradle build script (Kotlin DSL)
├── settings.gradle.kts       # Gradle settings
├── gradle.properties         # Optional properties
├── src/main/java/engine/     # All engine source code
│   ├── core/                 # Core systems (game loop, window)
│   ├── graphics/             # Rendering
│   ├── input/                # Keyboard/mouse handling
│   ├── ecs/                  # Entity‑Component‑System
│   ├── physics/              # Collision and movement
│   ├── assets/               # Asset loading and caching
│   ├── audio/                # Sound playback
│   ├── debug/                # Debugging tools
│   └── scenes/               # Scene management
├── src/main/resources/       # Shaders, textures, sounds
└── src/test/java/            # Unit tests (optional)
```

### 3. Implementation

#### Step 2.1 – Create the Project Directory
Open a terminal in your workspace (`c:/Users/itsma/Experiment/EX1`) and run:

```cmd
mkdir game‑engine
cd game‑engine
```

#### Step 2.2 – Initialize Gradle
Create the Gradle wrapper and basic configuration files.

**`settings.gradle.kts`** – defines the project name.
```kotlin
rootProject.name = "game‑engine"
```

**`build.gradle.kts`** – the main build script.
```kotlin
plugins {
    java
    application
}

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.3"
val jomlVersion = "1.10.5"

dependencies {
    // LWJGL core
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-openal")
    implementation("org.lwjgl:lwjgl-stb")
    
    // LWJGL natives – Gradle picks the right OS automatically
    runtimeOnly("org.lwjgl:lwjgl::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-openal::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-stb::natives-windows")
    
    // Math library for vectors and matrices
    implementation("org.joml:joml:${jomlVersion}")
}

application {
    mainClass.set("engine.Main")
}

tasks.compileJava {
    options.release.set(17)
}
```

#### Step 2.3 – Create the Source Folder Structure
Inside `game‑engine/`, create the directories:

```cmd
mkdir -p src/main/java/engine/core
mkdir -p src/main/java/engine/graphics
mkdir -p src/main/java/engine/input
mkdir -p src/main/java/engine/ecs
mkdir -p src/main/java/engine/physics
mkdir -p src/main/java/engine/assets
mkdir -p src/main/java/engine/audio
mkdir -p src/main/java/engine/debug
mkdir -p src/main/java/engine/scenes
mkdir -p src/main/resources
mkdir -p src/test/java
```

*(On Windows, use `mkdir src\main\java\engine\core` etc.)*

#### Step 2.4 – Write the “Hello Window” Program
Create `src/main/java/engine/Main.java`:

```java
package engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Game Engine V1 – Starting window...");
        
        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        
        // Configure window hints
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Hide initially
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        
        // Create a 800×600 window
        long window = glfwCreateWindow(800, 600, "Game Engine Window", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }
        
        // Center the window on the screen
        var vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
            window,
            (vidMode.width() - 800) / 2,
            (vidMode.height() - 600) / 2
        );
        
        // Show the window
        glfwShowWindow(window);
        
        // Main loop – keep window open until user closes it
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();  // Process input events
        }
        
        // Clean up
        glfwDestroyWindow(window);
        glfwTerminate();
        System.out.println("Window closed. Engine shutdown.");
    }
}
```

#### Step 2.5 – Build and Run
From the `game‑engine` directory, execute:

```cmd
gradlew run
```

If you don’t have Gradle installed, the wrapper (`gradlew`) will download it automatically. The command will compile the project, download LWJGL dependencies, and launch the window.

### 4. Code
The full code for this phase consists of:
* `settings.gradle.kts` – 1 line.
* `build.gradle.kts` – ~40 lines.
* `engine/Main.java` – ~40 lines.

All files are shown in the implementation steps above.

### 5. Output
* A **blank window** titled “Game Engine Window” should appear, centered on the screen.
* The window should be **800×600 pixels**, resizable, and close when you click the “X” button.
* In the terminal, you should see the messages:
  ```
  Game Engine V1 – Starting window...
  Window closed. Engine shutdown.
  ```

*If the window appears and closes cleanly, congratulations – you have a working foundation!*

### 6. Common Errors
* **`UnsatisfiedLinkError`** – The native libraries aren’t found. Ensure the `runtimeOnly` dependencies in `build.gradle.kts` match your operating system (e.g., `natives‑windows`, `natives‑linux`, `natives‑macos`).
* **`GLFW failed to initialize`** – Usually a graphics driver issue. Update your GPU drivers, or try running on a different machine.
* **`No main class found`** – Check that `mainClass.set("engine.Main")` in `build.gradle.kts` matches your package and class name exactly.
* **`Gradle not found`** – Use `./gradlew` (or `gradlew.bat` on Windows) instead of the global `gradle` command.

---
**Next Step**: [Phase 3: Core System - Game Loop](phase3.md) – we’ll replace the simple while‑loop with a proper fixed‑timestep game loop that updates and renders at 60 FPS.
