package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.Projectile;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.Recoil;
import me.deecaad.weaponmechanics.weapon.shoot.spread.Spread;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private Spread spread;
    private double baseSpread;
    private Recoil recoil;
    private double recoilYaw;
    private double recoilPitch;

    private boolean isCancelled;

    public PrepareWeaponShootEvent(
            @NotNull String weaponTitle,
            @NotNull ItemStack weaponStack,
            @NotNull LivingEntity shooter,
            @NotNull EquipmentSlot hand,
            @Nullable Mechanics shootMechanics,
            boolean resetFallDistance,
            @NotNull Projectile projectile,
            double projectileSpeed,
            int projectileAmount,
            @Nullable Spread spread,
            @Nullable Recoil recoil
    ) {
        super(weaponTitle, weaponStack, shooter, hand);
        this.shootMechanics = shootMechanics;
        this.resetFallDistance = resetFallDistance;
        this.projectile = projectile;
        this.projectileSpeed = projectileSpeed;
        this.projectileAmount = projectileAmount;
        this.spread = spread;
        this.baseSpread = spread == null ? 0 : spread.getBaseSpread();
        this.recoil = recoil;
        this.recoilYaw = 0;
        this.recoilPitch = 0;
    }

    public Mechanics getShootMechanics() {
        return shootMechanics;
    }

    public void setShootMechanics(Mechanics shootMechanics) {
        if (this.shootMechanics != null)
            this.shootMechanics.clearDirty(); // clear any modifications
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

    public Spread getSpread() {
        return spread;
    }

    public void setSpread(Spread spread) {
        this.spread = spread;
    }

    public double getBaseSpread() {
        return baseSpread;
    }

    public void setBaseSpread(double baseSpread) {
        this.baseSpread = baseSpread;
    }

    public Recoil getRecoil() {
        return recoil;
    }

    public void setRecoil(Recoil recoil) {
        this.recoil = recoil;
    }

    public double getRecoilYaw() {
        return recoilYaw;
    }

    public void setRecoilYaw(double recoilYaw) {
        this.recoilYaw = recoilYaw;
    }

    public double getRecoilPitch() {
        return recoilPitch;
    }

    public void setRecoilPitch(double recoilPitch) {
        this.recoilPitch = recoilPitch;
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
