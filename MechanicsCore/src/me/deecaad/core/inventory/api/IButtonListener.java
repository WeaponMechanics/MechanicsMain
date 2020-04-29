package me.deecaad.core.inventory.api;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface IButtonListener {

    /**
     * Cancel InventoryClickEvent if you wish so when this onClick(InventoryClickEvent) is ran
     *
     * @param window the window where click happened
     * @param event  the inventory click event used when clicked this button
     */
    void onClick(IWindow window, InventoryClickEvent event);

}