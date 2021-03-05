package me.deecaad.core.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * This class outlines a bukkit event that occurs when a bukkit player equips
 * or removes an item to/from an armor slot. This event is called 1 tick after
 * receiving the packet.
 */
public class ArmorEquipEvent extends EquipEvent {

    private static HandlerList handlerList = new HandlerList();

    private final EquipmentSlot slot;

    public ArmorEquipEvent(Entity what, ItemStack itemStack, EquipmentSlot slot) {
        super(what, itemStack);
        this.slot = slot;
    }

    /**
     * Returns the non-null slot that the item is being equipped/unequipped
     * from.
     *
     * @return The slot involved.
     */
    public EquipmentSlot getSlot() {
        return slot;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
