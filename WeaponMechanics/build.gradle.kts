plugins {
    `maven-publish`
    signing
    id("io.codearte.nexus-staging") version "0.30.0"
    id("me.deecaad.java-conventions")
}

repositories {
    mavenCentral()
    maven(url = "https://mvn.lumine.io/repository/maven-public/")
    maven(url = "https://repo.jeff-media.com/public/")
}

dependencies {
    implementation(Dependencies.BSTATS)
    implementation("com.jeff_media:SpigotUpdateChecker:3.0.3")
    implementation(Dependencies.GSON)
    adventureChatAPI()

    compileOnly(Dependencies.LATEST_SPIGOT_API)
    compileOnly(project(":MechanicsCore"))
    compileOnly(Dependencies.PROTOCOL_LIB)
    compileOnly(Dependencies.PLACEHOLDER_API)
    compileOnly(Dependencies.MYTHIC_MOBS)
    compileOnly(Dependencies.VIVECRAFT)
    compileOnly(files(file("../lib/crackshot/CrackShotPlus.jar")))
    compileOnly(files(file("../lib/crackshot/CrackShot.jar")))
}

tasks.test {
    useJUnitPlatform()
}


// Create javadocJar and sourcesJar tasks
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.named("javadoc"))
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

nexusStaging {
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
    packageGroup = "com.cjcrafter"
    stagingProfileId = findProperty("OSSRH_ID").toString()
    username = findProperty("OSSRH_USERNAME").toString()
    password = findProperty("OSSRH_PASSWORD").toString()
    numberOfRetries = 30
    delayBetweenRetriesInMillis = 3000
}

// Signing artifacts
signing {
    isRequired = true
    //useGpgCmd()

    useInMemoryPgpKeys(
        findProperty("SIGNING_KEY_ID").toString(),
        findProperty("SIGNING_PRIVATE_KEY").toString(),
        findProperty("SIGNING_PASSWORD").toString()
    )
    //sign(configurations["archives"])
    sign(publishing.publications)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(javadocJar)
            artifact(sourcesJar)

            pom {
                name.set("WeaponMechanics")
                description.set("A new age of weapons in Minecraft")
                url.set("https://github.com/WeaponMechanics/MechanicsMain")

                groupId = "com.cjcrafter"
                artifactId = "weaponmechanics"
                version = findProperty("weaponMechanicsVersion") as? String ?: throw IllegalArgumentException("property was null")

                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("CJCrafter")
                        name.set("Collin Barber")
                        email.set("collinjbarber@gmail.com")
                    }
                    developer {
                        id.set("DeeCaaD")
                        name.set("DeeCaaD")
                        email.set("perttu.kangas@hotmail.fi")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/WeaponMechanics/MechanicsMain.git")
                    developerConnection.set("scm:git:ssh://github.com/WeaponMechanics/MechanicsMain.git")
                    url.set("https://github.com/WeaponMechanics/MechanicsMain")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = findProperty("OSSRH_USERNAME").toString()
                password = findProperty("OSSRH_PASSWORD").toString()
            }
        }
    }
}

// After publishing, the nexus plugin will automatically close and release
tasks.named("publish") {
    finalizedBy("closeAndReleaseRepository")
}


