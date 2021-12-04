package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;

import javax.annotation.Nonnull;

/**
 * This class outlines the event of a weapon launching a projectile.
 */
public class WeaponShootEvent extends WeaponEvent {

    private ICustomProjectile projectile;

    public WeaponShootEvent(ICustomProjectile projectile) {
        super(projectile.getWeaponTitle(), projectile.getWeaponStack(), projectile.getShooter());

        this.projectile = projectile;
    }

    public ICustomProjectile getProjectile() {
        return projectile;
    }

    public void setProjectile(@Nonnull ICustomProjectile projectile) {
        if (projectile == null)
            throw new IllegalArgumentException("projectile cannot be null");

        this.projectile = projectile;
    }
}
