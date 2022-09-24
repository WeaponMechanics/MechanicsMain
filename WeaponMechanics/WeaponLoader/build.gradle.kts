plugins {
    id("me.deecaad.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.1-R0.1-SNAPSHOT")
    compileOnly(project(":MechanicsCore", "default"))
    compileOnly(project(":WeaponMechanics", "default"))
    compileOnly("me.cjcrafter:mechanicsautodownload:1.2.2")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(8)
    }
}