plugins {
    id("me.deecaad.java-conventions")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    implementation(project(":MechanicsCore"))
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

description = "CoreCompatibility 1.17 R1"