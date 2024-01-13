plugins {
    `java-library`
}

repositories {
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://repo.maven.apache.org/maven2/")
    maven(url = "https://libraries.minecraft.net/")
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven(url = "https://jitpack.io")
    maven(url = "https://maven.enginehub.org/repo/")
    maven(url = "https://mvn.lumine.io/repository/maven-public/")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://repo.opencollab.dev/main/")
    maven(url = "https://repo.jeff-media.com/public/") // SpigotUpdateChecker
}

dependencies {
    compileOnly(Dependencies.ANNOTATIONS)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(16)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}