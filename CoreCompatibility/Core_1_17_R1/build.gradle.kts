plugins {
    id("me.deecaad.java-conventions")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    implementation(project(":MechanicsCore"))
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")
    adventureChatAPI()
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(16)
    }
}