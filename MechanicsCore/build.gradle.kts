plugins {
    `maven-publish`
    signing
    id("io.codearte.nexus-staging") version "0.30.0"
    id("me.deecaad.java-conventions")
}

repositories {
    mavenCentral()
    maven(url = "https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    compileOnly(Dependencies.LATEST_SPIGOT_API)
    compileOnly(Dependencies.PLACEHOLDER_API)
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("io.netty:netty-all:4.1.90.Final")
    compileOnly("com.mojang:brigadier:1.0.18")

    compileOnly(Dependencies.PROTOCOL_LIB)
    compileOnly(Dependencies.GEYSER)

    implementation(Dependencies.JSON_SIMPLE)

    implementation(Dependencies.ADVENTURE_API)
    implementation(Dependencies.ADVENTURE_BUKKIT)
    implementation(Dependencies.ADVENTURE_TEXT_LEGACY)
    implementation(Dependencies.ADVENTURE_TEXT_MINIMESSAGE)
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.slf4j:slf4j-nop:1.7.30")
    implementation(Dependencies.MYTHIC_MOBS)

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation(Dependencies.LATEST_SPIGOT_API)
    testImplementation(Dependencies.ANNOTATIONS)
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
                name.set("MechanicsCore")
                description.set("Library plugin for WeaponMechanics containing Brigadier Commands, Mechanics, and more")
                url.set("https://github.com/WeaponMechanics/MechanicsMain")



                groupId = "com.cjcrafter"
                artifactId = "mechanicscore"
                version = findProperty("mechanicsCoreVersion") as? String ?: throw IllegalArgumentException("property was null")

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

