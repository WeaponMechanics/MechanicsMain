plugins {
    id("me.deecaad.java-conventions")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperDevBundle("1.20-R0.1-SNAPSHOT")
    implementation(project(":MechanicsCore"))
    implementation(project(":WeaponMechanics"))

    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
}

//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(17))
//    }
//}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }
}

description = "WeaponCompatibility 1.20 R1"