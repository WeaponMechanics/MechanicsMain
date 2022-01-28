package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This class outlines the event of a projectile being ticked. This event
 * occurs once per {@link WeaponProjectile} per tick (Yes, this may cause
 * performance issues).
 *
 * <p>This event is not always called, and can be disabled in the
 * <code>config.yml</code>.
 */
public class ProjectileMoveEvent extends ProjectileEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public ProjectileMoveEvent(WeaponProjectile projectile) {
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