package me.deecaad.compatibility.worldguard;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;

public class WorldGuardAPI {

    private static IWorldGuardCompatibility worldGuardCompatibility;

    static {
        try {
            // Check if WorldGuard is there
            Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");

            // Kinda hacky way to use reflections on own code, but when V6 module and V7 module was both added into lib of MechanicsCompatibility
            // I couldn't compile the code because of odd BukkitAdapter thing
            if (CompatibilityAPI.getVersion() < 1.13) {
                // V6
                Constructor<?> worldGuardV6Constructor = ReflectionUtil.getConstructor(Class.forName("me.deecaad.compatibility.worldguard.WorldGuardV6"));
                worldGuardCompatibility = (IWorldGuardCompatibility) ReflectionUtil.newInstance(worldGuardV6Constructor);
            } else {
                // V7
                Constructor<?> worldGuardV7Constructor = ReflectionUtil.getConstructor(Class.forName("me.deecaad.compatibility.worldguard.WorldGuardV7"));
                worldGuardCompatibility = (IWorldGuardCompatibility) ReflectionUtil.newInstance(worldGuardV7Constructor);
            }
        } catch (ClassNotFoundException e) {
            worldGuardCompatibility = new NoWorldGuard();
        }
        if (worldGuardCompatibility == null) {
            // If for some reason creating new instance didn't work?
            // There will be errors in console if it didn't work, but lets give default value
            worldGuardCompatibility = new NoWorldGuard();
        }
    }

    /**
     * @return The cached <code>IWorldGuardCompatibility</code>
     */
    @Nonnull
    public static IWorldGuardCompatibility getWorldGuardCompatibility() {
        return worldGuardCompatibility;
    }
}