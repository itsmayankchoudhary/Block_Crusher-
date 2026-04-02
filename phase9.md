# Phase 9: Asset Management

## STEP 9: Loading and Caching Game Resources

### 1. Goal
* Create a centralized **AssetManager** that loads and caches textures, sounds, and fonts.
* Prevent duplicate loading – the same file should be loaded only once.
* Support **reference counting** so that assets can be released when no longer used.
* Add **hot‑reloading** (optional) – automatically reload a texture when the file changes on disk.
* Integrate asset management with the existing rendering and audio systems.

### 2. Concept

#### Why an Asset Manager?
Games use hundreds of assets (images, sounds, fonts). Loading them from disk every time they’re needed is slow and wastes memory. An asset manager:
* **Caches** – stores loaded assets in a hash map.
* **Reference counting** – tracks how many game objects are using an asset; when count reaches zero, the asset can be unloaded.
* **Error handling** – provides a default fallback (e.g., a pink placeholder) when a file is missing.

#### Asset Types
* **Textures** – PNG, JPEG images used for sprites.
* **Sounds** – WAV, MP3, OGG files for sound effects and music.
* **Fonts** – TTF, OTF files for rendering text.
* **Shaders** – GLSL source files (already handled by our `Shader` class).

#### Hot‑Reloading
During development, you can modify a texture in an external editor and have the game automatically reload it without restarting. This is done by watching the file system for changes.

### 3. Implementation

#### Step 9.1 – Define the Asset Interface
Create `src/main/java/engine/assets/Asset.java`:

```java
package engine.assets;

/**
 * Base interface for all loadable assets.
 */
public interface Asset {
    /** Unique identifier (usually the file path). */
    String getId();
    
    /** Releases any native resources (OpenGL textures, OpenAL buffers). */
    void dispose();
}
```

#### Step 9.2 – Implement a Texture Asset
Create `src/main/java/engine/assets/TextureAsset.java`:

```java
package engine.assets;

import engine.graphics.Texture;

public class TextureAsset implements Asset {
    private final String id;
    private final Texture texture;
    private int refCount = 0;
    
    public TextureAsset(String id, String path) {
        this.id = id;
        this.texture = new Texture(path);
    }
    
    @Override public String getId() { return id; }
    
    public Texture getTexture() { return texture; }
    
    public void retain() { refCount++; }
    public void release() {
        if (--refCount <= 0) {
            texture.dispose(); // we need to add a dispose method to Texture
        }
    }
    
    @Override public void dispose() {
        texture.dispose();
    }
}
```

#### Step 9.3 – Create the Asset Manager
Create `src/main/java/engine/assets/AssetManager.java`:

```java
package engine.assets;

import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private final Map<String, Asset> assets = new HashMap<>();
    
    /**
     * Loads a texture from the given path and caches it.
     * If the texture is already loaded, returns the cached instance.
     */
    public TextureAsset loadTexture(String id, String path) {
        Asset existing = assets.get(id);
        if (existing instanceof TextureAsset) {
            TextureAsset tex = (TextureAsset) existing;
            tex.retain();
            return tex;
        }
        
        TextureAsset newAsset = new TextureAsset(id, path);
        newAsset.retain();
        assets.put(id, newAsset);
        return newAsset;
    }
    
    /**
     * Releases an asset. When its reference count reaches zero,
     * the asset is removed from the cache and disposed.
     */
    public void release(String id) {
        Asset asset = assets.get(id);
        if (asset == null) return;
        
        if (asset instanceof TextureAsset) {
            TextureAsset tex = (TextureAsset) asset;
            tex.release();
            if (tex.getRefCount() <= 0) {
                assets.remove(id);
                tex.dispose();
            }
        }
        // handle other asset types similarly
    }
    
    /** Unloads all assets, regardless of reference count. */
    public void clear() {
        assets.values().forEach(Asset::dispose);
        assets.clear();
    }
}
```

#### Step 9.4 – Add Sound Support (Optional)
Using LWJGL’s OpenAL bindings, we can load WAV files. Create `SoundAsset` and extend the asset manager.

First, add OpenAL to dependencies (already present in our `build.gradle.kts`). Then create a `Sound` wrapper similar to `Texture`.

#### Step 9.5 – Integrate with the Rendering System
Modify `SpriteBatch` to accept `TextureAsset` instead of raw `Texture`. This ensures the reference count is maintained.

Update `Main.java`:

```java
private static AssetManager assetManager;
private static TextureAsset playerTex;

// In main():
assetManager = new AssetManager();
playerTex = assetManager.loadTexture("player", "src/main/resources/textures/player.png");

// In render():
spriteBatch.drawSprite(x, y, width, height, playerTex.getTexture());

// When the game ends:
assetManager.clear();
```

#### Step 9.6 – Hot‑Reloading (Advanced)
Add a file watcher that monitors the resources directory and reloads assets when they change. This is optional but great for development.

Create `src/main/java/engine/assets/FileWatcher.java`:

```java
package engine.assets;

import java.nio.file.*;

public class FileWatcher implements Runnable {
    private final AssetManager manager;
    private final Path resourcesPath;
    
    public FileWatcher(AssetManager manager, String resourcesDir) {
        this.manager = manager;
        this.resourcesPath = Paths.get(resourcesDir);
    }
    
    @Override
    public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            resourcesPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        Path changed = (Path) event.context();
                        String id = changed.toString();
                        // Reload the asset (implementation depends on mapping file name to asset id)
                        manager.reload(id);
                    }
                }
                key.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

#### Step 9.7 – Default Fallback Assets
When a requested asset cannot be loaded, the asset manager should provide a built‑in fallback (e.g., a pink‑and‑black checkerboard texture). This prevents crashes and makes missing assets obvious.

Add a static method `createFallbackTexture()` that generates a simple texture in code.

### 4. Code
New files:
* `src/main/java/engine/assets/Asset.java`
* `src/main/java/engine/assets/TextureAsset.java`
* `src/main/java/engine/assets/AssetManager.java`
* `src/main/java/engine/assets/FileWatcher.java` (optional)

Modified files:
* `src/main/java/engine/graphics/Texture.java` – add `dispose()` method.
* `src/main/java/engine/graphics/SpriteBatch.java` – accept `TextureAsset`.
* `src/main/java/engine/Main.java` – integrate asset manager.

### 5. Output
* Textures are loaded only once, no matter how many sprites use them.
* Memory is freed when an asset is no longer referenced (e.g., after switching levels).
* If you edit a texture file while the game is running, the texture updates automatically (if hot‑reloading is enabled).
* Missing assets show a distinctive fallback texture instead of crashing.

### 6. Common Errors
* **Memory leak** – Forgetting to call `release()` leads to assets never being unloaded. Use try‑with‑resources pattern or make assets `AutoCloseable`.
* **Thread safety** – The asset manager is accessed from both the main thread and the file‑watcher thread. Use `ConcurrentHashMap` and synchronize reference counts.
* **File path issues** – Relative paths depend on the working directory. Use `ClassLoader.getResource()` for assets inside JARs, or absolute paths for development.
* **OpenGL context** – Textures must be created and disposed on the thread that owns the OpenGL context (the main thread). The file watcher should only set a “dirty” flag and let the main thread reload the texture during the next frame.

---
**Next Step**: [Phase 10: Scene Management](phase10.md) – we’ll organize the game into scenes (menu, level, pause) and allow seamless switching between them.
