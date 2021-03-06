package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This class outlines the event of a projectile ending. A projectile may end
 * if it goes too deep into the void/sky, if it has existed for too long, if
 * it has hit a {@link org.bukkit.block.Block} or an
 * {@link org.bukkit.entity.Entity}, etc.
 */
public class ProjectileEndEvent extends ProjectileEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public ProjectileEndEvent(WeaponProjectile projectile) {
        super(projectile);
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
