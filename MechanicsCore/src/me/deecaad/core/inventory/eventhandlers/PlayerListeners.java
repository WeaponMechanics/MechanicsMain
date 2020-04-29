package me.deecaad.core.inventory.eventhandlers;

import me.deecaad.core.inventory.entitydata.InventoryEntityData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener {

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        InventoryEntityData.removeInventoryEntityData(e.getPlayer());
    }

}