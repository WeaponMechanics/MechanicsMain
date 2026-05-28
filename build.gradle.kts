import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
    kotlin("jvm") version libs.versions.kotlin apply false
    base
}

allprojects {
    subprojects {
        pluginManager.withPlugin("java") {
            tasks.withType<JavaCompile>().configureEach {
                options.release.set(21)
                options.encoding = Charsets.UTF_8.name()
            }
        }

        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
                exceptionFormat = TestExceptionFormat.FULL
            }
        }

        tasks.withType<Javadoc>().configureEach {
            (options as StandardJavadocDocletOptions).apply {
                encoding = Charsets.UTF_8.name()
                charSet = Charsets.UTF_8.name()
                addStringOption("Xdoclint:none", "-quiet")
                links("https://docs.oracle.com/en/java/javase/21/docs/api/")
            }
        }
    }
}

