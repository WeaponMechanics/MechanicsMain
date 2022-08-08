plugins {
    id("me.deecaad.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.10.10")
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("io.netty:netty-all:4.1.77.Final")
    compileOnly("com.mojang:brigadier:1.0.18")

    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("net.kyori:adventure-api:4.11.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.11.0")
    implementation("net.kyori:adventure-platform-bukkit:4.1.1")
    implementation("net.kyori:adventure-text-minimessage:4.11.0")
    implementation("com.zaxxer:HikariCP:5.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.spigotmc:spigot-api:1.19.1-R0.1-SNAPSHOT")
    testImplementation("org.jetbrains:annotations:23.0.0")
}

tasks.test {
    useJUnitPlatform()
}

description = "MechanicsCore"
