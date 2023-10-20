plugins {
    id("me.deecaad.java-conventions")
}

repositories {
    mavenCentral()
    maven(url = "https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    compileOnly(Dependencies.LATEST_SPIGOT_API)
    compileOnly(Dependencies.PLACEHOLDER_API)
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("io.netty:netty-all:4.1.90.Final")
    compileOnly("com.mojang:brigadier:1.0.18")

    compileOnly(Dependencies.PROTOCOL_LIB)
    compileOnly(Dependencies.GEYSER)

    implementation(Dependencies.JSON_SIMPLE)

    implementation(Dependencies.ADVENTURE_API)
    implementation(Dependencies.ADVENTURE_BUKKIT)
    implementation(Dependencies.ADVENTURE_TEXT_LEGACY)
    implementation(Dependencies.ADVENTURE_TEXT_MINIMESSAGE)
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.slf4j:slf4j-nop:1.7.30")
    implementation(Dependencies.MYTHIC_MOBS)

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation(Dependencies.LATEST_SPIGOT_API)
    testImplementation(Dependencies.ANNOTATIONS)
}

tasks.test {
    useJUnitPlatform()
}

description = "MechanicsCore"
