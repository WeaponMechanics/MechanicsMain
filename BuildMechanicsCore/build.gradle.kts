plugins {
    id("me.deecaad.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.0"
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

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    baseName = "MechanicsCore"
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

description = "BuildMechanicsCore"