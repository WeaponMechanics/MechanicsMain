package me.deecaad.core.utils

import org.bukkit.Bukkit
import java.lang.IllegalStateException

/**
 * https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-1-16/
 */
object MinecraftVersions {

    private val allUpdates = LinkedHashMap<String, Update>()
    private val allVersions = LinkedHashMap<String, Version>()

    @JvmStatic
    fun updates(): Map<String, Update> = allUpdates

    @JvmStatic
    fun versions(): Map<String, Version> = allVersions

    @JvmStatic
    lateinit var CURRENT: Version

    init {
        try {
            CURRENT = """\d+\.\d+\.\d+""".toRegex().find(Bukkit.getVersion())?.let {
                allVersions[it.value]
            } ?: throw IllegalStateException("Could not find any version associated with ${Bukkit.getVersion()}, map: $allVersions")
        } catch (npe: NullPointerException) {
            // occurs during testing
        }
    }

    /**
     * 1.12, the colorful blocks update (concrete)
     */
    @JvmStatic
    val WORLD_OF_COLOR = Update(1, 12) {
        add(Version(it, 0, 1)) // 1.12
        add(Version(it, 1, 1)) // 1.12.1
        add(Version(it, 2, 1)) // 1.12.2
    }

    /**
     * 1.13, ocean update (the flattening, waterloggable blocks, sprint swimming, brigadier commands)
     */
    @JvmStatic
    val UPDATE_AQUATIC = Update(1, 13) {
        add(Version(it, 0, 1)) // 1.13
        add(Version(it, 1, 2)) // 1.13.1
        add(Version(it, 2, 2)) // 1.13.2
    }

    /**
     * 1.14, villagers update (sneaking below slabs, new village generation)
     */
    @JvmStatic
    val VILLAGE_AND_PILLAGE = Update(1, 14) {
        add(Version(it, 0, 1)) // 1.14
        add(Version(it, 1, 1)) // 1.14.1
        add(Version(it, 2, 1)) // 1.14.2
        add(Version(it, 3, 1)) // 1.14.3
        add(Version(it, 4, 1)) // 1.14.4
    }

    /**
     * 1.15, bees update (bug fixes, bees)
     */
    @JvmStatic
    val BUZZY_BEES = Update(1, 15) {
        add(Version(it, 0, 1)) // 1.15
        add(Version(it, 1, 1)) // 1.15.1
        add(Version(it, 2, 1)) // 1.15.2
    }

    /**
     * 1.16, nether update (crimson, fungus, nether generation, biome fogs)
     */
    @JvmStatic
    val NETHER_UPDATE = Update(1, 16) {
        add(Version(it, 0, 1)) // 1.16
        add(Version(it, 1, 1)) // 1.16.1
        add(Version(it, 2, 2)) // 1.16.2
        add(Version(it, 3, 2)) // 1.16.3
        add(Version(it, 4, 3)) // 1.16.4
        add(Version(it, 5, 3)) // 1.16.5
    }

    /**
     * 1.17, caves and cliffs part 1 (tuff, new mobs, new blocks)
     */
    @JvmStatic
    val CAVES_AND_CLIFFS_1 = Update(1, 17) {
        add(Version(it, 0, 1)) // 1.17
        add(Version(it, 1, 1)) // 1.17.1
    }

    /**
     * 1.18, caves and cliffs part 2 (new generations)
     */
    @JvmStatic
    val CAVES_AND_CLIFFS_2 = Update(1, 18) {
        add(Version(it, 0, 1)) // 1.18
        add(Version(it, 1, 1)) // 1.18.1
        add(Version(it, 2, 2)) // 1.18.2
    }

    /**
     * 1.19, the deep dark update (sculk, warden, mud, mangrove, etc.)
     */
    @JvmStatic
    val WILD_UPDATE = Update(1, 19) {
        add(Version(it, 0, 1)) // 1.19
        add(Version(it, 1, 1)) // 1.19.1
        add(Version(it, 2, 1)) // 1.19.2
        add(Version(it, 3, 2)) // 1.19.3
        add(Version(it, 4, 3)) // 1.19.4
    }

    /**
     * 1.20, the archaeology update (cherry grove, sniffers, etc.)
     */
    @JvmStatic
    val TRAILS_AND_TAILS = Update(1, 20) {
        add(Version(it, 0, 1)) // 1.20
        add(Version(it, 1, 1)) // 1.20.1
        add(Version(it, 2, 2)) // 1.20.2
        add(Version(it, 3, 3)) // 1.20.3
        add(Version(it, 4, 3)) // 1.20.4
    }

    /**
     * Represents a "big" Minecraft update, e.x. 1.13 -> 1.14
     *
     * @property major The major version. Always 1.
     * @property minor The minor version. Always 12 or higher.
     * @property versions List of all patches for this update.
     */
    class Update(
        val major: Int,
        val minor: Int,
        versions: MutableList<Version>.(Update) -> Unit,
    ): Comparable<Update> {
        val versions: MutableList<Version> = mutableListOf<Version>().apply { versions(this@Update) }

        init {
            allUpdates[toString()] = this
            allVersions.putAll(this.versions.associateBy { it.toString() })
        }

        override fun compareTo(other: Update): Int {
            return when {
                // Skip major, since it is always 1
                minor > other.minor -> 1
                minor < other.minor -> -1
                else -> 0
            }
        }

        override fun toString(): String {
            return "$major.$minor"
        }
    }

    /**
     * Represents a patch for an [Update]. e.x. 1.16.4 -> 1.16.5
     *
     * @property update The parent update.
     * @property patch The patch number.
     * @property protocol The current protocol version (taken from R1, R2...)
     */
    data class Version(
        val update: Update,
        val patch: Int,
        val protocol: Int
    ): Comparable<Version> {
        val major = update.major
        val minor = update.minor

        override fun compareTo(other: Version): Int {
            return when {
                // Skip major, since it is always 1
                minor > other.minor -> 1
                minor < other.minor -> -1
                patch > other.patch -> 1
                patch < other.patch -> -1
                else -> 0
            }
        }

        override fun toString(): String {
            return "$major.$minor.$patch"
        }
    }
}