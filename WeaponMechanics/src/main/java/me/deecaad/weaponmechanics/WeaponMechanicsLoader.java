package me.deecaad.weaponmechanics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.logging.Level;

public class WeaponMechanicsLoader extends JavaPlugin {

    private WeaponMechanics plugin;

    @Override
    public void onLoad() {
        ensureMechanicsCore();
        if (Bukkit.getPluginManager().getPlugin("MechanicsCore") == null) {
            return;
        }

        plugin = new WeaponMechanics(this);

        plugin.onLoad();
    }

    @Override
    public void onDisable() {
        plugin.onDisable();
    }

    @Override
    public void onEnable() {
        plugin.onEnable();
    }

    ClassLoader getClassLoader0() {
        return getClassLoader();
    }

    File getFile0() {
        return getFile();
    }

    private void ensureMechanicsCore() {

        // WeaponMechanics NEEDS MechanicsCore to run, however, people have the
        // incredible ability of having 0 abilities. AKA, they cannot read
        // "UnknownDependencyException". Instead of writing out a stacktrace
        // and having some people ask for help, we should:
        //      A. Try to download/copy/install MechanicsCore for them
        //      B. Disable the plugin
        if (Bukkit.getPluginManager().getPlugin("MechanicsCore") == null) {

            getLogger().log(Level.WARNING, "Missing MechanicsCore.jar, we will try to install it automatically",
                    "To disable this, go to the WeaponMechanics config.yml file");
            boolean installed = false;

            // try to install
            String link = "https://github.com/DeeCaaD/MechanicsMain/releases/download/v1.0.0/MechanicsCore-1.0.0.jar";
            if (getConfig().getBoolean("Mechanics_Core_Download.Enable", true)) {
                try {
                    URL url = new URL(link);
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(getConfig().getInt("Mechanics_Core_Download.Connection_Timeout", 10) * 1000); // 10 seconds
                    connection.setReadTimeout(getConfig().getInt("Mechanics_Core_Download.Read_Timeout", 30) * 1000); // 30 seconds

                    InputStream in = connection.getInputStream();
                    File target = new File(getDataFolder().getParent(), link.substring(link.lastIndexOf("/") + 1));
                    Files.copy(in, target.toPath());

                    Plugin plugin = Bukkit.getPluginManager().loadPlugin(target);
                    assert plugin != null;
                    plugin.onLoad();
                    installed = true;

                } catch (IOException ex) {
                    getLogger().log(Level.SEVERE, "Some error occurred while downloading MechanicsCore.jar automatically...",
                            "Please try downloading it manually from " + link);
                    getLogger().log(Level.WARNING, "Caught error: ", ex);
                } catch (InvalidPluginException | InvalidDescriptionException e) {
                    getLogger().log(Level.SEVERE, "Downloaded MechanicsCore.jar, but it was invalid... Perhaps there were connection issues?");
                }
            } else {
                getLogger().log(Level.INFO, "Skipping MechanicsCore.jar download due to config");
            }

            if (installed) {
                getLogger().log(Level.INFO, "Installed MechanicsCore.jar successfully!");
                return;
            }

            // Debugger has not been setup yet, use logger manually
            getLogger().log(Level.SEVERE, " !!!");
            getLogger().log(Level.SEVERE, "WeaponMechanics requires MechanicsCore in order to run!");
            getLogger().log(Level.SEVERE, "You should have gotten a 'MechanicsCore.jar' file along");
            getLogger().log(Level.SEVERE, "with 'WeaponMechanics.jar' in the zip file! Make sure you");
            getLogger().log(Level.SEVERE, "put BOTH files in the plugins folder!");
            getLogger().log(Level.SEVERE, "Disabling WeaponMechanics to avoid error.");

            getPluginLoader().disablePlugin(this);
        }
    }
}
