package me.deecaad.weaponmechanics.compatibility;

import org.bukkit.Bukkit;

public final class VivecraftCompatibility {

    public static final String PLUGIN_NAME = "Vivecraft-Spigot-Extension";

    private VivecraftCompatibility() {
    }

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME);
    }
}
