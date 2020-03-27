package me.deecaad.weaponmechanicsplus;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class WeaponMechanicsPlus extends JavaPlugin {

    private static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
    }

    @Override
    public void onDisable() {
        plugin = null;
    }

    public void onReload() {

    }

    /**
     * @return the WeaponMechanicsPlus plugin instance
     */
    public static Plugin getPlugin() {
        return plugin;
    }
}
