import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription


plugins {
    id("me.deecaad.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

repositories {
    maven(url = "https://repo.jeff-media.com/public/")
}

dependencies {
    implementation(project(":WeaponMechanics"))
    implementation(project(":WeaponCompatibility"))

    implementation(project(":Weapon_1_12_R1"))
    implementation(project(":Weapon_1_13_R2"))
    implementation(project(":Weapon_1_14_R1"))
    implementation(project(":Weapon_1_15_R1"))
    implementation(project(":Weapon_1_16_R3"))
    implementation(project(":Weapon_1_17_R1", "reobf"))
    implementation(project(":Weapon_1_18_R2", "reobf"))
    implementation(project(":Weapon_1_19_R3", "reobf"))
    implementation(project(":Weapon_1_20_R1", "reobf"))
    implementation(project(":Weapon_1_20_R2", "reobf"))
    implementation(project(":Weapon_1_20_R3", "reobf"))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17) // We need to set release compatibility to java 17 since MC 18+ uses it
    }
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    val weaponMechanicsVersion = findProperty("weaponMechanicsVersion") as? String ?: throw IllegalArgumentException("weaponMechanicsVersion was null")

    main = "me.deecaad.weaponmechanics.WeaponMechanicsLoader"
    name = "WeaponMechanics" // Since we don't want to use "BuildWeaponMechanics"
    version = weaponMechanicsVersion
    apiVersion = "1.13"

    authors = listOf("DeeCaaD", "CJCrafter")
    depend = listOf("ProtocolLib") // TODO switch to soft depends and add auto installer
    softDepend = listOf("MechanicsCore", "MythicMobs", "CrackShot", "CrackShotPlus", "VivecraftSpigot")

    permissions {
        register("weaponmechanics.use.*") {
            description = "Permission to use all weapons"
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
    }
}

tasks.named<ShadowJar>("shadowJar") {
    val weaponMechanicsVersion = findProperty("weaponMechanicsVersion") as? String ?: throw IllegalArgumentException("weaponMechanicsVersion was null")

    destinationDirectory.set(file("../build"))
    archiveFileName.set("WeaponMechanics-${weaponMechanicsVersion}.jar")
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        include(project(":WeaponMechanics"))
        include(project(":WeaponCompatibility"))

        include(project(":Weapon_1_12_R1"))
        include(project(":Weapon_1_13_R2"))
        include(project(":Weapon_1_14_R1"))
        include(project(":Weapon_1_15_R1"))
        include(project(":Weapon_1_16_R3"))
        include(project(":Weapon_1_17_R1"))
        include(project(":Weapon_1_18_R2"))
        include(project(":Weapon_1_19_R3"))
        include(project(":Weapon_1_20_R1"))
        include(project(":Weapon_1_20_R2"))
        include(project(":Weapon_1_20_R3"))

        relocate("org.bstats", "me.deecaad.weaponmechanics.lib.bstats") {
            include(dependency("org.bstats:"))
        }
        relocate("com.jeff_media", "me.deecaad.weaponmechanics.lib") {
            include(dependency("com.jeff_media:"))
        }
        relocate("com.google.gson", "me.deecaad.weaponmechanics.lib.gson") {
            include(dependency("com.google.code.gson:"))
        }
    }

    // This doesn't actually include any dependencies, this relocates all references
    // to the mechanics core lib.
    relocate("net.kyori", "me.deecaad.core.lib")

    doFirst {
        println("Compile WeaponMechanics")
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}