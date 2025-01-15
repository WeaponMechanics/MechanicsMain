plugins {
    id("me.deecaad.mechanics-project")
    id("com.gradleup.shadow") version "8.3.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

dependencies {
    implementation(project(":MechanicsCore"))
    implementation(project(":CoreCompatibility"))
    implementation(project(":WorldGuardV7"))

    // Implementation for all the libraries we shade:
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.21")
    implementation("net.kyori:adventure-api:${Versions.ADVENTURE_API}")
    implementation("net.kyori:adventure-platform-bukkit:${Versions.ADVENTURE_BUKKIT}")
    implementation("net.kyori:adventure-text-serializer-legacy:${Versions.ADVENTURE_API}")
    implementation("net.kyori:adventure-text-minimessage:${Versions.ADVENTURE_API}")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.slf4j:slf4j-nop:1.7.30")
    implementation(Dependencies.X_SERIES)
    implementation(Dependencies.FOLIA_SCHEDULER)
    implementation(Dependencies.COMMAND_API_SHADE)
    implementation("net.bytebuddy:byte-buddy:1.15.10")  // Making classes describable

    // Add all compatibility modules
    var addedOne = false
    file("../CoreCompatibility").listFiles()?.forEach {
        if (it.isDirectory && it.name.matches(Regex("Core_\\d+_\\d+_R\\d+"))) {
            implementation(project(":${it.name}", "reobf"))
            addedOne = true
        }
    }
    if (!addedOne)
        throw IllegalArgumentException("No CoreCompatibility modules found!")
}


// See https://github.com/Minecrell/plugin-yml
bukkit {
    val mechanicsCoreVersion = findProperty("mechanicsCoreVersion") as? String ?: throw IllegalArgumentException("weaponMechanicsVersion was null")

    main = "me.deecaad.core.MechanicsCore"
    name = "MechanicsCore" // Since we don't want to use "BuildMechanicsCore"
    version = mechanicsCoreVersion
    apiVersion = "1.13"
    foliaSupported = true

    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    authors = listOf("DeeCaaD", "CJCrafter")
    loadBefore = listOf("WorldEdit", "WorldGuard", "PlaceholderAPI", "MythicMobs", "Geyser-Spigot")
}

tasks.shadowJar {
    val mechanicsCoreVersion = findProperty("mechanicsCoreVersion") as? String ?: throw IllegalArgumentException("weaponMechanicsVersion was null")

    destinationDirectory.set(file("../build"))
    archiveFileName.set("MechanicsCore-$mechanicsCoreVersion.jar")

    dependencies {
        include(project(":MechanicsCore"))
        include(project(":CoreCompatibility"))
        include(project(":WorldGuardV7"))

        // Add all compatibility modules
        var addedOne = false
        file("../CoreCompatibility").listFiles()?.forEach {
            if (it.isDirectory && it.name.matches(Regex("Core_\\d+_\\d+_R\\d+"))) {
                include(project(":${it.name}"))
                addedOne = true
            }
        }
        if (!addedOne)
            throw IllegalArgumentException("No CoreCompatibility modules found!")

        relocate("kotlin.", "me.deecaad.core.lib.kotlin.") {
            include(dependency("org.jetbrains.kotlin:"))
        }

        relocate("net.kyori", "me.deecaad.core.lib") {
            include(dependency("net.kyori::"))
        }

        relocate("com.zaxxer.hikari", "me.deecaad.core.lib.hikari") {
            include(dependency("com.zaxxer::"))
        }

        relocate("org.slf4j", "me.deecaad.core.lib.slf4j") {
            include(dependency("org.slf4j::"))
        }

        relocate("com.cryptomorin.xseries", "me.deecaad.core.lib.xseries") {
            include(dependency("com.github.cryptomorin:XSeries:"))
        }

        relocate("com.cjcrafter.foliascheduler", "me.deecaad.core.lib.scheduler") {
            include(dependency("com.cjcrafter:foliascheduler:"))
        }

        relocate("dev.jorel.commandapi", "me.deecaad.core.lib.commandapi") {
            include(dependency("dev.jorel:commandapi-bukkit-shade:"))
        }

        relocate("net.bytebuddy", "me.deecaad.core.lib.bytebuddy") {
            include(dependency("net.bytebuddy::"))
        }
    }
}
