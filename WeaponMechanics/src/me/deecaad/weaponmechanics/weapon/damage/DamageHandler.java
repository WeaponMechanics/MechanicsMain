package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponKillEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import java.io.File;
import java.util.Map;

public class DamageHandler implements IValidator {

    /**
     * @return false if damaging was cancelled
     */
    public boolean tryUse(LivingEntity victim, LivingEntity shooter, String weaponTitle, CustomProjectile projectile, DamagePoint point, boolean isBackstab) {
        Configuration config = WeaponMechanics.getConfigurations();

        boolean isFriendlyFire = config.getBool(weaponTitle + ".Damage.Enable_Friendly_Fire");
        if (!isFriendlyFire && !DamageUtils.canHarm(shooter, victim)) {
            return false;
        }

        // Base damage amounts
        double damage = config.getDouble(weaponTitle + ".Damage.Base_Damage");

        if (point != null) {
            damage += config.getDouble(weaponTitle + ".Damage." + point.getReadable() + ".Bonus_Damage");
        }

        // Damage changes based on how far the projectile travelled
        double distance = projectile.getDistanceTravelled();
        DamageDropoff dropoff = config.getObject(weaponTitle + ".Damage.Dropoff", DamageDropoff.class);
        if (dropoff != null) {
            damage += dropoff.getDamage(distance);
        }

        // Critical Hit chance
        double chance = config.getDouble(weaponTitle + ".Damage.Critical_Hit.Chance") / 100;
        boolean isCritical = NumberUtil.chance(chance);
        if (isCritical) {
            damage += config.getDouble(weaponTitle + ".Damage.Critical_Hit.Bonus_Damage");
        }

        // Backstab damage
        if (isBackstab) {
            damage += config.getDouble(weaponTitle + ".Damage.Backstab.Bonus_Damage");
        }

        int armorDamage = config.getInt(weaponTitle + ".Damage.Armor_Damage");
        int fireTicks = config.getInt(weaponTitle + ".Damage.Fire_Ticks");

        WeaponDamageEntityEvent damageEntityEvent = new WeaponDamageEntityEvent(weaponTitle, projectile.getWeaponStack(), shooter, victim,
                damage, isBackstab, isCritical, point, armorDamage, fireTicks);
        Bukkit.getPluginManager().callEvent(damageEntityEvent);

        if (damageEntityEvent.isCancelled()) return false;

        fireTicks = damageEntityEvent.getFireTicks();
        isBackstab = damageEntityEvent.isBackStab();
        point = damageEntityEvent.getPoint();
        // No need to update damage, armorDamage or finalDamage here
        // since they're used just once after this (get from event simply)

        DamageUtils.apply(shooter, victim, damageEntityEvent.getFinalDamage());
        DamageUtils.damageArmor(victim, damageEntityEvent.getArmorDamage(), point);

        // Fire ticks
        if (fireTicks > 0) {
            victim.setFireTicks(fireTicks);
        }

        if (victim.isDead()) {
            Bukkit.getPluginManager().callEvent(new WeaponKillEntityEvent(weaponTitle, projectile.getWeaponStack(), shooter, victim, damageEntityEvent));
        }
        return true;
    }

    public void tryUseExplosion(LivingEntity shooter, String weaponTitle, Map<LivingEntity, Double> exposures) {
        Configuration config = WeaponMechanics.getConfigurations();

        boolean isFriendlyFire = config.getBool(weaponTitle + ".Damage.Enable_Friendly_Fire");
        boolean isOwnerImmune = config.getBool(weaponTitle + ".Damage.Explosion_Damage.Enable_Owner_Immunity");
        double baseDamage = config.getDouble(weaponTitle + ".Damage.Explosion_Damage.Damage");

        for (Map.Entry<LivingEntity, Double> entry : exposures.entrySet()) {
            LivingEntity victim = entry.getKey();
            double exposure = entry.getValue();

            if (!isFriendlyFire && !DamageUtils.canHarm(shooter, victim)) {
                continue;
            } else if (isOwnerImmune && victim.equals(shooter)) {
                continue;
            }

            double damage = DamageUtils.calculateFinalDamage(shooter, victim, baseDamage * exposure, null, false);

            DamageUtils.apply(shooter, victim, damage);
            DamageUtils.damageArmor(victim, config.getInt(weaponTitle + ".Damage.Explosion_Damage.Armor_Damage"));

            // Fire ticks
            int fireTicks = config.getInt(weaponTitle + ".Damage.Explosion_Damage.Fire_Ticks");
            if (fireTicks > 0) {
                victim.setFireTicks(fireTicks);
            }
        }
    }

    @Override
    public String getKeyword() {
        return "Damage";
    }

    @Override
    public void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path) {
        // todo
    }
}
