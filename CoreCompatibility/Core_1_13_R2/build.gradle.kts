plugins {
    id("me.deecaad.mechanics-project")
}

dependencies {
    implementation(project(":MechanicsCore"))
    compileOnly(files(file("../../lib/nms/spigot-1.13.2.jar")))
    adventureChatAPI()
}