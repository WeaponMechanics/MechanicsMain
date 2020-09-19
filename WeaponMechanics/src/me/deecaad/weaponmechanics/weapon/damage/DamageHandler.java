package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.general.AddPotionEffect;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import java.io.File;
import java.util.Map;

public class DamageHandler implements IValidator {

    private static final String[] DAMAGE_POINTS = new String[]{"Head", "Body", "Arms", "Legs", "Feet", "Backstab", "Critical_Hit"};

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
        if (NumberUtils.chance(chance)) {
            damage += config.getDouble(weaponTitle + ".Damage.Critical_Hit.Bonus_Damage");
        }

        // Backstab damage
        if (isBackstab) {
            damage += config.getDouble(weaponTitle + ".Damage.Backstab.Bonus_Damage");
        }

        DamageUtils.apply(shooter, victim, damage, point, isBackstab);
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

            if (!isFriendlyFire && !DamageUtils.canHarm(shooter, victim)) {
                continue;
            } else if (isOwnerImmune && victim.equals(shooter)) {
                continue;
            }

            DamageUtils.apply(shooter, victim, baseDamage * exposure, null, false);
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
    public void validate(Configuration config, File file, ConfigurationSection configurationSection, String path) {
        final String[] effectPaths = new String[]{"Global_Mechanics_Shooter", "Global_Mechanics_Victim", "Mechanics_Shooter", "Mechanics_Victim"};
        final String[] potionPaths = new String[]{"Potions_Shooter", "Potions_Victim"};
        AddPotionEffect potionSerializer = new AddPotionEffect();

        // Using string builder and instantiating outside the scope of the loop
        // for minor performance boost
        StringBuilder builder = new StringBuilder();
        for (String str : DAMAGE_POINTS) {
            for (String effect : effectPaths) {
                builder.append(path).append('.').append(str).append('.').append(effect);
                String toString = builder.toString();

                // Only if there are defined effects, should we serialize them
                if (config.containsKey(toString)) {

                }

                // Fastest method to clear StringBuilder
                builder.setLength(0);
            }
            for (String potion : potionPaths) {
                builder.append(path).append('.').append(str).append('.').append(potion);
                String toString = builder.toString();

                // Only if there are defined potions, should we serialize them
                if (config.containsKey(toString)) {
                    potionSerializer.serialize(file, configurationSection, toString);
                }

                builder.setLength(0);
            }
        }
    }
}
