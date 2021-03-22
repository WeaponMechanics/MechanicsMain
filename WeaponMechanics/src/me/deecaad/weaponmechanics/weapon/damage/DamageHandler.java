package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponKillEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class DamageHandler implements IValidator {

    /**
     * @return false if damaging was cancelled
     */
    public boolean tryUse(LivingEntity victim, @Nonnull ICustomProjectile projectile, double damage, DamagePoint point, boolean isBackstab) {
        Configuration config = getConfigurations();

        LivingEntity shooter = projectile.getShooter();
        String weaponTitle = projectile.getWeaponTitle();

        boolean isFriendlyFire = config.getBool(weaponTitle + ".Damage.Enable_Friendly_Fire");
        if (!isFriendlyFire && !DamageUtils.canHarm(shooter, victim)) {
            return false;
        }

        boolean isOwnerImmune = config.getBool(weaponTitle + ".Damage.Enable_Owner_Immunity");
        if (isOwnerImmune && victim.equals(shooter)) {
            return false;
        }

        // Critical Hit chance
        double chance = config.getDouble(weaponTitle + ".Damage.Critical_Hit.Chance", -1);
        boolean isCritical = chance != -1 && NumberUtil.chance((chance / 100));

        int armorDamage = config.getInt(weaponTitle + ".Damage.Armor_Damage");
        int fireTicks = config.getInt(weaponTitle + ".Damage.Fire_Ticks");

        WeaponDamageEntityEvent damageEntityEvent = new WeaponDamageEntityEvent(weaponTitle, projectile.getWeaponStack(), shooter, victim,
                damage, isBackstab, isCritical, point, armorDamage, fireTicks, projectile.getDistanceTravelled());
        Bukkit.getPluginManager().callEvent(damageEntityEvent);

        if (damageEntityEvent.isCancelled()) return false;

        fireTicks = damageEntityEvent.getFireTicks();
        isBackstab = damageEntityEvent.isBackstab();
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

    public void tryUseExplosion(ICustomProjectile projectile, Map<LivingEntity, Double> exposures) {
        Configuration config = getConfigurations();

        String weaponTitle = projectile.getWeaponTitle();
        double damage = config.getDouble(weaponTitle + ".Damage.Base_Damage");

        for (Map.Entry<LivingEntity, Double> entry : exposures.entrySet()) {
            // Key = victim
            // Value = exposure
            tryUse(entry.getKey(), projectile, damage * entry.getValue(), null, false);
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
