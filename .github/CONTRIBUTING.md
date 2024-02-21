# Welcome to the contributing guide
Thank you for considering contributing to this project. We appreciate your time and effort. 
We have a few guidelines to help you get started.

WeaponMechanics leverages the following technologies:
  - [Gradle](https://gradle.org/)
    - Handling modules, dependencies, and other build tasks 
  - [Kotlin](https://kotlinlang.org/) 
    - [Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) for Gradle
    - Use `@NotNull` and `@Nullable` annotations for null safety
    - Use Kotlin for utility classes, and other appropriate classes
  - [paperweight-userdev](https://github.com/PaperMC/paperweight-test-plugin)
    - Automatically download and remaps server jars for 1.17.2+ 
  - [brigadier](https://github.com/Mojang/brigadier)
    - Command parsing and handling

## New contributor guide

When cloning the repository, you will need to either:
1. Set `devMode=true` in [gradle.properties](../gradle.properties) to disable legacy versions.
2. Use [download-spigot-libs.bat](../lib/download-spigot-libs.bat) to build legacy versions.

This will allow you to compile the plugin. Make sure to reload the gradle project
after making changes to gradle files. To build the `.jar` files, run
```shell
./gradlew buildForSpigotRelease
```

## Making a pull request

When making a pull request, GitHub Actions will automatically run the following
checks on your code:
```shell
./gradlew build
./gradlew test
./gradlew spotlessCheck
```

If any of these checks fail, you will need to fix the issues before your pull
request can be merged. For "spotlessCheck", you can run `./gradlew spotlessApply`
to automatically address your code formatting issues.
