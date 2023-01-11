package me.deecaad.core.commands.wrappers;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;

public record BiomeHolder(Biome biome, NamespacedKey key) {
}
