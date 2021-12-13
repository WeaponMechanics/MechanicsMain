plugins {
    id("me.deecaad.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    implementation(project(":WeaponMechanics"))
    implementation(project(":WeaponMechanicsCompatibility"))
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    baseName = "WeaponMechanics"
    classifier = null;
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        include(project(":WeaponMechanics"))
        include(project(":WeaponMechanicsCompatibility"))

        relocate ("co.aikar.timings.lib", "me.deecaad.weaponmechanics.timingslib") {
            include(dependency("co.aikar:minecraft-timings"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

description = "BuildWeaponMechanics"