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
    implementation(project(":WeaponMechanics"))
    implementation(project(":WeaponMechanicsCompatibility"))
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    main = "me.deecaad.weaponmechanics.WeaponMechanics"
    name = "WeaponMechanics" // Since we don't want to use "BuildWeaponMechanics"
    apiVersion = "1.13"

    authors = listOf("DeeCaaD", "CJCrafter")
    softDepend = listOf("MechanicsCore")
}

tasks.named<ShadowJar>("shadowJar") {
    baseName = "WeaponMechanics" // Since we don't want to use "BuildWeaponMechanics"
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

description = "A New Age of Weapons in Minecraft"
version = "0.2.2"