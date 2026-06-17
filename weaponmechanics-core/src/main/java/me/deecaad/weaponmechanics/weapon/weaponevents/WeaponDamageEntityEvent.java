package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.mechanics.program.Program;
import me.deecaad.core.utils.RandomUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamageDropoff;
import me.deecaad.weaponmechanics.weapon.damage.DamageModifier;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.damage.MeleeDamageSource;
import me.deecaad.weaponmechanics.weapon.damage.ProjectileDamageSource;
import me.deecaad.weaponmechanics.weapon.damage.WeaponDamageSource;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Called whenever an entity is damaged by a weapon. For deaths, use the
 * {@link WeaponKillEntityEvent} instead. The calculations for final damage can be quite extensive,
 * so be user to change values before using {@link #getFinalDamage()}.
 */
public class WeaponDamageEntityEvent extends WeaponEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final WeaponDamageSource source;
    private final LivingEntity victim;
    private double baseDamage;
    private double finalDamage;
    private double critChance;
    private double critDamage;
    private int armorDamage;
    private int fireTicks;
    private DamageDropoff dropoff;
    private final List<DamageModifier> damageModifiers;

    private Program damageMechanics;
    private Program killMechanics;
    private Program backstabMechanics;
    private Program criticalHitMechanics;
    private Program headMechanics;
    private Program bodyMechanics;
    private Program armsMechanics;
    private Program legsMechanics;
    private Program feetMechanics;

    private boolean wasCritical;
    private boolean isCancelled;

    public WeaponDamageEntityEvent(WeaponDamageSource source, EquipmentSlot hand, LivingEntity victim,
        double baseDamage, double critChance, int armorDamage, int fireTicks,
        DamageModifier damageModifier, Program damageMechanics,
        Program killMechanics, Program backstabMechanics, Program criticalHitMechanics,
        Program headMechanics, Program bodyMechanics, Program armsMechanics,
        Program legsMechanics, Program feetMechanics) {

        super(source.getWeaponTitle(), source.getWeaponStack(), source.getShooter(), hand);

        this.source = source;
        this.victim = victim;
        this.baseDamage = baseDamage;
        this.finalDamage = Double.NaN;
        this.critChance = critChance;
        this.critDamage = WeaponMechanics.getInstance().getWeaponConfigurations().getDouble(weaponTitle + ".Damage.Critical_Hit.Bonus_Damage");
        this.armorDamage = armorDamage;
        this.fireTicks = fireTicks;
        this.dropoff = WeaponMechanics.getInstance().getWeaponConfigurations().getObject(weaponTitle + ".Damage.Dropoff", DamageDropoff.class);

        this.damageMechanics = damageMechanics;
        this.killMechanics = killMechanics;
        this.backstabMechanics = backstabMechanics;
        this.criticalHitMechanics = criticalHitMechanics;
        this.headMechanics = headMechanics;
        this.bodyMechanics = bodyMechanics;
        this.armsMechanics = armsMechanics;
        this.legsMechanics = legsMechanics;
        this.feetMechanics = feetMechanics;

        this.damageModifiers = new LinkedList<>();
        this.damageModifiers.add(damageModifier);
    }

    public @NotNull WeaponDamageSource getSource() {
        return source;
    }

    /**
     * Who is being damaged by the weapon.
     *
     * @return The non-null entity being damaged.
     */
    public @NotNull LivingEntity getVictim() {
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
     * Sets the base damage amount (before calculations). Resets the result of
     * {@link #getFinalDamage()}.
     *
     * @param baseDamage The base damage.
     */
    public void setBaseDamage(double baseDamage) {
        this.finalDamage = Double.NaN;
        this.wasCritical = false;
        this.baseDamage = baseDamage;
    }

    /**
     * Returns the damage AFTER all the calculations.
     *
     * @return The final damage.
     */
    public double getFinalDamage() {
        if (Double.isNaN(finalDamage)) {
            Configuration config = WeaponMechanics.getInstance().getWeaponConfigurations();

            // Calculate the final damage and save its value
            // The cached value is reset if any mutators are used.

            double damage = this.baseDamage;

            if (dropoff != null && source instanceof ProjectileDamageSource projectileSource)
                damage += dropoff.getDamage(projectileSource.getProjectile().getDistanceTravelled());
            if (source.getDamagePoint() != null)
                damage += config.getDouble(weaponTitle + ".Damage." + source.getDamagePoint().getReadable() + ".Bonus_Damage");
            if (RandomUtil.chance(critChance)) {
                damage += critDamage;
                wasCritical = true;
            }
            if ((source instanceof MeleeDamageSource meleeSource) && meleeSource.isBackStab())
                damage += config.getDouble(weaponTitle + ".Damage.Backstab.Bonus_Damage");

            EntityWrapper victimWrapper = WeaponMechanics.getInstance().getEntityWrapper(victim);

            double rate = 1.0;
            boolean isBackStab = source instanceof MeleeDamageSource meleeSource && meleeSource.isBackStab();
            for (DamageModifier modifier : damageModifiers) {
                rate += modifier.getRate(victimWrapper, getPoint(), isBackStab) - 1;
            }

            // Clamping to the base damage
            rate = damageModifiers.getFirst().clamp(rate);

            return finalDamage = damage * rate;
        }

        return finalDamage;
    }

    /**
     * Overrides the final damage and skips calculations. Probably don't want to use this method, as it
     * will skip headshots, backstabs, armor, etc.
     *
     * @param finalDamage The final damage amount.
     */
    public void setFinalDamage(double finalDamage) {
        this.finalDamage = finalDamage;
    }

    /**
     * Returns the chance of a critical hit.
     *
     * @return The chance of a critical hit.
     */
    public double getCritChance() {
        return critChance;
    }

    /**
     * Sets the chance of a critical hit. Resets the result of {@link #getFinalDamage()}.
     *
     * @param critChance The chance of a critical hit.
     */
    public double setCritChance(double critChance) {
        this.finalDamage = Double.NaN;
        this.wasCritical = false;
        return this.critChance = critChance;
    }

    /**
     * Returns true if the damage from the last calculation was a critical hit.
     *
     * @return true if this was a critical hit.
     */
    public boolean wasCritical() {
        return wasCritical;
    }

    /**
     * Returns the bonus damage from a critical hit.
     *
     * @return The bonus damage from a critical hit.
     */
    public double getCritDamage() {
        return critDamage;
    }

    /**
     * Sets the bonus damage from a critical hit. Resets the result of {@link #getFinalDamage()}.
     *
     * @param critDamage The bonus damage from a critical hit.
     */
    public void setCritDamage(double critDamage) {
        this.finalDamage = Double.NaN;
        this.wasCritical = false;
        this.critDamage = critDamage;
    }

    /**
     * Gets the body part that was hit (head/arms/chest/etc).
     *
     * @return The nullable damage point.
     */
    public @Nullable DamagePoint getPoint() {
        return source.getDamagePoint();
    }

    /**
     * Returns the amount of damage to armor. There is a chance for this number to be ignored (if the
     * armor has unbreaking).
     *
     * @return the amount of damage to armor.
     */
    public int getArmorDamage() {
        return armorDamage;
    }

    /**
     * Sets the amount of damage to armor. There is a chance for this number to be ignored (if the armor
     * has unbreaking).
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

    public @Nullable DamageDropoff getDropoff() {
        return dropoff;
    }

    public void setDropoff(@Nullable DamageDropoff dropoff) {
        this.dropoff = dropoff;
    }

    public void addDamageModifier(@Nullable DamageModifier modifier) {
        damageModifiers.add(modifier);
    }

    public @NotNull List<DamageModifier> getDamageModifiers() {
        return damageModifiers;
    }

    public @Nullable Program getDamageMechanics() {
        return damageMechanics;
    }

    public void setDamageMechanics(@Nullable Program damageMechanics) {
        this.damageMechanics = damageMechanics;
    }

    public Program getKillMechanics() {
        return killMechanics;
    }

    public void setKillMechanics(@Nullable Program killMechanics) {
        this.killMechanics = killMechanics;
    }

    public @Nullable Program getBackstabMechanics() {
        return backstabMechanics;
    }

    public void setBackstabMechanics(@Nullable Program backstabMechanics) {
        this.backstabMechanics = backstabMechanics;
    }

    public @Nullable Program getCriticalHitMechanics() {
        return criticalHitMechanics;
    }

    public void setCriticalHitMechanics(@Nullable Program criticalHitMechanics) {
        this.criticalHitMechanics = criticalHitMechanics;
    }

    public @Nullable Program getHeadMechanics() {
        return headMechanics;
    }

    public void setHeadMechanics(@Nullable Program headMechanics) {
        this.headMechanics = headMechanics;
    }

    public @Nullable Program getBodyMechanics() {
        return bodyMechanics;
    }

    public void setBodyMechanics(@Nullable Program bodyMechanics) {
        this.bodyMechanics = bodyMechanics;
    }

    public @Nullable Program getArmsMechanics() {
        return armsMechanics;
    }

    public void setArmsMechanics(@Nullable Program armsMechanics) {
        this.armsMechanics = armsMechanics;
    }

    public @Nullable Program getLegsMechanics() {
        return legsMechanics;
    }

    public void setLegsMechanics(@Nullable Program legsMechanics) {
        this.legsMechanics = legsMechanics;
    }

    public @Nullable Program getFeetMechanics() {
        return feetMechanics;
    }

    public void setFeetMechanics(@Nullable Program feetMechanics) {
        this.feetMechanics = feetMechanics;
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
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}