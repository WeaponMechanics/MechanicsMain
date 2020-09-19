package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class WeaponDamageEntityEvent extends WeaponEvent implements Cancellable {

    private final LivingEntity victim;
    private double baseDamage;
    private double finalDamage;
    private boolean isBackStab;
    private DamagePoint point;
    private int armorDamage;
    private int fireTicks;
    private boolean isCancelled;

    public WeaponDamageEntityEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser,
                                   LivingEntity victim, double baseDamage, boolean isBackStab,
                                   DamagePoint point, int armorDamage, int fireTicks) {

        super(weaponTitle, weaponItem, weaponUser);

        this.victim = victim;
        this.baseDamage = baseDamage;
        this.finalDamage = Integer.MIN_VALUE;
        this.isBackStab = isBackStab;
        this.point = point;
        this.armorDamage = armorDamage;
        this.fireTicks = fireTicks;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(double baseDamage) {
        this.baseDamage = baseDamage;
    }

    public double getFinalDamage() {
        if (finalDamage == Integer.MIN_VALUE) {
            // Calculate final damage
        }

        return finalDamage;
    }

    public void setFinalDamage(double finalDamage) {
        this.finalDamage = finalDamage;
    }

    public boolean isBackStab() {
        return isBackStab;
    }

    public void setBackStab(boolean backStab) {
        isBackStab = backStab;
    }

    public DamagePoint getPoint() {
        return point;
    }

    public void setPoint(DamagePoint point) {
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
}
