package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * This class outlines the event of a weapon launching a projectile.
 */
public class WeaponShootEvent extends WeaponEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private WeaponProjectile projectile;

    public WeaponShootEvent(WeaponProjectile projectile) {
        super(projectile.getWeaponTitle(), projectile.getWeaponStack(), projectile.getShooter(), projectile.getHand());

        this.projectile = projectile;
    }

    public WeaponProjectile getProjectile() {
        return projectile;
    }

    public void setProjectile(@NotNull WeaponProjectile projectile) {
        if (projectile == null)
            throw new IllegalArgumentException("projectile cannot be null");

        this.projectile = projectile;
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
