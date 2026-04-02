# Phase 11: Audio System

## STEP 11: Adding Sound Effects and Music

### 1. Goal
* Set up **OpenAL** for 2D audio playback.
* Load **WAV** and **OGG** files (sound effects and music).
* Play **one‑shot sounds** (e.g., jump, shoot) and **looping background music**.
* Implement a simple **audio manager** that caches sounds and controls volume.
* Add **spatial audio** (optional) – sounds that change volume/pan based on listener position.

### 2. Concept

#### OpenAL Basics
OpenAL is a cross‑platform audio API similar to OpenGL. It works with **sources** (emitters) and **buffers** (sound data). The **listener** represents the player’s ears.

* **Buffer** – holds raw PCM audio data (loaded from a file).
* **Source** – a playing instance that references a buffer. You can set its pitch, gain (volume), and looping.
* **Listener** – a single global entity with position and orientation (for 3D audio).

#### Supported Formats
* **WAV** – uncompressed, easy to load.
* **OGG/Vorbis** – compressed, smaller file size, requires a decoder (like `stb_vorbis`).

We’ll use LWJGL’s OpenAL bindings, which are already included in our dependencies.

#### Audio Manager Design
* **SoundCache** – maps file paths to OpenAL buffers.
* **SourcePool** – a pool of OpenAL sources (since OpenAL implementations have a limited number of simultaneous sources, typically 256).
* **Music** – a dedicated source for background music that can be paused, stopped, and faded.

### 3. Implementation

#### Step 11.1 – Add OpenAL Dependency
Ensure LWJGL’s OpenAL module is included in `build.gradle.kts` (it already is). Verify that the following line is present:

```kotlin
implementation("org.lwjgl:lwjgl-openal")
```

#### Step 11.2 – Create an Audio Loader
Create `src/main/java/engine/audio/AudioLoader.java`:

```java
package engine.audio;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class AudioLoader {
    /**
     * Loads a WAV file into an OpenAL buffer.
     * (We’ll use a simple WAV loader; for production, use a library like `libvorbis` or `javax.sound`.)
     */
    public static int loadWav(String path) {
        // In a real implementation you would parse the WAV header.
        // For simplicity, we’ll assume the file is mono 44.1 kHz 16‑bit.
        throw new UnsupportedOperationException("WAV loading not implemented – use OGG instead");
    }
    
    /**
     * Loads an OGG/Vorbis file into an OpenAL buffer using STB Vorbis.
     */
    public static int loadOgg(String path) {
        IntBuffer channels = IntBuffer.allocate(1);
        IntBuffer sampleRate = IntBuffer.allocate(1);
        ShortBuffer rawAudio = stb_vorbis_decode_filename(path, channels, sampleRate);
        if (rawAudio == null) {
            throw new RuntimeException("Failed to load OGG file: " + path);
        }
        
        int format = channels.get(0) == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
        int buffer = alGenBuffers();
        alBufferData(buffer, format, rawAudio, sampleRate.get(0));
        
        return buffer;
    }
}
```

#### Step 11.3 – Create an Audio Manager
Create `src/main/java/engine/audio/AudioManager.java`:

```java
package engine.audio;

import static org.lwjgl.openal.AL10.*;

import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private final Map<String, Integer> soundBuffers = new HashMap<>();
    private final int[] sources;
    private int nextSource = 0;
    
    public AudioManager(int maxSources) {
        sources = new int[maxSources];
        for (int i = 0; i < maxSources; i++) {
            sources[i] = alGenSources();
        }
    }
    
    public int loadSound(String id, String path) {
        if (soundBuffers.containsKey(id)) {
            return soundBuffers.get(id);
        }
        int buffer = AudioLoader.loadOgg(path);
        soundBuffers.put(id, buffer);
        return buffer;
    }
    
    public void playSound(String id, float volume, boolean loop) {
        Integer buffer = soundBuffers.get(id);
        if (buffer == null) return;
        
        int source = sources[nextSource];
        nextSource = (nextSource + 1) % sources.length;
        
        alSourceStop(source);
        alSourcei(source, AL_BUFFER, buffer);
        alSourcef(source, AL_GAIN, volume);
        alSourcei(source, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
        alSourcePlay(source);
    }
    
    public void setListenerPosition(float x, float y) {
        // For 2D games we can ignore Z coordinate
        alListener3f(AL_POSITION, x, y, 0.0f);
    }
    
    public void cleanup() {
        for (int source : sources) {
            alDeleteSources(source);
        }
        for (int buffer : soundBuffers.values()) {
            alDeleteBuffers(buffer);
        }
    }
}
```

#### Step 11.4 – Initialize OpenAL in Main
OpenAL requires a device and context. LWJGL can create them automatically with `AL.createCapabilities()`. Add this to `Main.java` before the game loop.

```java
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import static org.lwjgl.openal.ALC10.*;

private static AudioManager audioManager;

// In main(), after GLFW initialization:
long device = alcOpenDevice((ByteBuffer) null);
if (device == NULL) {
    throw new IllegalStateException("Failed to open OpenAL device");
}
long context = alcCreateContext(device, null);
alcMakeContextCurrent(context);
AL.createCapabilities();

audioManager = new AudioManager(16); // 16 simultaneous sounds
audioManager.loadSound("jump", "src/main/resources/audio/jump.ogg");
audioManager.loadSound("background", "src/main/resources/audio/music.ogg");

// Start background music
audioManager.playSound("background", 0.5f, true);
```

#### Step 11.5 – Integrate Audio with Gameplay
In the `GameScene`, play a sound effect when the player jumps or collides.

```java
// In GameScene.update():
if (playerJumped) {
    audioManager.playSound("jump", 1.0f, false);
}
```

#### Step 11.6 – Volume Control and Mute
Add methods to `AudioManager` to set master volume and mute.

```java
public void setMasterVolume(float volume) {
    // Volume is clamped between 0.0 and 1.0
    for (int source : sources) {
        alSourcef(source, AL_GAIN, volume);
    }
}

public void pauseAll() {
    for (int source : sources) {
        alSourcePause(source);
    }
}

public void resumeAll() {
    for (int source : sources) {
        int state = alGetSourcei(source, AL_SOURCE_STATE);
        if (state == AL_PAUSED) {
            alSourcePlay(source);
        }
    }
}
```

#### Step 11.7 – Cleanup on Exit
Don’t forget to destroy the OpenAL context when the game ends.

```java
// In Main.java, before glfwTerminate():
audioManager.cleanup();
ALC.destroy();
```

### 4. Code
New files:
* `src/main/java/engine/audio/AudioLoader.java`
* `src/main/java/engine/audio/AudioManager.java`

Modified files:
* `src/main/java/engine/Main.java`
* `build.gradle.kts` (already includes OpenAL)

### 5. Output
* Background music starts playing when the game launches.
* Sound effects play when the player jumps or collides with objects.
* The music loops seamlessly.
* The volume can be adjusted (via future UI).
* No audible glitches or performance drops.

### 6. Common Errors
* **No sound** – Check that the OpenAL device was created successfully. Verify the audio file path and format (must be mono/stereo 16‑bit PCM). Use `alGetError()` to get OpenAL error codes.
* **Crackling/distorted sound** – The sample rate of the audio file may not match the output device. Ensure you’re loading the file correctly.
* **Too many sources** – If you play more sounds than the number of allocated sources, older sounds will be cut off. Increase the source pool size or implement priority‑based source stealing.
* **Memory leak** – Remember to delete buffers and sources when they are no longer needed. The `AudioManager.cleanup()` method should be called on shutdown.
* **OGG loading fails** – Ensure `stb_vorbis` native library is available. LWJGL includes it, but you may need to add `implementation("org.lwjgl:lwjgl-stb")` to the dependencies.

---
**Next Step**: [Phase 12: Debugging Tools](phase12.md) – we’ll add an in‑game debug overlay, logging, and performance profiling.
