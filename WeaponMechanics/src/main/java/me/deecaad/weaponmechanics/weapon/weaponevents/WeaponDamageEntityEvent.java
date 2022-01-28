package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamageDropoff;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.damage.DamageUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This class outlines the event of a weapon damaging a {@link LivingEntity}.
 */
public class WeaponDamageEntityEvent extends WeaponEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity victim;
    private double baseDamage;
    private double finalDamage;
    private boolean isBackstab;
    private boolean isCritical;
    private DamagePoint point;
    private int armorDamage;
    private int fireTicks;
    private double distanceTravelled;
    private boolean isCancelled;

    public WeaponDamageEntityEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser,
                                   LivingEntity victim, double baseDamage, boolean isBackstab, boolean isCritical,
                                   DamagePoint point, int armorDamage, int fireTicks, double distanceTravelled) {

        super(weaponTitle, weaponItem, weaponUser);

        this.victim = victim;
        this.baseDamage = baseDamage;
        this.finalDamage = Double.NaN;
        this.isBackstab = isBackstab;
        this.isCritical = isCritical;
        this.point = point;
        this.armorDamage = armorDamage;
        this.fireTicks = fireTicks;
        this.distanceTravelled = distanceTravelled;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(double baseDamage) {
        this.finalDamage = Double.NaN;
        this.baseDamage = baseDamage;
    }

    public double getFinalDamage() {
        if (Double.isNaN(finalDamage)) {
            Configuration config = WeaponMechanics.getConfigurations();

            // Calculate the final damage and save its value
            // Final damage value is reset if set point, damage
            // critical or backstab methods are used

            double damage = this.baseDamage;

            if (point != null) {
                damage += config.getDouble(weaponTitle + ".Damage." + point.getReadable() + ".Bonus_Damage");
            }

            // Damage changes based on how far the projectile travelled
            DamageDropoff dropoff = config.getObject(weaponTitle + ".Damage.Dropoff", DamageDropoff.class);
            if (dropoff != null) {
                damage += dropoff.getDamage(distanceTravelled);
            }

            // Critical Hit chance
            if (isCritical) {
                damage += config.getDouble(weaponTitle + ".Damage.Critical_Hit.Bonus_Damage");
            }

            // Backstab damage
            if (isBackstab) {
                damage += config.getDouble(weaponTitle + ".Damage.Backstab.Bonus_Damage");
            }

            if (damage < 0) {
                damage = 0;
            }

            return finalDamage = DamageUtil.calculateFinalDamage(getShooter(), victim, damage, point, isBackstab);
        }

        return finalDamage;
    }

    public void setFinalDamage(double finalDamage) {
        this.finalDamage = finalDamage;
    }

    public boolean isBackstab() {
        return isBackstab;
    }

    public void setBackstab(boolean backstab) {
        this.finalDamage = Double.NaN;
        this.isBackstab = backstab;
    }

    public boolean isCritical() {
        return isCritical;
    }

    public void setCritical(boolean isCritical) {
        this.finalDamage = Double.NaN;
        this.isCritical = isCritical;
    }

    public DamagePoint getPoint() {
        return point;
    }

    public void setPoint(DamagePoint point) {
        this.finalDamage = Double.NaN;
        this.point = point;
    }

    public int getArmorDamage() {
        return armorDamage;
    }

    public void setArmorDamage(int armorDamage) {
        this.armorDamage = armorDamage;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public void setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
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