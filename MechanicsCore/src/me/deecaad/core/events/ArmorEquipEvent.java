package me.deecaad.core.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

// todo entity equip event
public class ArmorEquipEvent extends EntityEvent {

    private static HandlerList handlerList = new HandlerList();

    private final ArmorSlot slot;
    private final ItemStack armor;

    public ArmorEquipEvent(Entity what, ArmorSlot slot, ItemStack armor) {
        super(what);

        this.slot = slot;
        this.armor = armor;
    }

    public ArmorSlot getSlot() {
        return slot;
    }

    public ItemStack getArmor() {
        return armor;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public enum ArmorSlot {
        HEAD,
        CHEST,
        LEGS,
        FEET;

        public EquipmentSlot asEquipmentSlot() {
            return EquipmentSlot.valueOf(name());
        }
    }
}
