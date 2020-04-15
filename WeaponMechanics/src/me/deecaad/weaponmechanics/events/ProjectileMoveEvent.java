package me.deecaad.weaponmechanics.events;

import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;

public class ProjectileMoveEvent extends WeaponMechanicsEvent {

    private final ICustomProjectile customProjectile;

    /**
     * Called whenever any custom projectile ticks
     *
     * @param customProjectile the custom projectile instance
     */
    public ProjectileMoveEvent(ICustomProjectile customProjectile) {
        this.customProjectile = customProjectile;
    }

    /**
     * @return the custom projectile instance
     */
    public ICustomProjectile getCustomProjectile() {
        return customProjectile;
    }
}