import net.minecrell.pluginyml.bukkit.BukkitPluginDescription


plugins {
    id("me.deecaad.mechanics-project")
    id("com.gradleup.shadow") version "8.3.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

dependencies {
    implementation(project(":WeaponMechanics"))

    // Implementation for all the libraries we shade:
    implementation(Dependencies.BSTATS)
    implementation("com.jeff_media:SpigotUpdateChecker:3.0.3")
    implementation(Dependencies.GSON)

    // Add all compatibility modules
    var addedOne = false
    file("../WeaponCompatibility").listFiles()?.forEach {
        if (it.isDirectory && it.name.matches(Regex("Weapon_\\d+_\\d+_R\\d+"))) {
            implementation(project(":${it.name}", "reobf"))
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
    foliaSupported = true

    authors = listOf("DeeCaaD", "CJCrafter")
    depend = listOf("packetevents")
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

        var addedOne = false
        file("../WeaponCompatibility").listFiles()?.forEach {
            if (it.isDirectory && it.name.matches(Regex("Weapon_\\d+_\\d+_R\\d+"))) {
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
    relocate("kotlin.", "me.deecaad.core.lib.kotlin.")
    relocate("net.kyori", "me.deecaad.core.lib")
    relocate("com.cryptomorin.xseries", "me.deecaad.core.lib.xseries")
    relocate("com.cjcrafter.foliascheduler", "me.deecaad.core.lib.scheduler")
    relocate("dev.jorel.commandapi", "me.deecaad.core.lib.commandapi")
}