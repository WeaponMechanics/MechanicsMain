plugins {
    id("me.deecaad.java-conventions")
}

dependencies {
    implementation(project(":MechanicsCore"))
    compileOnly(files(file("../../lib/nms/spigot-1.15.2.jar")))
    adventureChatAPI()
}