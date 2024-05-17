plugins {
    id("me.deecaad.mechanics-project")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":MechanicsCore"))
    compileOnly(project(":WeaponMechanics"))

    paperweight.paperDevBundle("1.17.1-R0.1-SNAPSHOT")

    compileOnly(Dependencies.PROTOCOL_LIB)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(16)
    }
}