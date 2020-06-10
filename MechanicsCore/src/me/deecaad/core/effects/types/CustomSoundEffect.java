package me.deecaad.core.effects.types;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class CustomSoundEffect extends SoundEffect {

    private static Method worldGetHandle;
    private static Method makeSoundMethod;

    static {
        worldGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftWorld"), "getHandle");
        makeSoundMethod = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("World"), "makeSound", double.class, double.class, double.class, String.class, float.class, float.class);
    }

    private final String sound;

    public CustomSoundEffect(String sound, float volume, float pitch, float pitchNoise) {
        super(pitch, pitchNoise, volume);
        this.sound = sound;
    }

    @Override
    public void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data) {
        if (CompatibilityAPI.getVersion() >= 1.11) {
            world.playSound(new Location(world, x, y, z), sound, SoundCategory.PLAYERS, volume, getRandomPitch());
        } else if (CompatibilityAPI.getVersion() >= 1.09) {
            world.playSound(new Location(world, x, y, z), sound, volume, getRandomPitch());
        } else {
            Object worldServer = ReflectionUtil.invokeMethod(worldGetHandle, world);
            ReflectionUtil.invokeMethod(makeSoundMethod, worldServer, x, y, z, sound, volume, getRandomPitch());
        }
    }
}
