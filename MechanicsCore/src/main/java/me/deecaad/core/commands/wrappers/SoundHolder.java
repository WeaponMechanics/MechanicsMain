package me.deecaad.core.commands.wrappers;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps a sound. If the sound is a vanilla sound supported by Spigot, <code>sound</code> will store
 * that sound. Named sounds are stored as null.
 *
 * @param sound The vanilla sound, or null.
 * @param key The key of the sound.
 */
public record SoundHolder(@Nullable Sound sound, @NotNull NamespacedKey key) {
}
