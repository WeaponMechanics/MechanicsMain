plugins {
    id("me.deecaad.java-conventions")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperDevBundle("1.20.3-R0.1-SNAPSHOT")
    implementation(project(":MechanicsCore"))
    implementation(project(":WeaponMechanics"))

    compileOnly(Dependencies.PROTOCOL_LIB)
}
tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }
}