package me.deecaad.core;

import me.deecaad.core.packetlistener.PacketListenerAPI;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.Debugger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class MechanicsCore extends JavaPlugin {

    private static Plugin plugin;

    // public so people can import a static variable
    public static Debugger debug;

    @Override
    public void onEnable() {
        plugin = this;
        debug = new Debugger(getLogger(), 2);
        new PacketListenerAPI(this);
    }

    @Override
    public void onDisable() {
        PlaceholderAPI.onDisable();
        PacketListenerAPI.onDisable();
        plugin = null;
    }

    /**
     * @return the MechanicsCore plugin instance
     */
    public static Plugin getPlugin() {
        return plugin;
    }
}
