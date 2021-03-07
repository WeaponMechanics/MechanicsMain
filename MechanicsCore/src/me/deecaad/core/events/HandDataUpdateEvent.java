package me.deecaad.core.events;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * Called when main hand or off hand item NBT is changed.
 *
 * This event is always ran async!
 */
public class HandDataUpdateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancel;
    private final Entity entity;
    private final boolean mainHand;
    private final ItemStack itemStack;
    private final ItemStack oldItemStack;

    public HandDataUpdateEvent(Entity what, EquipmentSlot slot, ItemStack itemStack, ItemStack oldItemStack) {
        super(true);
        this.entity = what;
        this.mainHand = slot == EquipmentSlot.HAND;
        this.itemStack = itemStack;
        this.oldItemStack = oldItemStack;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EntityType getEntityType() {
        return this.entity.getType();
    }

    public boolean isMainHand() {
        return mainHand;
    }

    public boolean isOffHand() {
        return !mainHand;
    }

    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Nullable
    public ItemStack getOldItemStack() {
        return oldItemStack;
    }

    public boolean isEquipping() {
        return itemStack != null && itemStack.getType() != Material.AIR;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    /**
     * Cancels PacketPlayOutSetSlot packet from being sent to player.
     * All changes are still made server-side.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
