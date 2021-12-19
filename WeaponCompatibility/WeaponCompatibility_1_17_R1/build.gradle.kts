plugins {
    id("me.deecaad.java-conventions")
}

dependencies {
    implementation(project(":MechanicsCore"))
    implementation(project(":WeaponMechanics"))
    compileOnly(files(file("../../lib/nms/spigot-1.17.1.jar")))
}

description = "WeaponCompatibility 1.17 R1"