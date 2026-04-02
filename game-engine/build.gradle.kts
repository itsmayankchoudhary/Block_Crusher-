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