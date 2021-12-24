package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.primitive.DoubleEntry;
import me.deecaad.core.utils.primitive.DoubleMap;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.mechanics.defaultmechanics.CommonDataTags;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponKillEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class DamageHandler {

    private WeaponHandler weaponHandler;

    public DamageHandler() {}

    public DamageHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    /**
     * @return false if damaging was cancelled
     */
    public boolean tryUse(LivingEntity victim, @Nonnull WeaponProjectile projectile, double damage, DamagePoint point, boolean isBackstab) {
        Configuration config = getConfigurations();

        LivingEntity shooter = projectile.getShooter();
        String weaponTitle = projectile.getWeaponTitle();

        boolean isFriendlyFire = config.getBool(weaponTitle + ".Damage.Enable_Friendly_Fire");
        if (!isFriendlyFire && !DamageUtil.canHarm(shooter, victim)) {
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
        point = damageEntityEvent.getPoint();

        DamageUtil.apply(shooter, victim, damageEntityEvent.getFinalDamage());
        DamageUtil.damageArmor(victim, damageEntityEvent.getArmorDamage(), point);

        // Fire ticks
        if (fireTicks > 0) {
            victim.setFireTicks(fireTicks);
        }

        CastData shooterCast = new CastData(WeaponMechanics.getEntityWrapper(shooter), projectile.getWeaponTitle(), projectile.getWeaponStack());
        shooterCast.setData(CommonDataTags.TARGET_LOCATION.name(), victim.getLocation());

        CastData victimCast = new CastData(WeaponMechanics.getEntityWrapper(victim), projectile.getWeaponTitle(), projectile.getWeaponStack());
        victimCast.setData(CommonDataTags.TARGET_LOCATION.name(), shooter.getLocation());

        // On all damage
        useMechanics(config, shooterCast, victimCast, weaponTitle + ".Damage");

        // On point
        if (point != null) useMechanics(config, shooterCast, victimCast, weaponTitle + ".Damage." + point.getReadable());

        // On backstab
        if (damageEntityEvent.isBackstab()) {
            useMechanics(config, shooterCast, victimCast, weaponTitle + ".Damage.Backstab");
        }

        // On critical
        if (damageEntityEvent.isCritical()) {
            useMechanics(config, shooterCast, victimCast, weaponTitle + ".Damage.Critical_Hit");
        }

        if (victim.isDead() || victim.getHealth() <= 0.0) {
            Bukkit.getPluginManager().callEvent(new WeaponKillEntityEvent(weaponTitle, projectile.getWeaponStack(), shooter, victim, damageEntityEvent));

            // On kill
            useMechanics(config, shooterCast, victimCast, weaponTitle + ".Damage.Kill");
        }

        return true;
    }

    private void useMechanics(Configuration config, CastData shooter, CastData victim, String path) {
        Mechanics mechanics = config.getObject(path + ".Shooter_Mechanics", Mechanics.class);
        if (mechanics != null) {
            mechanics.use(shooter);
        }
        mechanics = config.getObject(path + ".Victim_Mechanics", Mechanics.class);
        if (mechanics != null) {
            mechanics.use(victim);
        }
    }

    public void tryUseExplosion(WeaponProjectile projectile, Location origin, DoubleMap<LivingEntity> exposures) {
        Configuration config = getConfigurations();

        String weaponTitle = projectile.getWeaponTitle();
        double damage = config.getDouble(weaponTitle + ".Damage.Base_Explosion_Damage");
        if (damage == 0) {
            // If explosion damage isn't used, use Base_Damage
            damage = config.getDouble(weaponTitle + ".Damage.Base_Damage");
        }

        for (DoubleEntry<LivingEntity> entry : exposures.entrySet()) {
            // Value = exposure

            LivingEntity victim = entry.getKey();
            Location victimLocation = victim.getLocation();
            Vector explosionToVictimDirection = victimLocation.toVector().subtract(origin.toVector());
            boolean backstab = victimLocation.getDirection().dot(explosionToVictimDirection) > 0.0;

            tryUse(victim, projectile, damage * entry.getValue(), null, backstab);
        }
    }
}
