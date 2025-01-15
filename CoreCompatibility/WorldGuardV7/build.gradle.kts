plugins {
    id("me.deecaad.mechanics-project")
}

dependencies {
    api(Dependencies.LATEST_SPIGOT_API)
    implementation(project(":MechanicsCore"))
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.0-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13-SNAPSHOT")
}
