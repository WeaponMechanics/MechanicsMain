plugins {
    id("me.deecaad.mechanics-project")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":MechanicsCore"))
    adventureChatAPI()

    paperweight.paperDevBundle("1.17.1-R0.1-SNAPSHOT")
}

// These older
java.toolchain.languageVersion.set(JavaLanguageVersion.of(16))

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(16)
    }
}