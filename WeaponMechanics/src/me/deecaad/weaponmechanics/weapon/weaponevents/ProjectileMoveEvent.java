package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;

public class ProjectileMoveEvent extends ProjectileEvent {

    public ProjectileMoveEvent(ICustomProjectile projectile) {
        super(projectile);
    }

    public ICustomProjectile getProjectile() {
        return projectile;
    }
}