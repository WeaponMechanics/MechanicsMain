package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;

/**
 * This class outlines the event of a projectile ending. A projectile may end
 * if it goes too deep into the void/sky, if it has existed for too long, if
 * it has hit a {@link org.bukkit.block.Block} or an
 * {@link org.bukkit.entity.Entity}, etc.
 */
public class ProjectileEndEvent extends ProjectileEvent {

    public ProjectileEndEvent(ICustomProjectile projectile) {
        super(projectile);
    }
}
