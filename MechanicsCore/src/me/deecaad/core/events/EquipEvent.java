package me.deecaad.core.events;

import me.deecaad.core.utils.MaterialUtil;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An event that fires when a {@link org.bukkit.entity.LivingEntity} changes an
 * item in their equipment. An entity's equipment is a structure mapping an
 * {@link org.bukkit.inventory.EquipmentSlot} to an item.
 *
 * <p>As of writing this comment, this event is only fired for bukkit
 * {@link org.bukkit.entity.Player}s. This is subject to change.
 */
public class EquipEvent extends EntityEvent {

    public static final HandlerList HANDLERS = new HandlerList();

    private final EquipmentSlot slot;
    private final ItemStack dequipped;
    private ItemStack equipped;

    public EquipEvent(Entity what, EquipmentSlot slot, ItemStack dequipped, ItemStack equipped) {
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
    @Nonnull
    public EquipmentSlot getSlot() {
        return slot;
    }

    /**
     * Returns the item that was previously in that slot, or <code>null</code>.
     *
     * @return The nullable previous item.
     */
    @Nullable
    public ItemStack getDequipped() {
        return dequipped;
    }

    /**
     * Returns the item that is being equipped, or <code>null</code>.
     *
     * @return The nullable currently equipped item.
     */
    @Nullable
    public ItemStack getEquipped() {
        return equipped;
    }

    /**
     * Sets the item that is currently being equipped. Note that this method
     * should probably be avoided, as it will delete the item previously in
     * the slot.
     *
     * @param equipped The nullable item to equip.
     */
    public void setEquipped(@Nullable ItemStack equipped) {
        if (MaterialUtil.isAir(equipped.getType()))
            this.equipped = null;
        else
            this.equipped = equipped;
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
        switch (slot) {
            case HEAD: case CHEST: case LEGS: case FEET:
                return true;
            default:
                return false;
        }
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}