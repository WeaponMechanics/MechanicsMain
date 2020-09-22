package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;

public class ProjectileEndEvent extends ProjectileEvent {

    public ProjectileEndEvent(ICustomProjectile projectile) {
        super(projectile);
    }
}
