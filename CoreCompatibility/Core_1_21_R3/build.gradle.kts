plugins {
    id("me.deecaad.mechanics-project")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":MechanicsCore"))
    adventureChatAPI()
    compileOnly(Dependencies.FOLIA_SCHEDULER)

    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(21)
    }
}