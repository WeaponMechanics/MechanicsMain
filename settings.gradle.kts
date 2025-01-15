pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

// Include every module
include(":WeaponMechanics")
include(":MechanicsCore")
include(":BuildMechanicsCore")
include(":BuildWeaponMechanics")

include(":CoreCompatibility")
include(":WorldGuardV7")

// All projects in the non-root directory need to have their directories updates.

project(":WorldGuardV7").projectDir = file("CoreCompatibility/WorldGuardV7")

/**
 * Utility function to add all compatibility modules of a given type.
 *
 * @param type Either "Core" or "Weapon"
 */
fun compatibility(type: String) {
    var addedOne = false
    file("${type}Compatibility").listFiles()?.forEach {
        if (it.isDirectory && it.name.matches(Regex("${type}_\\d+_\\d+_R\\d+"))) {
            include(":${it.name}")
            project(":${it.name}").projectDir = file("${type}Compatibility/${it.name}")
            addedOne = true
        }
    }
    if (!addedOne)
        throw IllegalArgumentException("No ${type}Compatibility modules found!")
}

// Add all compatibility modules
compatibility("Core")
compatibility("Weapon")