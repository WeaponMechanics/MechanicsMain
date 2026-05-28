plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

repositories {
    maven(url = "https://central.sonatype.com/repository/maven-snapshots/") // MechanicsCore Snapshots
}

dependencies {
    compileOnly(project(":weaponmechanics-core"))

    compileOnly(libs.mechanicsCore)
    compileOnly(libs.foliaScheduler)

    paperweight.paperDevBundle("26.1.2.build.66-stable")
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(25)) } }
tasks.withType<JavaCompile>().configureEach { options.release.set(25) }

// Paper 26.1+ is mojang-mapped at runtime
tasks.named("reobfJar") { enabled = false }
tasks.named("assemble") { dependsOn(tasks.named("jar")) }

configurations {
    listOf("apiElements", "runtimeElements").forEach { name ->
        named(name).configure {
            outgoing.artifacts.clear()
            outgoing.artifact(tasks.named("jar"))
        }
    }
}
