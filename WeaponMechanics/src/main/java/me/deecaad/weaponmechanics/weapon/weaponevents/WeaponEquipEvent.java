package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This class outlines the event of a player holding a weapon. This can be done
 * by picking up the item, switching the hot bar slots, etc.
 */
public class WeaponEquipEvent extends WeaponEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final boolean mainHand;

    public WeaponEquipEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter, boolean mainHand) {
        super(weaponTitle, weaponStack, shooter);
        this.mainHand = mainHand;
    }

    public boolean isMainHand() {
        return mainHand;
    }

    public boolean isOffHand() {
        return !mainHand;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}