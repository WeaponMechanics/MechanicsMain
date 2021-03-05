package me.deecaad.core.events;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class EquipEvent extends EntityEvent {

    private static HandlerList handlerList = new HandlerList();

    private final ItemStack itemStack;

    public EquipEvent(Entity what, ItemStack itemStack) {
        super(what);
        this.itemStack = itemStack;
    }

    /**
     * The item being equipped, or <code>null</code> if the item is being
     * unequipped.
     *
     * @return The item involved.
     */
    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * @return True if the item being equipped is not <code>AIR</code>
     */
    public boolean isEquipping() {
        return itemStack != null && itemStack.getType() != Material.AIR;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}