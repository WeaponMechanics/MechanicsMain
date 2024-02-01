package me.deecaad.core.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

/**
 * These tests are more for verifying that new updates are added correctly.
 */
class MinecraftVersionTest {

    @Test
    fun `ensure update order is increasing`() {
        var previous: MinecraftVersions.Update? = null
        for (update in MinecraftVersions.updates().values) {
            if (previous != null && previous > update)
                fail { "Update order was incorrect, $previous came before $update" }

            previous = update
        }
    }

    @Test
    fun `ensure version order is increasing`() {
        var previous: MinecraftVersions.Version? = null
        for (version in MinecraftVersions.versions().values) {
            if (previous != null && previous > version)
                fail { "Version order was incorrect, $previous came before $version" }

            previous = version
        }
    }
}