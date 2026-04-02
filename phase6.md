# Phase 6: Entity Component System (ECS)

## STEP 6: Building a Flexible Object Architecture

### 1. Goal
* Understand the **Entity‑Component‑System** pattern and its advantages over inheritance.
* Implement a simple ECS from scratch.
* Create **entities** that can hold multiple **components**.
* Write **systems** that process components (e.g., a `MovementSystem` that updates positions).
* Replace the hard‑coded triangle with an entity that has a `TransformComponent` and a `RenderComponent`.

### 2. Concept

#### What is ECS?
ECS is a architectural pattern where:
* **Entity** – A unique identifier (usually an integer). It’s just a container for components.
* **Component** – A piece of data (position, velocity, color). Components have no logic.
* **System** – Logic that runs on entities that have a specific set of components.

Think of it like a **play**:
* **Entity** = an actor on stage.
* **Component** = costume, props, script lines (data the actor carries).
* **System** = director who tells all actors wearing a “red costume” to move left.

#### Why ECS over Inheritance?
* Inheritance creates deep, rigid hierarchies (e.g., `Player extends GameObject extends Sprite`).
* Adding a new feature (like “can fly”) requires modifying the class hierarchy or using multiple inheritance (which Java doesn’t have).
* ECS is **compositional** – you can give any entity a `FlightComponent` without touching its class.

#### Our ECS Design
We’ll keep it simple:
* **Entity** – just an `int` ID.
* **Component** – a plain Java class tagged with an interface.
* **ComponentStore** – maps entity IDs to component instances.
* **System** – a `process()` method that iterates over entities with required components.

### 3. Implementation

#### Step 6.1 – Define the Component Interface
Create `src/main/java/engine/ecs/Component.java`:

```java
package engine.ecs;

/**
 * Marker interface for all components.
 * A component is a plain data container with no behavior.
 */
public interface Component {
    // No methods required – just a tag.
}
```

#### Step 6.2 – Create a Few Concrete Components
**TransformComponent** – holds position, rotation, scale.

```java
package engine.ecs;

public class TransformComponent implements Component {
    public float x, y;
    public float rotation; // degrees
    public float scaleX = 1.0f, scaleY = 1.0f;
    
    public TransformComponent(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
```

**RenderComponent** – holds a reference to a mesh (for now, just a color).

```java
package engine.ecs;

import org.joml.Vector3f;

public class RenderComponent implements Component {
    public Vector3f color = new Vector3f(1.0f, 0.0f, 0.0f); // default red
}
```

#### Step 6.3 – Create the Entity Manager
Create `src/main/java/engine/ecs/EntityManager.java`:

```java
package engine.ecs;

import java.util.*;

public class EntityManager {
    private int nextEntityId = 0;
    private Map<Class<? extends Component>, Map<Integer, Component>> componentStores = new HashMap<>();
    
    public int createEntity() {
        return nextEntityId++;
    }
    
    public void addComponent(int entity, Component component) {
        componentStores
            .computeIfAbsent(component.getClass(), k -> new HashMap<>())
            .put(entity, component);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(int entity, Class<T> componentClass) {
        Map<Integer, Component> store = componentStores.get(componentClass);
        if (store == null) return null;
        return (T) store.get(entity);
    }
    
    public <T extends Component> Set<Integer> getEntitiesWith(Class<T> componentClass) {
        Map<Integer, Component> store = componentStores.get(componentClass);
        if (store == null) return Collections.emptySet();
        return store.keySet();
    }
    
    public <T extends Component> boolean hasComponent(int entity, Class<T> componentClass) {
        Map<Integer, Component> store = componentStores.get(componentClass);
        return store != null && store.containsKey(entity);
    }
}
```

#### Step 6.4 – Define the System Interface
Create `src/main/java/engine/ecs/System.java`:

```java
package engine.ecs;

/**
 * A system processes entities that have a specific set of components.
 */
public interface System {
    void process(EntityManager entityManager, float deltaTime);
}
```

#### Step 6.5 – Implement a Movement System
Create `src/main/java/engine/ecs/MovementSystem.java`:

```java
package engine.ecs;

public class MovementSystem implements System {
    @Override
    public void process(EntityManager entityManager, float deltaTime) {
        // Get all entities that have both TransformComponent and VelocityComponent
        // (We haven’t created VelocityComponent yet – you can add it as an exercise)
        // For now, just move entities with TransformComponent based on input.
        
        Set<Integer> entities = entityManager.getEntitiesWith(TransformComponent.class);
        for (int entity : entities) {
            TransformComponent transform = entityManager.getComponent(entity, TransformComponent.class);
            // Example: move right if right arrow is pressed
            // (We’ll integrate input in the next step)
        }
    }
}
```

#### Step 6.6 – Integrate ECS with the Existing Engine
Modify `Main.java` to create an entity manager, add a test entity, and run systems each update.

Add these fields to `Main`:

```java
private static EntityManager entityManager;
private static MovementSystem movementSystem;
```

In `main()`, after initializing the renderer:

```java
// Initialize ECS
entityManager = new EntityManager();
movementSystem = new MovementSystem();

// Create a test entity
int triangle = entityManager.createEntity();
entityManager.addComponent(triangle, new TransformComponent(0.0f, 0.0f));
entityManager.addComponent(triangle, new RenderComponent());
```

Update the `update()` method to run systems:

```java
private static void update() {
    // Run systems
    movementSystem.process(entityManager, 1.0f / 60.0f); // fixed delta
    
    // Input‑based movement (temporary)
    float speed = 0.01f;
    Set<Integer> entities = entityManager.getEntitiesWith(TransformComponent.class);
    for (int entity : entities) {
        TransformComponent transform = entityManager.getComponent(entity, TransformComponent.class);
        if (Input.isRightPressed()) transform.x += speed;
        if (Input.isLeftPressed())  transform.x -= speed;
        if (Input.isUpPressed())    transform.y += speed;
        if (Input.isDownPressed())  transform.y -= speed;
    }
}
```

#### Step 6.7 – Render Entities
Create a `RenderingSystem` that draws each entity with a `TransformComponent` and `RenderComponent`.

First, update `Renderer` to accept a list of transforms and colors (or refactor to draw multiple triangles). For simplicity, we’ll keep drawing a single triangle but move it according to the entity’s transform.

Modify `Renderer` to accept a position:

```java
public void render(float x, float y) {
    // ... clear screen, use shader ...
    int offsetLoc = glGetUniformLocation(shader.getId(), "uOffset");
    glUniform2f(offsetLoc, x, y);
    // ... draw
}
```

Then in `Main.render()`:

```java
// Get the first entity (assuming only one)
Set<Integer> entities = entityManager.getEntitiesWith(TransformComponent.class);
if (!entities.isEmpty()) {
    int entity = entities.iterator().next();
    TransformComponent transform = entityManager.getComponent(entity, TransformComponent.class);
    renderer.render(transform.x, transform.y);
}
```

### 4. Code
New files:
* `src/main/java/engine/ecs/Component.java`
* `src/main/java/engine/ecs/TransformComponent.java`
* `src/main/java/engine/ecs/RenderComponent.java`
* `src/main/java/engine/ecs/EntityManager.java`
* `src/main/java/engine/ecs/System.java`
* `src/main/java/engine/ecs/MovementSystem.java`

Modified files:
* `src/main/java/engine/Main.java`
* `src/main/java/engine/graphics/Renderer.java`

### 5. Output
* The same red triangle as before, but now it’s represented as an **entity** with components.
* The triangle can be moved with arrow keys (the movement is now driven by the ECS).
* The engine is now **data‑driven** – adding new game objects is as simple as creating entities and attaching components.

### 6. Common Errors
* **`ClassCastException` when retrieving components** – Ensure you store components with the exact class you later request (`TransformComponent.class`).
* **Entities not appearing** – Verify that the entity has both `TransformComponent` and `RenderComponent`.
* **Poor performance with many entities** – Our naive `HashMap`‑based storage is fine for hundreds of entities. For thousands, consider using archetype‑based ECS (advanced).
* **Components not being found** – Double‑check that you added the component to the correct entity ID.

---
**Next Step**: [Phase 7: Physics System](phase7.md) – we’ll add a `VelocityComponent` and a `PhysicsSystem` that applies gravity and detects collisions between entities.
