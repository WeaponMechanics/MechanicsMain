package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

public class DamageHandler {

    private static final String[] DAMAGE_POINTS = new String[]{"Head", "Body", "Arms", "Legs", "Feet", "Backstab", "Critical_Hit"};

    public boolean tryUse(LivingEntity victim, LivingEntity shooter, String weaponTitle, CustomProjectile projectile, DamagePoint point, boolean isBackstab) {
        Configuration config = WeaponMechanics.getConfigurations();

        boolean isFriendlyFire = config.getBool(weaponTitle + ".Damage.Enable_Friendly_Fire");
        if (!DamageUtils.canHarm(shooter, victim, isFriendlyFire)) {
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
        double chance = config.getDouble(weaponTitle + ".Damage.Critical_Hit.Chance") / 100.0;
        if (NumberUtils.chance(chance)) {
            damage += config.getDouble(weaponTitle + ".Damage.Critical_Hit.Bonus_Damage");
        }

        // Backstab damage
        if (isBackstab) {
            damage += config.getDouble(weaponTitle + ".Damage.Backstab.Bonus_Damage");
        }

        double finalDamage = DamageUtils.calculateFinalDamage(shooter, victim, damage, point, isBackstab);
        DamageUtils.apply(shooter, victim, finalDamage);
        DamageUtils.damageArmor(victim, config.getInt(weaponTitle + ".Damage.Armor_Damage"), point);

        if (victim.isDead()) {
            // todo WeaponKillEvent
        }

        // Fire ticks
        int fireTicks = config.getInt(weaponTitle + ".Damage.Fire_Ticks");
        if (fireTicks > 0) {
            victim.setFireTicks(fireTicks);
        }

        // Using string builder for minor performance optimization
        // Since this method may be called >20 times per second,
        // every bit of optimization is important
        final StringBuilder builder = new StringBuilder(100);

        for (String str : DAMAGE_POINTS) {

            builder.append(weaponTitle).append(".Damage.").append(str).append('.');
            final int length = builder.length();

            builder.append("Global_Mechanics_Shooter");

            builder.setLength(length);
            builder.append("Global_Mechanics_Victim");

            builder.setLength(0);
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

            if (!DamageUtils.canHarm(shooter, victim, isFriendlyFire)) {
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
}
