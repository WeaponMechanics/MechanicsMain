plugins {
    id("me.deecaad.mechanics-project")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":MechanicsCore"))
    adventureChatAPI()

    paperweight.paperDevBundle("1.18.2-R0.1-SNAPSHOT")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }
}