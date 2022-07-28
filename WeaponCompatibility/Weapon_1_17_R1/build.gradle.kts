plugins {
    id("me.deecaad.java-conventions")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    implementation(project(":MechanicsCore"))
    implementation(project(":WeaponMechanics"))
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")

    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
}

//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(16))
//    }
//}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(16)
    }
}

description = "WeaponCompatibility 1.17 R1"