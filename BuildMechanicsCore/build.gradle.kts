import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("me.deecaad.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    implementation(project(":MechanicsCore"))
    implementation(project(":CoreCompatibility"))
    implementation(project(":WorldGuardV6"))
    implementation(project(":WorldGuardV7"))

    implementation(project(":Core_1_9_R2" ))
    implementation(project(":Core_1_10_R1"))
    implementation(project(":Core_1_11_R1"))
    implementation(project(":Core_1_12_R1"))
    implementation(project(":Core_1_13_R2"))
    implementation(project(":Core_1_14_R1"))
    implementation(project(":Core_1_15_R1"))
    implementation(project(":Core_1_16_R3"))
    implementation(project(":Core_1_17_R1", "reobf"))
    implementation(project(":Core_1_18_R1", "reobf"))
    implementation(project(":Core_1_18_R2", "reobf"))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17) // We need to set release compatibility to java 17 since MC 18+ uses it
    }
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    main = "me.deecaad.core.MechanicsCore"
    name = "MechanicsCore" // Since we don't want to use "BuildMechanicsCore"
    apiVersion = "1.13"

    authors = listOf("DeeCaaD", "CJCrafter")
    softDepend = listOf("WorldEdit", "WorldGuard", "PlaceholderAPI")
}

tasks.named<ShadowJar>("shadowJar") {
    baseName = "MechanicsCore" // Since we don't want to use "BuildMechanicsCore"
    classifier = null;
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        include(project(":MechanicsCore"))
        include(project(":CoreCompatibility"))
        include(project(":WorldGuardV6"))
        include(project(":WorldGuardV7"))

        include(project(":Core_1_9_R2" ))
        include(project(":Core_1_10_R1"))
        include(project(":Core_1_11_R1"))
        include(project(":Core_1_12_R1"))
        include(project(":Core_1_13_R2"))
        include(project(":Core_1_14_R1"))
        include(project(":Core_1_15_R1"))
        include(project(":Core_1_16_R3"))
        include(project(":Core_1_17_R1"))
        include(project(":Core_1_18_R1"))
        include(project(":Core_1_18_R2"))
        include(project(":Core_1_18_R1")) // compiled paper user-dev

        relocate ("net.kyori.adventure", "me.deecaad.core.lib.adventure") {
            include(dependency("net.kyori:adventure-api"))
            include(dependency("net.kyori:adventure-platform-bukkit"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

description = "Library plugin for WeaponMechanics"
version = "1.1.1-BETA"