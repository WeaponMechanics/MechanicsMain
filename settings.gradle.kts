pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "MechanicsMain"

// Include every module
include(":WeaponMechanics")
include(":MechanicsCore")
include(":WeaponMechanicsPlus")
include(":BuildMechanicsCore")
include(":BuildWeaponMechanics")

include(":CoreCompatibility")
include(":WorldGuardV6")
include(":WorldGuardV7")
include(":Core_1_12_R1")
include(":Core_1_13_R2")
include(":Core_1_14_R1")
include(":Core_1_15_R1")
include(":Core_1_16_R3")
include(":Core_1_17_R1")
include(":Core_1_18_R2")
include(":Core_1_19_R3")
include(":Core_1_20_R1")
include(":Core_1_20_R2")

include(":WeaponCompatibility")
include(":Weapon_1_12_R1")
include(":Weapon_1_13_R2")
include(":Weapon_1_14_R1")
include(":Weapon_1_15_R1")
include(":Weapon_1_16_R3")
include(":Weapon_1_17_R1")
include(":Weapon_1_18_R2")
include(":Weapon_1_19_R3")
include(":Weapon_1_20_R1")
include(":Weapon_1_20_R2")


// All projects in the non-root directory need to have their directories updates.
project(":WorldGuardV7").projectDir = file("CoreCompatibility/WorldGuardV7")
project(":WorldGuardV6").projectDir = file("CoreCompatibility/WorldGuardV6")

project(":Core_1_12_R1").projectDir = file("CoreCompatibility/Core_1_12_R1")
project(":Core_1_13_R2").projectDir = file("CoreCompatibility/Core_1_13_R2")
project(":Core_1_14_R1").projectDir = file("CoreCompatibility/Core_1_14_R1")
project(":Core_1_15_R1").projectDir = file("CoreCompatibility/Core_1_15_R1")
project(":Core_1_16_R3").projectDir = file("CoreCompatibility/Core_1_16_R3")
project(":Core_1_17_R1").projectDir = file("CoreCompatibility/Core_1_17_R1")
project(":Core_1_18_R2").projectDir = file("CoreCompatibility/Core_1_18_R2")
project(":Core_1_19_R3").projectDir = file("CoreCompatibility/Core_1_19_R3")
project(":Core_1_20_R1").projectDir = file("CoreCompatibility/Core_1_20_R1")
project(":Core_1_20_R2").projectDir = file("CoreCompatibility/Core_1_20_R2")

project(":Weapon_1_12_R1").projectDir = file("WeaponCompatibility/Weapon_1_12_R1")
project(":Weapon_1_13_R2").projectDir = file("WeaponCompatibility/Weapon_1_13_R2")
project(":Weapon_1_14_R1").projectDir = file("WeaponCompatibility/Weapon_1_14_R1")
project(":Weapon_1_15_R1").projectDir = file("WeaponCompatibility/Weapon_1_15_R1")
project(":Weapon_1_16_R3").projectDir = file("WeaponCompatibility/Weapon_1_16_R3")
project(":Weapon_1_17_R1").projectDir = file("WeaponCompatibility/Weapon_1_17_R1")
project(":Weapon_1_18_R2").projectDir = file("WeaponCompatibility/Weapon_1_18_R2")
project(":Weapon_1_19_R3").projectDir = file("WeaponCompatibility/Weapon_1_19_R3")
project(":Weapon_1_20_R1").projectDir = file("WeaponCompatibility/Weapon_1_20_R1")
project(":Weapon_1_20_R2").projectDir = file("WeaponCompatibility/Weapon_1_20_R2")
