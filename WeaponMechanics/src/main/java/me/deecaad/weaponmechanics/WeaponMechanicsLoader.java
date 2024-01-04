package me.deecaad.weaponmechanics;

import me.deecaad.core.MechanicsCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class WeaponMechanicsLoader extends JavaPlugin {

    private WeaponMechanics plugin;

    @Override
    public void onLoad() {
        PluginManager pm = Bukkit.getPluginManager();

        // Only use AutoMechanicsDownload if Core isn't installed
        if (pm.getPlugin("MechanicsCore") == null) {
            getLogger().log(Level.SEVERE, "No MechanicsCore found, disabling");
            pm.disablePlugin(this);
            return;
        }

        // Sometimes, because we use softDepends in the plugin.yml,
        // WeaponMechanics will be loaded BEFORE MechanicsCore. To remedy
        // this, we try to manually load MechanicsCore.
        if (MechanicsCore.debug == null) {
            Plugin plugin = pm.getPlugin("MechanicsCore");
            plugin.onLoad();
        }

        // Now that we are sure that MechanicsCore is installed, we can
        // load WeaponMechanics
        plugin = new WeaponMechanics(this);
        plugin.onLoad();
    }

    @Override
    public void onDisable() {
        if (plugin != null) plugin.onDisable();
    }

    @Override
    public void onEnable() {
        if (plugin != null) plugin.onEnable();
    }
}
