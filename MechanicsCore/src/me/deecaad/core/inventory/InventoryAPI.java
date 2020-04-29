package me.deecaad.core.inventory;

import me.deecaad.core.inventory.entitydata.InventoryEntityData;
import me.deecaad.core.inventory.eventhandlers.InventoryListeners;
import me.deecaad.core.inventory.eventhandlers.PlayerListeners;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class InventoryAPI {

    private static Plugin plugin;

    /**
     * Initiate InventoryAPI using plugin. Can not be initiated twice.
     *
     * @param initPlugin                    the plugin used to init InventoryAPI
     * @param ignoreAlreadyInitiatedMessage true if you don't want already initiated message
     */
    public static void init(Plugin initPlugin, boolean ignoreAlreadyInitiatedMessage) {
        if (plugin != null) {
            if (!ignoreAlreadyInitiatedMessage) {
                //
            }
            return;
        }
        plugin = initPlugin;
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryListeners(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListeners(), plugin);
    }

    /**
     * This should be called when shutting down server!
     */
    public static void onDisable() {
        InventoryEntityData.shutdownInventoryEntityData();
        plugin = null;
    }

    /**
     * @return the plugin which initiated InventoryAPI
     */
    public static Plugin getPlugin() {
        return plugin;
    }
}