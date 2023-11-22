import gradle.kotlin.dsl.accessors._257d6030bdcc0b367a335c8e2149827a.compileOnly

object Versions {

    // Spigot + Plugins
    const val LATEST_SPIGOT_API = "1.20.2-R0.1-SNAPSHOT"
    const val BSTATS = "3.0.1"
    const val PROTOCOL_LIB = "5.1.0"
    const val PLACEHOLDER_API = "2.11.3"
    const val MYTHIC_MOBS = "5.3.5"
    const val GEYSER = "2.2.0-SNAPSHOT"

    // Adventure Chat API
    const val ADVENTURE_API = "4.15.0-SNAPSHOT"
    const val ADVENTURE_BUKKIT = "4.3.1"
    const val ADVENTURE_TEXT_LEGACY = ADVENTURE_API
    const val ADVENTURE_TEXT_MINIMESSAGE = ADVENTURE_API

    // Misc
    const val JSON_SIMPLE = "1.1.1"
    const val ANNOTATIONS = "24.0.1"
}

object Dependencies {

    // Spigot + Plugins
    const val LATEST_SPIGOT_API = "org.spigotmc:spigot-api:${Versions.LATEST_SPIGOT_API}"
    const val BSTATS = "org.bstats:bstats-bukkit:${Versions.BSTATS}"
    const val PROTOCOL_LIB = "com.comphenix.protocol:ProtocolLib:${Versions.PROTOCOL_LIB}"
    const val PLACEHOLDER_API = "me.clip:placeholderapi:${Versions.PLACEHOLDER_API}"
    const val MYTHIC_MOBS = "io.lumine:Mythic-Dist:${Versions.MYTHIC_MOBS}"
    const val GEYSER = "org.geysermc.geyser:api:${Versions.GEYSER}"

    // Adventure Chat API
    const val ADVENTURE_API = "net.kyori:adventure-api:${Versions.ADVENTURE_API}"
    const val ADVENTURE_BUKKIT = "net.kyori:adventure-platform-bukkit:${Versions.ADVENTURE_BUKKIT}"
    const val ADVENTURE_TEXT_LEGACY = "net.kyori:adventure-text-serializer-legacy:${Versions.ADVENTURE_API}"
    const val ADVENTURE_TEXT_MINIMESSAGE = "net.kyori:adventure-text-minimessage:${Versions.ADVENTURE_API}"

    // Misc
    const val JSON_SIMPLE = "com.googlecode.json-simple:json-simple:${Versions.JSON_SIMPLE}"
    const val ANNOTATIONS = "org.jetbrains:annotations:${Versions.ANNOTATIONS}"
}

fun org.gradle.api.artifacts.dsl.DependencyHandler.adventureChatAPI() {
    compileOnly(Dependencies.ADVENTURE_API)
    compileOnly(Dependencies.ADVENTURE_BUKKIT)
    compileOnly(Dependencies.ADVENTURE_TEXT_LEGACY)
    compileOnly(Dependencies.ADVENTURE_TEXT_MINIMESSAGE)
}