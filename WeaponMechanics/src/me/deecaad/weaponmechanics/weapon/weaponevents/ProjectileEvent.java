package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import org.bukkit.Location;

/**
 * This class outlines convenient methods for any {@link WeaponEvent} that
 * involves an {@link ICustomProjectile}.
 */
public abstract class ProjectileEvent extends WeaponEvent {

    protected final ICustomProjectile projectile;

    protected ProjectileEvent(ICustomProjectile projectile) {
        super(projectile.getWeaponTitle(), projectile.getWeaponStack(), projectile.getShooter());

        this.projectile = projectile;
    }

    /**
     * Returns the projectile involved in this event.
     *
     * @return The non-null projectile.
     */
    public ICustomProjectile getProjectile() {
        return projectile;
    }

    /**
     * Returns the current location of the projectile.
     *
     * @return The non-null location of the projectile
     */
    public Location getLocation() {
        return projectile.getLocation().toLocation(projectile.getWorld());
    }

    /**
     * Returns the location of the projectile before it's most recent tick.
     *
     * @return The non-null location the projectile was 1 tick ago.
     */
    public Location getLastLocation() {
        return projectile.getLastLocation().toLocation(projectile.getWorld());
    }
}
