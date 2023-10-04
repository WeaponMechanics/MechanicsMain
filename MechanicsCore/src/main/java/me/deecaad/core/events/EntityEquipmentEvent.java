package me.deecaad.core.events;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An event that fires when a {@link org.bukkit.entity.LivingEntity} changes an
 * item in their equipment. An entity's equipment is a structure mapping an
 * {@link org.bukkit.inventory.EquipmentSlot} to an item.
 *
 * <p>As of writing this comment, this event is only fired for bukkit
 * {@link org.bukkit.entity.Player}s. This is subject to change.
 */
public class EntityEquipmentEvent extends EntityEvent {

    public static final HandlerList HANDLERS = new HandlerList();

    private final EquipmentSlot slot;
    private final ItemStack dequipped;
    private final ItemStack equipped;

    public EntityEquipmentEvent(Entity what, EquipmentSlot slot, ItemStack dequipped, ItemStack equipped) {
        super(what);

        this.slot = slot;
        this.dequipped = dequipped;
        this.equipped = equipped;
    }

    /**
     * Returns the slot that the item is being equipped to. Note that prior to
     * 1.9, {@link EquipmentSlot#OFF_HAND} did not exist.
     *
     * @return The non-null slot the item is being equipped to/from.
     */
    @NotNull
    public EquipmentSlot getSlot() {
        return slot;
    }

    /**
     * Returns <code>true</code> if an item is being removed from the slot.
     * Note that an item may be dequipped at the same time one is equipped.
     *
     * @return <code>true</code> if an item is removed.
     * @see #isEquipping()
     */
    public boolean isDequipping() {
        return dequipped != null && dequipped.getType() != Material.AIR;
    }

    /**
     * Returns <code>true</code> if an item is being equipped to the slot.
     * Note that an item may be equipped at the same time one is dequipped.
     *
     * @return <code>true</code>
     * @see #isDequipping()
     */
    public boolean isEquipping() {
        return equipped != null && equipped.getType() != Material.AIR;
    }

    /**
     * Returns the item that was previously in that slot, or <code>null</code>.
     *
     * @return The nullable previous item.
     */
    public ItemStack getDequipped() {
        return dequipped;
    }

    /**
     * Returns the item that is being equipped, or <code>null</code>.
     *
     * @return The nullable currently equipped item.
     */
    public ItemStack getEquipped() {
        return equipped;
    }

    /**
     * Returns <code>true</code> if the slot involved is an armor slot.
     *
     * @return true if the slot is an armor slot.
     * @see #getSlot()
     */
    public boolean isArmor() {

        // While it would be better to check for offhand/mainhand only, that
        // may throw errors in minecraft 1.8.8
        return switch (slot) {
            case HEAD, CHEST, LEGS, FEET -> true;
            default -> false;
        };
    }

    @Override
    public String toString() {
        return "EquipEvent{" +
                "slot=" + slot +
                ", dequipped=" + dequipped +
                ", equipped=" + equipped +
                '}';
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}