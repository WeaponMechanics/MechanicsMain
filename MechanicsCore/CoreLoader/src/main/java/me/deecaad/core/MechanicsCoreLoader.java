package me.deecaad.core;

import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class MechanicsCoreLoader extends JavaPlugin {

    private MechanicsCore plugin;

    @Override
    public void onLoad() {
        PluginManager pm = Bukkit.getPluginManager();

        // Check java version... WeaponMechanics only supports using java 16
        // or higher. This check is done now, so we can disable the plugin
        // before an error occurs
        if (ReflectionUtil.getJavaVersion() < 16) {
            getLogger().log(Level.SEVERE, "Cannot use java version " + ReflectionUtil.getJavaVersion() + " with WeaponMechanics");
            getLogger().log(Level.SEVERE, "Found Java Version: " + System.getProperty("java.version"));
            getLogger().log(Level.SEVERE, "WeaponMechanics requires AT LEAST java 16 in order to run (you can run newer versions)");
            getLogger().log(Level.SEVERE, "Update Java for Local Servers: https://www.java.com/en/download/help/java_update.html");
            getLogger().log(Level.SEVERE, "If you are using a server host, contact customer support for instructions on how to update to the latest java version");

            pm.disablePlugin(this);
            return;
        }

        plugin = new MechanicsCore(this);
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
