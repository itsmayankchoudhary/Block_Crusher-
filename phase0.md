# Phase 0: Developer Mindset

## STEP 0: Understanding What a Game Engine Really Is

### 1. Goal
* Establish a clear mental model of a game engine.
* Differentiate between a **game** and an **engine**.
* Understand the core responsibilities of an engine.

### 2. Concept

#### What is a Game Engine?
A game engine is a **software framework** that provides the fundamental building blocks needed to create a game. Think of it as a **car factory** – the factory doesn’t produce a single car model; instead, it provides assembly lines, robotic arms, painting stations, and quality‑control systems that can be used to manufacture many different kinds of cars.

Similarly, a game engine gives you:
* A **window** to draw graphics
* A **game loop** that runs 60 times per second
* Tools to **load images, sounds, and fonts**
* Systems to **handle keyboard/mouse input**
* **Physics** and **collision** detection
* **Scene management** to organize levels

#### Game vs Engine – The Analogy
* **Game** – A specific product built with the engine. Example: *Super Mario Bros.* is a game.
* **Engine** – The toolkit used to build that game. Example: *Unity* or *Unreal Engine*.

If you were building a house:
* **Engine** = Power tools, cement mixer, scaffolding, blueprint software.
* **Game** = The actual house you build with those tools.

#### Core Responsibilities
Every game engine, no matter how simple, must handle these five tasks:

1. **Windowing** – Create a native window (or full‑screen display).
2. **Game Loop** – Update game logic and render graphics at a consistent rate.
3. **Rendering** – Draw shapes, sprites, text, and effects on the screen.
4. **Input** – Capture keyboard, mouse, or gamepad events.
5. **Asset Management** – Load and manage external files (images, sounds, fonts).

### 3. Implementation
This phase is purely conceptual – no code yet. However, we will prepare our mindset by answering three key questions:

1. **What will our engine do?**
   * Provide a 2D rendering pipeline.
   * Handle user input.
   * Manage game objects via an Entity‑Component‑System.
   * Detect collisions and apply simple physics.
   * Play sound effects and music.

2. **What will our engine NOT do?**
   * It will **not** be a 3D engine (we start with 2D).
   * It will **not** include advanced features like networking, AI, or particle systems in V1.
   * It will **not** be a drag‑and‑drop editor (we’ll code games programmatically).

3. **What is our development philosophy?**
   * **Learn first, optimize later** – We’ll write clear, understandable code before we micro‑optimize.
   * **Modularity** – Each system (graphics, input, physics) will be isolated and replaceable.
   * **Test with a real game** – We’ll validate the engine by building a small game at the end.

### 4. Code
No code in this step – we’re setting the foundation.

### 5. Output
* A clear mental model of the engine’s purpose.
* Understanding of the separation between engine and game.
* Readiness to move on to **Phase 1: Scope Definition**.

### 6. Common Errors
* **Confusing the engine with a game** – Remember, the engine is reusable; the game is a single product.
* **Over‑scoping** – Trying to build a “Unity killer” on day one. Start small, iterate.
* **Under‑scoping** – Skipping essential systems (like a game loop) because they seem “too simple.”

---
**Next Step**: [Phase 1: Scope Definition](phase1.md) – where we decide exactly what our engine will and won’t include.
