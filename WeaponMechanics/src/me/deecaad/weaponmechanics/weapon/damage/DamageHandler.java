package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.effects.EffectList;
import me.deecaad.core.effects.serializers.EffectListSerializer;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.general.AddPotionEffect;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.io.File;

public class DamageHandler implements IValidator {

    private static final String[] DAMAGE_POINTS = new String[]{"Head", "Body", "Arms", "Legs", "Feet", "Backstab", "Critical_Hit"};

    public boolean tryUse(LivingEntity victim, LivingEntity shooter, String weaponTitle, CustomProjectile projectile, DamagePoint point, boolean isBackstab) {
        Configuration config = WeaponMechanics.getConfigurations();

        boolean isFriendlyFire = config.getBool(weaponTitle + ".Damage.Enable_Friendly_Fire");
        if (!isFriendlyFire && !DamageUtils.canHarm(shooter, victim)) {
            return false;
        }

        // Base damage amounts
        double damage = config.getDouble(weaponTitle + ".Damage.Base_Damage")
                + config.getDouble(weaponTitle + ".Damage." + point.getReadable() + ".Bonus_Damage");

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

        // Fire ticks
        int fireTicks = config.getInt(weaponTitle + ".Damage.Fire_Ticks");
        if (fireTicks > 0) {
            victim.setFireTicks(fireTicks);
        }

        // Setup variables for effects and potions
        Plugin plugin = WeaponMechanics.getPlugin();
        Location victimLoc = victim.getEyeLocation();
        Location shooterLoc = shooter.getEyeLocation();
        Vector victimDir = victimLoc.getDirection();
        Vector shooterDir = shooterLoc.getDirection();

        // Using string builder for minor performance optimization
        // Since this method may be called >20 times per second,
        // every bit of optimization is important
        final StringBuilder builder = new StringBuilder(100);

        for (String str : DAMAGE_POINTS) {

            builder.append(weaponTitle).append(".Damage.").append(str).append('.');
            final int length = builder.length();

            builder.append("Global_Effects_Shooter");
            EffectList globalEffectsShooter = config.getObject(builder.toString(), EffectList.class);
            if (globalEffectsShooter != null) {
                globalEffectsShooter.getEffects().forEach(effect -> effect.spawn(plugin, shooterLoc, shooterDir));
            }

            builder.setLength(length);
            builder.append("Global_Effects_Victim");
            EffectList globalEffectsVictim = config.getObject(builder.toString(), EffectList.class);
            if (globalEffectsVictim != null) {
                globalEffectsVictim.getEffects().forEach(effect -> effect.spawn(plugin, victimLoc, victimDir));
            }

            if (shooter.getType() == EntityType.PLAYER) {
                builder.setLength(length);
                builder.append("Effects_Shooter");
                EffectList effectsShooter = config.getObject(builder.toString(), EffectList.class);
                if (effectsShooter != null) {
                    effectsShooter.getEffects().forEach((effect -> effect.spawnFor(plugin, (Player) shooter, shooterLoc, shooterDir)));
                }
            }

            if (victim.getType() == EntityType.PLAYER) {
                builder.setLength(length);
                builder.append("Effects_Victim");
                EffectList effectsVictim = config.getObject(builder.toString(), EffectList.class);
                if (effectsVictim != null) {
                    effectsVictim.getEffects().forEach(effect -> effect.spawnFor(plugin, (Player) victim, victimLoc, victimDir));
                }
            }

            builder.setLength(length);
            builder.append("Potions_Shooter");
            AddPotionEffect potionsShooter = config.getObject(builder.toString(), AddPotionEffect.class);
            if (potionsShooter != null) {
                potionsShooter.add(shooter);
            }

            builder.setLength(length);
            builder.append("Potions_Victim");
            AddPotionEffect potionsVictim = config.getObject(builder.toString(), AddPotionEffect.class);
            if (potionsVictim != null) {
                potionsVictim.add(victim);
            }

            builder.setLength(0);
        }
        return true;
    }

    public boolean tryUseExplosion() {
        return false;
    }

    @Override
    public String getKeyword() {
        return "Damage";
    }

    @Override
    public void validate(Configuration config, File file, ConfigurationSection configurationSection, String path) {
        final String[] effectPaths = new String[]{"Global_Effects_Shooter", "Global_Effects_Victim", "Effects_Shooter", "Effects_Victim"};
        final String[] potionPaths = new String[]{"Potions_Shooter", "Potions_Victim"};
        EffectListSerializer effectSerializer = new EffectListSerializer();
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
                    effectSerializer.serialize(file, configurationSection, toString);
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
