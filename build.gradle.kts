import java.io.FileOutputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.BufferedInputStream
import java.util.zip.ZipOutputStream

// This is a helper method to compile MechanicsCore, WeaponMechanics, and to
// put all the stuff in zip files and such for github release.
tasks.register("buildForSpigotRelease").configure {
    val mechanicsCoreVersion = project(":BuildMechanicsCore").version.toString()
    val weaponMechanicsVersion = project(":BuildWeaponMechanics").version.toString()

    println("Cleaning build directory")
    val folder = file("build")
    folder.deleteRecursively()
    folder.mkdir()

    // We need a `versions.txt` file which contains information on the current
    // versions of each plugin which is to be included in the release. For
    // example:
    //   MechanicsCore: 1.0.0-BETA
    //   WeaponMechanics: 1.0.0-BETA
    //   WeaponMechanicsResourcePack: 1.0.0
    println("Writing version.txt")
    val file = file("build/versions.txt")
    file.appendText("MechanicsCore: $mechanicsCoreVersion\n")
    file.appendText("WeaponMechanics: $weaponMechanicsVersion\n")
    file.appendText("WeaponMechanicsResourcePack: 1.1.0\n")

    println("Compile MechanicsCore")
    dependsOn(":BuildMechanicsCore:shadowJar")
    println("Compile WeaponMechanics")
    dependsOn(":BuildWeaponMechanics:shadowJar")

    println("Generate zip file")
    finalizedBy("zipForSpigotRelease")
}

tasks.register<Zip>("zipForSpigotRelease") {
    archiveFileName.set("WeaponMechanics.zip")
    destinationDirectory.set(layout.buildDirectory)

    from (layout.buildDirectory) {
        include("*.jar")
    }
}