package me.deecaad.core.effects.types;

import me.deecaad.compatibility.CompatibilityAPI;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MinecraftSoundEffect extends SoundEffect {

    private final Sound sound;

    public MinecraftSoundEffect(Sound sound, float volume, float pitch, float pitchNoise) {
        super(pitch, pitchNoise, volume);

        this.sound = sound;
    }

    @Override
    public void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data) {
        if (CompatibilityAPI.getVersion() >= 1.11) {
            world.playSound(new Location(world, x, y, z), sound, SoundCategory.PLAYERS, volume, getRandomPitch());
        } else {
            world.playSound(new Location(world, x, y, z), sound, volume, getRandomPitch());
        }
    }

    @Override
    protected void spawnOnceFor(@Nonnull Plugin source, @Nonnull Player player, @Nonnull World world, double x, double y, double z, @Nullable Object data) {
        if (CompatibilityAPI.getVersion() >= 1.11) {
            player.playSound(new Location(world, x, y, z), sound, SoundCategory.PLAYERS, volume, getRandomPitch());
        } else {
            player.playSound(new Location(world, x, y, z), sound, volume, getRandomPitch());
        }
    }
}
