package me.deecaad.core.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * These tests are more for verifying that new updates are added correctly.
 */
class MinecraftVersionTest {

    @ParameterizedTest
    @CsvSource(
        "Bukkit 1.12.2,1.12.2R1",
        "Paper (1.16.5),1.16.5R3",
        "Purpur 1.20.4,1.20.4R3",
    )
    fun `test parse version`(versionString: String, expected: String) {
        val version = MinecraftVersions.parseCurrentVersion(versionString)
        if ("${version}R${version.protocol}" != expected)
            fail { "Version was incorrect, expected $expected but got $version" }
    }

    @ParameterizedTest
    @CsvSource(
        "Bukkit version 12345",
        "Who knows",
    )
    fun `test parse version with invalid version`(versionString: String) {
        assertThrows<IllegalStateException> {
            MinecraftVersions.parseCurrentVersion(versionString)
        }
    }

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