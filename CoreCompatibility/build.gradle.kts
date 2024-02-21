plugins {
    id("me.deecaad.mechanics-project")
}

dependencies {
    implementation(project(":MechanicsCore"))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
}
