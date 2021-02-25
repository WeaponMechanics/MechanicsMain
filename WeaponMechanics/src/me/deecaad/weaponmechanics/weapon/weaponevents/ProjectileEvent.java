package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import org.bukkit.Location;

public abstract class ProjectileEvent extends WeaponEvent {

    protected final ICustomProjectile projectile;

    protected ProjectileEvent(ICustomProjectile projectile) {
        super(projectile.getWeaponTitle(), projectile.getWeaponStack(), projectile.getShooter());

        this.projectile = projectile;
    }

    public ICustomProjectile getProjectile() {
        return projectile;
    }

    public Location getLocation() {
        return projectile.getLocation().toLocation(projectile.getWorld());
    }

    public Location getLastLocation() {
        return projectile.getLastLocation().toLocation(projectile.getWorld());
    }
}
