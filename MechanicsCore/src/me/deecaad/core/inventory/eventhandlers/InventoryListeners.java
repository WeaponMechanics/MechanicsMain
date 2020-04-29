package me.deecaad.core.inventory.eventhandlers;

import me.deecaad.core.inventory.api.IButtonListener;
import me.deecaad.core.inventory.api.IWindow;
import me.deecaad.core.inventory.api.WindowButton;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListeners implements Listener {

    @EventHandler
    public void click(InventoryClickEvent e) {
        Inventory clickedInventory = e.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }
        InventoryHolder clickedInventoryHolder = clickedInventory.getHolder();
        if (clickedInventoryHolder != null && clickedInventoryHolder instanceof IWindow) {
            IWindow window = (IWindow) clickedInventoryHolder;
            WindowButton windowButton = window.getWindowButton(e.getSlot());
            if (windowButton == null) {
                if (window.isCancelNonButtonSlotClicks()) {
                    e.setCancelled(true);
                }
            } else {
                if (window.isCancelButtonSlotClicks()) {
                    e.setCancelled(true);
                }
                IButtonListener buttonListener = windowButton.getButtonListener();
                if (buttonListener != null) {
                    buttonListener.onClick(window, e);
                }
            }
        }
        Inventory inventory = e.getInventory();
        if (inventory == null) {
            return;
        }
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if (inventoryHolder != null && inventoryHolder instanceof IWindow && !inventory.equals(clickedInventory) && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            IWindow window = (IWindow) inventoryHolder;
            if (window.isCancelNonButtonSlotClicks() || window.isCancelButtonSlotClicks()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void drag(InventoryDragEvent e) {
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if (inventoryHolder != null && inventoryHolder instanceof IWindow) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent e) {
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if (inventoryHolder != null && inventoryHolder instanceof IWindow) {
            ((IWindow) inventoryHolder).onClose((Player) e.getPlayer(), e.getInventory());
        }
    }
}