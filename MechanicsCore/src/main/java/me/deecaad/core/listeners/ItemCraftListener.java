package me.deecaad.core.listeners;

import me.deecaad.core.compatibility.CompatibilityAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ItemCraftListener implements Listener {

    // Easier to keep compatibility this way
    private static final Set<String> craftingInventories;

    static {

        // LAST UPDATE IN VERSION 1.18
        craftingInventories = new HashSet<>(Arrays.asList("ANVIL", "BEACON",
                "BLAST_FURNACE", "BREWING", "CARTOGRAPHY", "COMPOSTER", "CRAFTING", "ENCHANTING", "FURNACE",
                "GRINDSTONE", "LOOM", "MERCHANT", "SMITHING", "SMOKER", "STONECUTTER", "WORKBENCH"));
    }

    @EventHandler
    public void click(InventoryClickEvent event) {
        if (event.isCancelled()) return;

        InventoryType.SlotType slotType = event.getSlotType();
        if (slotType == InventoryType.SlotType.OUTSIDE) return;

        Inventory clickedInventory = event.getClickedInventory();

        InventoryView view = event.getView();
        Inventory playerInventory = view.getBottomInventory();

        // If "external" inventory is not open, this is player's CRAFTING inventory
        InventoryType topInventoryType = view.getTopInventory().getType();

        // 1) Deny shift + click from player inventory
        if ((event.getClick().isShiftClick() || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) // Check if shift click
                && clickedInventory == playerInventory // Check if clicked in player inventory
                && isCraftingInventory(topInventoryType) // Check if the top inventory is crafting inventory
                && isDenyCraftingItem(event.getCurrentItem())) { // Check if clicked item should be denied from moving
            event.setCancelled(true);
            return;
        }

        // 2) Deny basic inventory click
        if (clickedInventory != playerInventory // Check if clicked inventory isn't player inventory
                && isCraftingSlotType(slotType) // Check if clicked slot type was something that can be in crafting inventory
                && isDenyCraftingItem(event.getCursor())) { // Check if cursor item should be denied from moving
            event.setCancelled(true);
        }

        // todo check if there is need for extra checks on HOTBAR_MOVE_AND_READD / HOTBAR_SWAP
    }

    @EventHandler
    public void drag(InventoryDragEvent event) {
        if (event.isCancelled()) return;
        // 3) Deny when dragging

        // If "external" inventory is not open, this is player's CRAFTING inventory
        Inventory topInventory = event.getView().getTopInventory();

        // Check if top inventory is crafting inventory
        if (!isCraftingInventory(topInventory.getType())) return;

        // Check if the item should be denied
        if (!isDenyCraftingItem(event.getOldCursor())) return;

        // Now we iterate raw slots where item was being dragged to
        for (int rawSlot : event.getRawSlots()) {

            // Here are some pictures showing how raw slots are assigned
            // https://www.spigotmc.org/wiki/raw-slot-ids/

            // If the raw slow index is smaller than top inventory
            // size it, then this raw slot must be in the top inventory
            if (rawSlot < topInventory.getSize()) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void moveItem(InventoryMoveItemEvent event) {
        if (event.isCancelled()) return;

        // 4) Deny when anything (hopper mostly) tries to move item

        if (isCraftingInventory(event.getDestination().getType())
                && isDenyCraftingItem(event.getItem())) {
            event.setCancelled(true);
        }
    }

    private boolean isCraftingSlotType(InventoryType.SlotType slotType) {
        return slotType == InventoryType.SlotType.CRAFTING
                || slotType == InventoryType.SlotType.FUEL
                || slotType == InventoryType.SlotType.RESULT;
    }

    private boolean isCraftingInventory(InventoryType inventoryType) {
        return craftingInventories.contains(inventoryType.name());
    }

    private boolean isDenyCraftingItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        return CompatibilityAPI.getNBTCompatibility().getInt(itemStack, "MechanicsCore", "deny-crafting") == 1;
    }
}