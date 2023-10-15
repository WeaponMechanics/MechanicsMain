plugins {
    id("me.deecaad.java-conventions")
}

dependencies {
    api(Dependencies.LATEST_SPIGOT_API)
    implementation(project(":MechanicsCore"))
    compileOnly(files(file("../../lib/worldguard/worldedit-bukkit-6.1.9.jar")))
    compileOnly(files(file("../../lib/worldguard/worldguard-bukkit-6.2.2.jar")))
}
