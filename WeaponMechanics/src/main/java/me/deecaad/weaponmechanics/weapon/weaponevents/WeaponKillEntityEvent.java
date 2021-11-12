package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * This class outlines the event of a projectile killing a {@link LivingEntity}.
 */
public class WeaponKillEntityEvent extends WeaponEvent {

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
}