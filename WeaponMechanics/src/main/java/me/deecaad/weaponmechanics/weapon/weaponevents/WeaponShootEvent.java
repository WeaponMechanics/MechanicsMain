package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;

import javax.annotation.Nonnull;

/**
 * This class outlines the event of a weapon launching a projectile.
 */
public class WeaponShootEvent extends WeaponEvent {

    private WeaponProjectile projectile;

    public WeaponShootEvent(WeaponProjectile projectile) {
        super(projectile.getWeaponTitle(), projectile.getWeaponStack(), projectile.getShooter());

        this.projectile = projectile;
    }

    public WeaponProjectile getProjectile() {
        return projectile;
    }

    public void setProjectile(@Nonnull WeaponProjectile projectile) {
        if (projectile == null)
            throw new IllegalArgumentException("projectile cannot be null");

        this.projectile = projectile;
    }
}
