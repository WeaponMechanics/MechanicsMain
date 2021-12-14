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
    implementation(project(":MechanicsCoreCompatibility"))
    implementation(project(":WorldGuardV6"))
    implementation(project(":WorldGuardV7"))
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
        include(project(":MechanicsCoreCompatibility"))
        include(project(":WorldGuardV6"))
        include(project(":WorldGuardV7"))
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

description = "Library plugin for WeaponMechanics"
version = "0.1.1"