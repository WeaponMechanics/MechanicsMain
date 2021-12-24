package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;

/**
 * This class outlines the event of a projectile being ticked. This event
 * occurs once per {@link WeaponProjectile} per tick (Yes, this may cause
 * performance issues).
 *
 * <p>This event is not always called, and can be disabled in the
 * <code>config.yml</code>.
 */
public class ProjectileMoveEvent extends ProjectileEvent {

    public ProjectileMoveEvent(WeaponProjectile projectile) {
        super(projectile);
    }

    public WeaponProjectile getProjectile() {
        return projectile;
    }
}