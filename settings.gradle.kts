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
include(":WorldGuardV6")
include(":WorldGuardV7")

// All projects in the non-root directory need to have their directories updates.

project(":WorldGuardV7").projectDir = file("CoreCompatibility/WorldGuardV7")
project(":WorldGuardV6").projectDir = file("CoreCompatibility/WorldGuardV6")

// Add all compatibility modules
var addedOne = false
file("CoreCompatibility").listFiles()?.forEach {
    if (it.isDirectory && it.name.matches(Regex("Core_\\d+_\\d+_R\\d+"))) {
        include(":${it.name}")
        project(":${it.name}").projectDir = file("CoreCompatibility/${it.name}")
        addedOne = true
    }
}
if (!addedOne)
    throw IllegalArgumentException("No CoreCompatibility modules found!")

addedOne = false
file("WeaponCompatibility").listFiles()?.forEach {
    if (it.isDirectory && it.name.matches(Regex("Weapon_\\d+_\\d+_R\\d+"))) {
        include(":${it.name}")
        project(":${it.name}").projectDir = file("WeaponCompatibility/${it.name}")
        addedOne = true
    }
}
if (!addedOne)
    throw IllegalArgumentException("No WeaponCompatibility modules found!")