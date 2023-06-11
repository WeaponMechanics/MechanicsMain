package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.Projectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called after {@link WeaponPreShootEvent} but right before
 * {@link WeaponShootEvent}. This can be used to modify mechanics, projectile
 * amount, etc.
 */
public class PrepareWeaponShootEvent extends WeaponEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private Mechanics shootMechanics;
    private boolean resetFallDistance;
    private Projectile projectile;
    private double projectileSpeed;
    private int projectileAmount;

    private boolean isCancelled;

    public PrepareWeaponShootEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter, EquipmentSlot hand, Mechanics shootMechanics, boolean resetFallDistance, Projectile projectile, double projectileSpeed, int projectileAmount) {
        super(weaponTitle, weaponStack, shooter, hand);
        this.shootMechanics = shootMechanics;
        this.resetFallDistance = resetFallDistance;
        this.projectile = projectile;
        this.projectileSpeed = projectileSpeed;
        this.projectileAmount = projectileAmount;
    }

    public Mechanics getShootMechanics() {
        return shootMechanics;
    }

    public void setShootMechanics(Mechanics shootMechanics) {
        this.shootMechanics = shootMechanics;
    }

    public boolean isResetFallDistance() {
        return resetFallDistance;
    }

    public void setResetFallDistance(boolean resetFallDistance) {
        this.resetFallDistance = resetFallDistance;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public void setProjectile(Projectile projectile) {
        this.projectile = projectile;
    }

    public double getProjectileSpeed() {
        return projectileSpeed;
    }

    public void setProjectileSpeed(double projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
    }

    public int getProjectileAmount() {
        return projectileAmount;
    }

    public void setProjectileAmount(int projectileAmount) {
        this.projectileAmount = projectileAmount;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
