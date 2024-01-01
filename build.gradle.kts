import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask

plugins {
    id("com.github.breadmoirai.github-release") version "2.4.1"
    id("com.cjcrafter.polymart-release") version "1.0.1"
}

polymart {
    val weaponMechanicsVersion = findProperty("weaponMechanicsVersion") as? String ?: throw IllegalArgumentException("weaponMechanicsVersion was null")

    apiKey = findProperty("polymart_api_key").toString()
    title = weaponMechanicsVersion
    version = weaponMechanicsVersion
    resourceId = 4709
    message = "This is a *test update* which was **published automatically**. You may ignore this."
    file.set(file("build").resolve("WeaponMechanics.zip"))
}

tasks.register<GithubReleaseTask>("createGithubRelease").configure {

    // https://github.com/BreadMoirai/github-release-gradle-plugin
    val weaponMechanicsVersion = findProperty("weaponMechanicsVersion") as? String ?: throw IllegalArgumentException("weaponMechanicsVersion was null")

    owner.set("WeaponMechanics")
    repo.set("MechanicsMain")
    authorization.set("Token ${findProperty("pass").toString()}")
    tagName.set("v${weaponMechanicsVersion}")
    targetCommitish.set("master")
    releaseName.set("v${weaponMechanicsVersion}")
    draft.set(false)
    prerelease.set(false)
    generateReleaseNotes.set(true)
    body.set("")
    overwrite.set(false)
    allowUploadToExisting.set(false)
    apiEndpoint.set("https://api.github.com")

    setReleaseAssets(file("build").listFiles())

    // If set to true, you can debug that this would do
    dryRun.set(false)

    doFirst {
        println("Creating GitHub release")
    }
}

// This is a helper method to compile MechanicsCore, WeaponMechanics, and to
// put all the stuff in zip files and such for github release.
tasks.register("buildForSpigotRelease").configure {
    println("Cleaning build directory")
    val folder = file("build")
    folder.deleteRecursively()
    folder.mkdir()

    dependsOn(":BuildMechanicsCore:shadowJar")
    dependsOn(":BuildWeaponMechanics:shadowJar")

    finalizedBy("resourcePackForSpigotRelease", "zipForSpigotRelease")
}

tasks.register<Copy>("resourcePackForSpigotRelease") {
    dependsOn("buildForSpigotRelease")

    // !!! Has to be updated when resource pack is updated !!!
    val resourcePackVersion = "2.1.0"

    from("${layout.projectDirectory}\\resourcepack\\WeaponMechanicsResourcePack-${resourcePackVersion}.zip")
    into(layout.buildDirectory)

    doFirst {
        println("Copy resource pack")
    }
}

tasks.register<Zip>("zipForSpigotRelease") {
    dependsOn("buildForSpigotRelease", "resourcePackForSpigotRelease")
    archiveFileName.set("WeaponMechanics.zip")
    destinationDirectory.set(layout.buildDirectory)

    from(layout.buildDirectory) {
        include("*.jar")
    }

    from("install-instructions.txt")

    doFirst {
        println("Generate zip file")
    }
}