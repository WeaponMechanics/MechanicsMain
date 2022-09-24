package me.deecaad.weaponmechanics;

import me.cjcrafter.auto.AutoMechanicsDownload;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.ReflectionUtil;
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

        // Only use AutoMechanicsDownload if Core isn't installed
        if (pm.getPlugin("MechanicsCore") != null) {
            try {
                int connect = getConfig().getInt("Mechanics_Core_Download.Read_Timeout", 10) * 1000;
                int read = getConfig().getInt("Mechanics_Core_Download.Connection_Timeout", 30) * 1000;
                AutoMechanicsDownload downloader = new AutoMechanicsDownload(connect, read);
                downloader.MECHANICS_CORE.install();
            } catch (Throwable e) {
                getLogger().log(Level.WARNING, "Failed to use auto-installer", e);
            }

            // Installation failed...
            if (pm.getPlugin("MechanicsCore") == null) {
                getLogger().log(Level.SEVERE, "No MechanicsCore found, disabling");
                pm.disablePlugin(this);
                return;
            }
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
