import net.minecrell.pluginyml.bukkit.BukkitPluginDescription


plugins {
    id("me.deecaad.mechanics-project")
    //id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.github.goooler.shadow") version "8.1.7"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

dependencies {
    implementation(project(":WeaponMechanics"))

    // Add all compatibility modules
    val devMode = findProperty("devMode") == "true"
    var addedOne = false
    file("../WeaponCompatibility").listFiles()?.forEach {
        if (it.isDirectory && it.name.matches(Regex("Weapon_\\d+_\\d+_R\\d+"))) {
            // Use the reobf variant for all modules 1.17+
            val major = it.name.split("_")[2].toInt()

            if (major >= 17) {
                implementation(project(":${it.name}", "reobf"))
            } else if (!devMode) {
                implementation(project(":${it.name}"))
            }
            addedOne = true
        }
    }
    if (!addedOne)
        throw IllegalArgumentException("No WeaponCompatibility modules found!")
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    val weaponMechanicsVersion = findProperty("weaponMechanicsVersion") as? String ?: throw IllegalArgumentException("weaponMechanicsVersion was null")

    main = "me.deecaad.weaponmechanics.WeaponMechanicsLoader"
    name = "WeaponMechanics" // Since we don't want to use "BuildWeaponMechanics"
    version = weaponMechanicsVersion
    apiVersion = "1.13"

    authors = listOf("DeeCaaD", "CJCrafter")
    depend = listOf("ProtocolLib")
    softDepend = listOf("MechanicsCore", "MythicMobs", "CrackShot", "CrackShotPlus", "VivecraftSpigot")

    permissions {
        register("weaponmechanics.use.*") {
            description = "Permission to use all weapons"
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
    }
}

tasks.shadowJar {
    val weaponMechanicsVersion = findProperty("weaponMechanicsVersion") as? String ?: throw IllegalArgumentException("weaponMechanicsVersion was null")

    destinationDirectory.set(file("../build"))
    archiveFileName.set("WeaponMechanics-${weaponMechanicsVersion}.jar")

    dependencies {
        include(project(":WeaponMechanics"))

        val devMode = findProperty("devMode") == "true"
        var addedOne = false
        file("../WeaponCompatibility").listFiles()?.forEach {
            if (it.isDirectory && it.name.matches(Regex("Weapon_\\d+_\\d+_R\\d+"))) {
                // Filter out projects when in devMode
                val major = it.name.split("_")[2].toInt()
                if (devMode && major < 17)
                    return@forEach

                include(project(":${it.name}"))
                addedOne = true
            }
        }
        if (!addedOne)
            throw IllegalArgumentException("No WeaponCompatibility modules found!")


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
    relocate("kotlin.", "me.deecaad.weaponmechanics.lib.kotlin.")
}