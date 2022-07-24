package me.deecaad.weaponmechanics;

import me.cjcrafter.auto.AutoMechanicsDownload;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class WeaponMechanicsLoader extends JavaPlugin {

    private WeaponMechanics plugin;

    @Override
    public void onLoad() {
        int connect = getConfig().getInt("Mechanics_Core_Download.Read_Timeout", 10) * 1000;
        int read = getConfig().getInt("Mechanics_Core_Download.Connection_Timeout", 30) * 1000;
        AutoMechanicsDownload downloader = new AutoMechanicsDownload(connect, read);
        downloader.MECHANICS_CORE.install();

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
}
