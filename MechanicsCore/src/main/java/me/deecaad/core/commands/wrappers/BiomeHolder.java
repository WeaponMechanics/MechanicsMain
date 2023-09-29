package me.deecaad.core.commands.wrappers;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps a biome. If the biome is a vanilla biome supported by Spigot,
 * <code>biome</code> will store that biome. Custom biomes are stored as null.
 *
 * @param biome The vanilla biome, or null.
 * @param key   The key of the biome.
 */
public record BiomeHolder(@Nullable Biome biome, @NotNull NamespacedKey key) {
}
