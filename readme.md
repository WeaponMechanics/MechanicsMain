# Weapon Mechanics [![Discord](https://img.shields.io/discord/306158221473742848.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://google.com)

---
High performance, fully featured gun plugin for newer Minecraft versions that makes
older gun plugins look like children's toys.

## Quick Links
* [Wiki](Wiki)
* [Download](Spigot)
* [Getting Support](Discord)
* [Bug report](github bug report)
* [Donate](#Donate)

---
## How to (Server Owners)
See the [Getting Started]() section on the wiki.

## How to (Developers)
See [Hooking into WeaponMechanics]() page on the wiki.  

Maven Repository:
```xml
    <repository>
        <id>papermc</id>
        <url>https://papermc.io/repo/repository/maven-public/</url>
    </repository>

    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.18.1-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
```

Using Gradle:
```kotlin
repositories {
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
}
```

---

## Donate
WeaponMechanics is a free to use software, and the combination of hundreds
of hours of work. If you like WeaponMechanics and want to see new features,
please consider donating.

* Donate Link 1

