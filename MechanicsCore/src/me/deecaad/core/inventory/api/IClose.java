package me.deecaad.core.inventory.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface IClose {

    /**
     * Used to do something when window is being closed
     *
     * @param player    the player closing
     * @param inventory the inventory closen
     */
    void onClose(Player player, Inventory inventory);
}