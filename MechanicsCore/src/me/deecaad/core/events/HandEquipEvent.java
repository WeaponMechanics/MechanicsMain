package me.deecaad.core.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * This class outlines a bukkit event that occurs when a bukkit player equips
 * or removes an item to/from an main hand or off hand. This event is called 1 tick after
 * receiving the packet.
 */
public class HandEquipEvent extends EquipEvent {

    private static HandlerList handlerList = new HandlerList();

    private final boolean mainHand;

    public HandEquipEvent(Entity what, ItemStack itemStack, EquipmentSlot slot) {
        super(what, itemStack);
        mainHand = slot == EquipmentSlot.HAND;
    }

    public boolean isMainHand() {
        return mainHand;
    }

    public boolean isOffHand() {
        return !mainHand;
    }
}