package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;

/**
 * This class outlines the event of a projectile being ticked. This event
 * occurs once per {@link ICustomProjectile} per tick (Yes, this may cause
 * performance issues).
 *
 * <p>This event is not always called, and can be disabled in the
 * <code>config.yml</code>.
 */
public class ProjectileMoveEvent extends ProjectileEvent {

    public ProjectileMoveEvent(ICustomProjectile projectile) {
        super(projectile);
    }

    public ICustomProjectile getProjectile() {
        return projectile;
    }
}