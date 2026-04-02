# Phase 7: Physics System

## STEP 7: Adding Motion and Collision

### 1. Goal
* Introduce **velocity** and **acceleration** to move entities automatically.
* Implement **Axis‑Aligned Bounding Box (AABB)** collision detection.
* Add **gravity** and simple **bouncing**.
* Create a `PhysicsSystem` that updates positions and resolves collisions.
* Test with two rectangles that collide and bounce apart.

### 2. Concept

#### Newton’s Laws (Simplified)
* **Velocity** – change in position per second (`pixels/second`).
* **Acceleration** – change in velocity per second (`pixels/second²`). Gravity is a constant downward acceleration.
* **Euler Integration** – we approximate motion as:
  ```
  velocity += acceleration * deltaTime
  position += velocity * deltaTime
  ```

#### Collision Detection – AABB
Two axis‑aligned rectangles collide if:
* `rectA.right >= rectB.left` AND
* `rectA.left <= rectB.right` AND
* `rectA.bottom >= rectB.top` AND
* `rectA.top <= rectB.bottom`

(We’ll use Y‑up coordinate system for simplicity.)

#### Collision Resolution
When two rectangles intersect, we need to **push them apart**. The simplest method is to find the smallest overlap axis (horizontal or vertical) and move each rectangle half the overlap distance away.

### 3. Implementation

#### Step 7.1 – Create Velocity and Collision Components
Create `src/main/java/engine/ecs/VelocityComponent.java`:

```java
package engine.ecs;

public class VelocityComponent implements Component {
    public float vx = 0.0f;
    public float vy = 0.0f;
}
```

Create `src/main/java/engine/ecs/CollisionComponent.java`:

```java
package engine.ecs;

public class CollisionComponent implements Component {
    public float width, height; // half‑widths (extents)
    
    public CollisionComponent(float width, float height) {
        this.width = width;
        this.height = height;
    }
}
```

#### Step 7.2 – Write the Physics System
Create `src/main/java/engine/ecs/PhysicsSystem.java`:

```java
package engine.ecs;

import java.util.Set;

public class PhysicsSystem implements System {
    private static final float GRAVITY = -0.0005f; // small downward acceleration
    
    @Override
    public void process(EntityManager entityManager, float deltaTime) {
        // Apply gravity to all entities with velocity
        Set<Integer> velocityEntities = entityManager.getEntitiesWith(VelocityComponent.class);
        for (int entity : velocityEntities) {
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);
            vel.vy += GRAVITY * deltaTime;
        }
        
        // Update positions based on velocity
        Set<Integer> movingEntities = entityManager.getEntitiesWith(TransformComponent.class);
        movingEntities.retainAll(entityManager.getEntitiesWith(VelocityComponent.class));
        for (int entity : movingEntities) {
            TransformComponent transform = entityManager.getComponent(entity, TransformComponent.class);
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);
            transform.x += vel.vx * deltaTime;
            transform.y += vel.vy * deltaTime;
        }
        
        // Detect collisions between entities that have both Transform and Collision components
        Set<Integer> collidableEntities = entityManager.getEntitiesWith(TransformComponent.class);
        collidableEntities.retainAll(entityManager.getEntitiesWith(CollisionComponent.class));
        Integer[] entities = collidableEntities.toArray(new Integer[0]);
        for (int i = 0; i < entities.length; i++) {
            for (int j = i + 1; j < entities.length; j++) {
                resolveCollision(entityManager, entities[i], entities[j]);
            }
        }
    }
    
    private void resolveCollision(EntityManager em, int a, int b) {
        TransformComponent ta = em.getComponent(a, TransformComponent.class);
        CollisionComponent ca = em.getComponent(a, CollisionComponent.class);
        TransformComponent tb = em.getComponent(b, TransformComponent.class);
        CollisionComponent cb = em.getComponent(b, CollisionComponent.class);
        
        // AABB collision check
        float dx = ta.x - tb.x;
        float dy = ta.y - tb.y;
        float combinedHalfWidths = ca.width + cb.width;
        float combinedHalfHeights = ca.height + cb.height;
        
        if (Math.abs(dx) < combinedHalfWidths && Math.abs(dy) < combinedHalfHeights) {
            // Collision! Compute overlap on each axis
            float overlapX = combinedHalfWidths - Math.abs(dx);
            float overlapY = combinedHalfHeights - Math.abs(dy);
            
            // Resolve along the smallest overlap axis
            if (overlapX < overlapY) {
                float sign = dx > 0 ? 1 : -1;
                ta.x += sign * overlapX * 0.5f;
                tb.x -= sign * overlapX * 0.5f;
                
                // Bounce horizontally (optional)
                VelocityComponent va = em.getComponent(a, VelocityComponent.class);
                VelocityComponent vb = em.getComponent(b, VelocityComponent.class);
                if (va != null) va.vx = -va.vx * 0.8f; // elasticity
                if (vb != null) vb.vx = -vb.vx * 0.8f;
            } else {
                float sign = dy > 0 ? 1 : -1;
                ta.y += sign * overlapY * 0.5f;
                tb.y -= sign * overlapY * 0.5f;
                
                VelocityComponent va = em.getComponent(a, VelocityComponent.class);
                VelocityComponent vb = em.getComponent(b, VelocityComponent.class);
                if (va != null) va.vy = -va.vy * 0.8f;
                if (vb != null) vb.vy = -vb.vy * 0.8f;
            }
        }
    }
}
```

#### Step 7.3 – Update Entity Creation
In `Main.java`, after creating the entity manager, add a second entity (a rectangle) and give both entities velocity and collision components.

```java
// First entity (player‑controlled triangle)
int triangle = entityManager.createEntity();
entityManager.addComponent(triangle, new TransformComponent(0.0f, 0.0f));
entityManager.addComponent(triangle, new RenderComponent());
entityManager.addComponent(triangle, new VelocityComponent());
entityManager.addComponent(triangle, new CollisionComponent(0.1f, 0.1f));

// Second entity (falling rectangle)
int block = entityManager.createEntity();
entityManager.addComponent(block, new TransformComponent(0.3f, 0.5f));
RenderComponent blockColor = new RenderComponent();
blockColor.color.set(0.0f, 0.5f, 1.0f); // blue
entityManager.addComponent(block, blockColor);
entityManager.addComponent(block, new VelocityComponent());
entityManager.addComponent(block, new CollisionComponent(0.15f, 0.15f));
```

#### Step 7.4 – Modify Rendering to Draw Rectangles
Our current `Renderer` only draws a triangle. For simplicity, we’ll keep using triangles but we can change the shader to draw rectangles by scaling. Alternatively, we can create a `MeshComponent` that stores shape data. That’s too much for this step.

Instead, we’ll just draw both entities as triangles (same shape) but at different positions and colors. Update `Renderer.render()` to accept a list of entities and draw each.

Create a `RenderingSystem` (optional) or modify `Main.render()` to iterate over entities with `TransformComponent` and `RenderComponent`.

We’ll leave that as an exercise for the reader, but here’s a quick sketch:

```java
Set<Integer> renderables = entityManager.getEntitiesWith(TransformComponent.class);
renderables.retainAll(entityManager.getEntitiesWith(RenderComponent.class));
for (int entity : renderables) {
    TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
    RenderComponent r = entityManager.getComponent(entity, RenderComponent.class);
    // Set shader color uniform based on r.color
    // Draw triangle at (t.x, t.y)
}
```

#### Step 7.5 – Integrate Physics System
Add the `PhysicsSystem` to `Main` and call it in `update()`:

```java
private static PhysicsSystem physicsSystem;

// In main():
physicsSystem = new PhysicsSystem();

// In update():
physicsSystem.process(entityManager, 1.0f / 60.0f);
```

Remove the manual movement from `update()` (or keep it for player input). Instead, we can set the triangle’s velocity based on input:

```java
VelocityComponent playerVel = entityManager.getComponent(triangle, VelocityComponent.class);
if (playerVel != null) {
    playerVel.vx = 0.0f;
    if (Input.isRightPressed()) playerVel.vx += 0.01f;
    if (Input.isLeftPressed())  playerVel.vx -= 0.01f;
    // (vertical control optional)
}
```

### 4. Code
New files:
* `src/main/java/engine/ecs/VelocityComponent.java`
* `src/main/java/engine/ecs/CollisionComponent.java`
* `src/main/java/engine/ecs/PhysicsSystem.java`

Modified files:
* `src/main/java/engine/Main.java`
* `src/main/java/engine/graphics/Renderer.java` (optional)

### 5. Output
* Two colored triangles (or rectangles) appear on screen.
* The second triangle falls due to gravity.
* When they collide, they bounce apart (with some energy loss).
* The player‑controlled triangle can be moved left/right with arrow keys, pushing the other triangle.

### 6. Common Errors
* **Objects pass through each other** – The time step may be too large; reduce `deltaTime` or implement **continuous collision detection**.
* **Jittery collisions** – The resolution pushes objects apart but next frame gravity pulls them together again, causing vibration. Add a small epsilon or disable gravity after collision.
* **Performance issues with many entities** – The double loop is O(n²). Use spatial partitioning (grid, quad‑tree) for production engines.
* **Wrong coordinate system** – Ensure Y‑axis direction matches your rendering (OpenGL NDC is Y‑up, but our gravity uses Y‑down). Adjust signs accordingly.

---
**Next Step**: [Phase 8: Rendering Engine](phase8.md) – we’ll replace the single‑triangle renderer with a batch renderer that can draw hundreds of sprites efficiently.
