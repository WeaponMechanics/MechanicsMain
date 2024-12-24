plugins {
    `java-library`
    id("com.diffplug.spotless")
}

repositories {
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot API
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") // Adventure Snapshots
    maven(url = "https://libraries.minecraft.net/") // Brigadier, GameProfile
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven(url = "https://jitpack.io") // Vault
    maven(url = "https://maven.enginehub.org/repo/") // WorldGuard
    maven(url = "https://mvn.lumine.io/repository/maven-public/") // MythicMobs
    maven(url = "https://repo.opencollab.dev/main/") // GeyserMC
    maven(url = "https://repo.jeff-media.com/public/") // SpigotUpdateChecker
    maven(url = "https://repo.dmulloy2.net/repository/public/")
    maven(url = "https://repo.codemc.org/repository/maven-public/") // NBTAPI from CommandAPI
}

dependencies {
    compileOnly(Dependencies.ANNOTATIONS)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(21)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    test {
        useJUnitPlatform()
    }
}

spotless {
    kotlin {
        ktlint()
    }
    java {
        eclipse().configFile(rootProject.file("java-style.xml").absolutePath)
        formatAnnotations()
    }
}