plugins {
    id("me.deecaad.java-conventions")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")
    implementation(project(":MechanicsCore"))
    implementation(project(":WeaponMechanics"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

description = "WeaponCompatibility 1.18 R1"