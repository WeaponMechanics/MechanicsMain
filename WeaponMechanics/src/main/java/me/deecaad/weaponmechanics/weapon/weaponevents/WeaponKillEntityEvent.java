package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This class outlines the event of a projectile killing a {@link LivingEntity}.
 */
public class WeaponKillEntityEvent extends WeaponEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity victim;
    private final WeaponDamageEntityEvent damageEvent;

    public WeaponKillEntityEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser, LivingEntity victim, WeaponDamageEntityEvent damageEvent) {
        super(weaponTitle, weaponItem, weaponUser);
        this.victim = victim;
        this.damageEvent = damageEvent;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    /**
     * Returns the {@link WeaponDamageEntityEvent} that was called before
     * killing the victim ({@link #getVictim()}).
     *
     * @return The damage event called right before this.
     */
    public WeaponDamageEntityEvent getDamageEvent() {
        return damageEvent;
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