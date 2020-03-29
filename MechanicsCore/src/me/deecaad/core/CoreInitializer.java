package me.deecaad.core;

import me.deecaad.core.packetlistener.PacketListenerAPI;
import me.deecaad.core.placeholder.PlaceholderAPI;
import org.bukkit.plugin.Plugin;

/**
 * Simple class to make initializing core easier
 */
public class CoreInitializer {

    /**
     * This initializes all core features
     *
     * @param plugin the plugin used to init
     */
    public void init(Plugin plugin) {
        new PacketListenerAPI(plugin);
    }

    /**
     * This clears all data from all core features
     */
    public void onDisable() {
        PlaceholderAPI.onDisable();
        PacketListenerAPI.onDisable();
    }
}
