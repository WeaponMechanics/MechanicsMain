plugins {
    id("me.deecaad.java-conventions")
}

repositories {
    maven {
        name = "lumine-repo"
        url = uri("http://mvn.lumine.io/repository/maven-public/")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.10.10")
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("io.netty:netty-all:4.1.86.Final")
    compileOnly("com.mojang:brigadier:1.0.18")

    compileOnly("com.github.dmulloy2:ProtocolLib:4.8.0")

    implementation("com.googlecode.json-simple:json-simple:1.1.1")

    implementation("net.kyori:adventure-api:4.13.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.12.0")
    implementation("net.kyori:adventure-platform-bukkit:4.1.2")
    implementation("net.kyori:adventure-text-minimessage:4.12.0")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.slf4j:slf4j-nop:1.7.30")
    implementation("io.lumine:Mythic-Dist:5.0.1-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.spigotmc:spigot-api:1.19.1-R0.1-SNAPSHOT")
    testImplementation("org.jetbrains:annotations:23.0.0")
}

tasks.test {
    useJUnitPlatform()
}

description = "MechanicsCore"
