plugins {
    id("me.deecaad.java-conventions")
}

dependencies {
    implementation(project(":MechanicsCore"))
    implementation(project(":WeaponMechanics"))
    compileOnly(files(file("../../lib/nms/spigot-1.16.5.jar")))

    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
}

description = "WeaponCompatibility 1.16 R3"