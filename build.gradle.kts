
// This is a helper method to compile MechanicsCore, WeaponMechanics, and to
// put all the stuff in zip files and such for github release.
tasks.register("buildForSpigotRelease").configure {
    delete {
        fileTree("build")
    }

    val mechanicsCoreVersion = project(":BuildMechanicsCore").version.toString()
    val weaponMechanicsVersion = project(":BuildWeaponMechanics").version.toString()

    val folder = file("build")
    folder.mkdir()

    // We need a `versions.txt` file which contains information on the current
    // versions of each plugin which is to be included in the release. For
    // example:
    //   MechanicsCore: 1.0.0-BETA
    //   WeaponMechanics: 1.0.0-BETA
    //   WeaponMechanicsResourcePack: 1.0.0
    val file = file("build/versions.txt")
    file.appendText("MechanicsCore: $mechanicsCoreVersion\n")
    file.appendText("WeaponMechanics: $weaponMechanicsVersion\n")
    file.appendText("WeaponMechanicsResourcePack: 1.0.0\n")

    dependsOn(":BuildMechanicsCore:shadowJar")
    dependsOn(":BuildWeaponMechanics:shadowJar")
}