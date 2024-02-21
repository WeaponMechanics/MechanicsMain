plugins {
    id("me.deecaad.mechanics-project")
}

dependencies {
    implementation(project(":MechanicsCore"))
    compileOnly(files(file("../../lib/nms/spigot-1.16.5.jar")))
    adventureChatAPI()
}