package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamageDropoff;
import me.deecaad.weaponmechanics.weapon.damage.DamageModifier;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Called whenever an entity is damaged by a weapon. For deaths, use the
 * {@link WeaponKillEntityEvent} instead. The calculations for final damage
 * can be quite extensive, so be user to change values before using
 * {@link #getFinalDamage()}.
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
    private boolean isExplosion;
    private double distanceTravelled;
    private List<DamageModifier> damageModifiers;

    private boolean isCancelled;

    public WeaponDamageEntityEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser, EquipmentSlot hand,
                                   LivingEntity victim, double baseDamage, boolean isBackstab, boolean isCritical,
                                   DamagePoint point, int armorDamage, int fireTicks, boolean isExplosion,
                                   double distanceTravelled, DamageModifier damageModifier) {

        super(weaponTitle, weaponItem, weaponUser, hand);

        this.victim = victim;
        this.baseDamage = baseDamage;
        this.finalDamage = Double.NaN;
        this.isBackstab = isBackstab;
        this.isCritical = isCritical;
        this.point = point;
        this.armorDamage = armorDamage;
        this.fireTicks = fireTicks;
        this.isExplosion = isExplosion;
        this.distanceTravelled = distanceTravelled;

        this.damageModifiers = new LinkedList<>();
        this.damageModifiers.add(damageModifier);
    }

    /**
     * Who is being damaged by the weapon.
     *
     * @return The non-null entity being damaged.
     */
    public LivingEntity getVictim() {
        return victim;
    }

    /**
     * The weapon's base damage amount (before calculations).
     *
     * @return The base damage.
     */
    public double getBaseDamage() {
        return baseDamage;
    }

    /**
     * Sets the base damage amount (before calculations). Resets the result
     * of {@link #getFinalDamage()}.
     *
     * @param baseDamage The base damage.
     */
    public void setBaseDamage(double baseDamage) {
        this.finalDamage = Double.NaN;
        this.baseDamage = baseDamage;
    }

    /**
     * Returns the damage AFTER all the calculations.
     *
     * @return The final damage.
     */
    public double getFinalDamage() {
        if (Double.isNaN(finalDamage)) {
            Configuration config = WeaponMechanics.getConfigurations();

            // Calculate the final damage and save its value
            // Final damage value is reset if set point, damage
            // critical or backstab methods are used

            double damage = this.baseDamage;

            DamageDropoff dropoff = config.getObject(weaponTitle + ".Damage.Dropoff", DamageDropoff.class);
            if (dropoff != null && !isExplosion)
                damage += dropoff.getDamage(distanceTravelled);
            if (point != null)
                damage += config.getDouble(weaponTitle + ".Damage." + point.getReadable() + ".Bonus_Damage");
            if (isCritical)
                damage += config.getDouble(weaponTitle + ".Damage.Critical_Hit.Bonus_Damage");
            if (isBackstab)
                damage += config.getDouble(weaponTitle + ".Damage.Backstab.Bonus_Damage");

            double rate = 1.0;
            for (DamageModifier modifier : damageModifiers) {
                rate += modifier.getRate(getShooterWrapper(false), getPoint(), isBackstab()) - 1;
            }

            // Clamping to the base damage
            rate = damageModifiers.get(0).clamp(rate);

            return finalDamage = damage * rate;
        }

        return finalDamage;
    }

    /**
     * Overrides the final damage and skips calculations. Probably don't want
     * to use this method, as it will skip headshots, backstabs, armor, etc.
     *
     * @param finalDamage The final damage amount.
     */
    public void setFinalDamage(double finalDamage) {
        this.finalDamage = finalDamage;
    }

    /**
     * Returns true if the damage came from behind the victim.
     *
     * @return true is this is a backstab.
     */
    public boolean isBackstab() {
        return isBackstab;
    }

    /**
     * Sets whether this was a backstab. Resets the result of
     * {@link #getFinalDamage()}.
     *
     * @param backstab true if this is a backstab.
     */
    public void setBackstab(boolean backstab) {
        this.finalDamage = Double.NaN;
        this.isBackstab = backstab;
    }

    /**
     * Returns true if the damage is critical (usually determined by chance).
     *
     * @return true if this is critical hit.
     */
    public boolean isCritical() {
        return isCritical;
    }

    /**
     * Sets whether this is a critical hit. Resets the result of
     * {@link #getFinalDamage()}.
     *
     * @param isCritical true if this is a critical hit.
     */
    public void setCritical(boolean isCritical) {
        this.finalDamage = Double.NaN;
        this.isCritical = isCritical;
    }

    /**
     * Gets the body part that was hit (head/arms/chest/etc).
     *
     * @return The nullable damage point.
     */
    public DamagePoint getPoint() {
        return point;
    }

    /**
     * Sets the body part that was hit (head/arms/chest/etc). Resets the result
     * of {@link #getFinalDamage()}.
     *
     * @param point The nullable damage point.
     */
    public void setPoint(DamagePoint point) {
        this.finalDamage = Double.NaN;
        this.point = point;
    }

    /**
     * Returns the amount of damage to armor. There is a chance for this number
     * to be ignored (if the armor has unbreaking).
     *
     * @return the amount of damage to armor.
     */
    public int getArmorDamage() {
        return armorDamage;
    }

    /**
     * Sets the amount of damage to armor. There is a chance for this number to
     * be ignored (if the armor has unbreaking).
     *
     * @param armorDamage Sets the amount of damage to the armor.
     */
    public void setArmorDamage(int armorDamage) {
        this.armorDamage = armorDamage;
    }

    /**
     * How many ticks should the victim be lit on fire for.
     *
     * @return The fire ticks.
     */
    public int getFireTicks() {
        return fireTicks;
    }

    /**
     * Sets the number of ticks the victim should be lit on fire for.
     *
     * @param fireTicks The fire ticks.
     * @see LivingEntity#setFireTicks(int)
     */
    public void setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
    }

    public boolean isExplosion() {
        return isExplosion;
    }

    public void setExplosion(boolean explosion) {
        isExplosion = explosion;
    }

    public double getDistanceTravelled() {
        return distanceTravelled;
    }

    public void addDamageModifier(DamageModifier modifier) {
        damageModifiers.add(modifier);
    }

    public List<DamageModifier> getDamageModifiers() {
        return damageModifiers;
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