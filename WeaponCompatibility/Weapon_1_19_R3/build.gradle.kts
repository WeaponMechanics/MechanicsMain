plugins {
    id("me.deecaad.mechanics-project")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":MechanicsCore"))
    compileOnly(project(":WeaponMechanics"))

    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")

    compileOnly(Dependencies.PROTOCOL_LIB)
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }
}