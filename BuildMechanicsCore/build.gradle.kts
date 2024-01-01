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

    implementation(project(":Core_1_12_R1"))
    implementation(project(":Core_1_13_R2"))
    implementation(project(":Core_1_14_R1"))
    implementation(project(":Core_1_15_R1"))
    implementation(project(":Core_1_16_R3"))
    implementation(project(":Core_1_17_R1", "reobf"))
    implementation(project(":Core_1_18_R2", "reobf"))
    implementation(project(":Core_1_19_R3", "reobf"))
    implementation(project(":Core_1_20_R1", "reobf"))
    implementation(project(":Core_1_20_R2", "reobf"))
    implementation(project(":Core_1_20_R3", "reobf"))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17) // We need to set release compatibility to java 17 since MC 18+ uses it
    }
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    val mechanicsCoreVersion = findProperty("mechanicsCoreVersion") as? String ?: throw IllegalArgumentException("weaponMechanicsVersion was null")

    main = "me.deecaad.core.MechanicsCore"
    name = "MechanicsCore" // Since we don't want to use "BuildMechanicsCore"\
    version = mechanicsCoreVersion
    apiVersion = "1.13"

    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    authors = listOf("DeeCaaD", "CJCrafter")
    loadBefore = listOf("WorldEdit", "WorldGuard", "PlaceholderAPI", "MythicMobs", "Geyser-Spigot")
}

tasks.named<ShadowJar>("shadowJar") {
    val mechanicsCoreVersion = findProperty("mechanicsCoreVersion") as? String ?: throw IllegalArgumentException("weaponMechanicsVersion was null")

    destinationDirectory.set(file("../build"))
    archiveFileName.set("MechanicsCore-$mechanicsCoreVersion.jar")
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        include(project(":MechanicsCore"))
        include(project(":CoreCompatibility"))
        include(project(":WorldGuardV6"))
        include(project(":WorldGuardV7"))

        include(project(":Core_1_12_R1"))
        include(project(":Core_1_13_R2"))
        include(project(":Core_1_14_R1"))
        include(project(":Core_1_15_R1"))
        include(project(":Core_1_16_R3"))
        include(project(":Core_1_17_R1"))
        include(project(":Core_1_18_R2"))
        include(project(":Core_1_19_R3"))
        include(project(":Core_1_20_R1"))
        include(project(":Core_1_20_R2"))
        include(project(":Core_1_20_R3"))

        relocate ("net.kyori", "me.deecaad.core.lib") {
            include(dependency("net.kyori::"))
        }

        relocate ("com.zaxxer.hikari", "me.deecaad.core.lib.hikari") {
            include(dependency("com.zaxxer::"))
        }

        relocate ("org.slf4j", "me.deecaad.core.lib.slf4j") {
            include(dependency("org.slf4j::"))
        }
    }

    doFirst {
        println("Compile MechanicsCore")
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}