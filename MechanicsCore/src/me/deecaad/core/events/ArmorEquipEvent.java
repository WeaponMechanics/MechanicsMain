package me.deecaad.core.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * This class outlines a bukkit event that occurs when a bukkit player equips
 * or removes an item to/from an armor slot. This event is called 1 tick after
 * receiving the packet.
 */
public class ArmorEquipEvent extends EntityEvent {

    private static HandlerList handlerList = new HandlerList();

    private final ArmorSlot slot;
    private final ItemStack armor;

    /**
     * The constructor.
     *
     * @param what  The bukkit entity that is equipping the armor.
     * @param slot  Which slot the item is being equipped to.
     * @param armor The item being equipped, or null if the item is unequipped.
     */
    public ArmorEquipEvent(Entity what, ArmorSlot slot, ItemStack armor) {
        super(what);

        this.slot = slot;
        this.armor = armor;
    }

    /**
     * Returns the non-null slot that the item is being equipped/unequipped
     * from.
     *
     * @return The slot involved.
     */
    public ArmorSlot getSlot() {
        return slot;
    }

    /**
     * The item being equipped, or <code>null</code> if the item is being
     * unequipped. The equipped item isn't always armor, as it can be a
     * player head, a pumpkin, or a block placed there by a plugin.
     *
     * @return The item involved.
     */
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

    /**
     * This enum outlines an armor slot belonging to a bukkit living entity.
     *
     * @see EquipmentSlot
     */
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
