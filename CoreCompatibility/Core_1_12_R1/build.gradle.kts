plugins {
    id("me.deecaad.java-conventions")
}

dependencies {
    implementation(project(":MechanicsCore"))
    compileOnly(files(file("../../lib/nms/spigot-1.12.2.jar")))
    compileOnly("com.mojang:brigadier:1.0.18")
}

description = "CoreCompatibility 1.12 R1"