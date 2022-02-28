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
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponKillEntityEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

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
    public boolean tryUse(LivingEntity victim, WeaponProjectile projectile, double damage, DamagePoint point, boolean isBackstab) {
        return tryUse(victim, damage, point, isBackstab, projectile.getShooter(), projectile.getWeaponTitle(),
                projectile.getWeaponStack(), projectile.getDistanceTravelled());
    }

    /**
     * @return false if damaging was cancelled
     */
    public boolean tryUse(LivingEntity victim, double damage, DamagePoint point, boolean isBackstab,
                          LivingEntity shooter, String weaponTitle, ItemStack weaponStack, double distanceTravelled) {
        Configuration config = getConfigurations();

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

        WeaponDamageEntityEvent damageEntityEvent = new WeaponDamageEntityEvent(weaponTitle, weaponStack, shooter, victim,
                damage, isBackstab, isCritical, point, armorDamage, fireTicks, distanceTravelled);
        Bukkit.getPluginManager().callEvent(damageEntityEvent);

        if (damageEntityEvent.isCancelled()) return false;

        fireTicks = damageEntityEvent.getFireTicks();
        point = damageEntityEvent.getPoint();

        if (DamageUtil.apply(shooter, victim, damageEntityEvent.getFinalDamage())) {
            WeaponMechanics.debug.debug("Damage was cancelled");

            // Damage was cancelled
            return false;
        }

        DamageUtil.damageArmor(victim, damageEntityEvent.getArmorDamage(), point);

        // Fire ticks
        if (fireTicks > 0) {
            victim.setFireTicks(fireTicks);
        }

        EntityWrapper shooterWrapper = WeaponMechanics.getEntityWrapper(shooter, true);
        CastData shooterCast;
        if (shooterWrapper != null) {
            shooterCast = new CastData(shooterWrapper, weaponTitle, weaponStack);
        } else {
            shooterCast = new CastData(shooter, weaponTitle, weaponStack);
        }
        shooterCast.setData(CommonDataTags.TARGET_LOCATION.name(), victim.getLocation());
        shooterCast.setData(CommonDataTags.SHOOTER_NAME.name(), shooter.getName());
        shooterCast.setData(CommonDataTags.VICTIM_NAME.name(), victim.getName());

        EntityWrapper victimWrapper = WeaponMechanics.getEntityWrapper(victim, true);
        CastData victimCast;
        if (victimWrapper != null) {
            victimCast = new CastData(victimWrapper, weaponTitle, weaponStack);
        } else {
            victimCast = new CastData(victim, weaponTitle, weaponStack);
        }
        victimCast.setData(CommonDataTags.TARGET_LOCATION.name(), shooter.getLocation());
        victimCast.setData(CommonDataTags.SHOOTER_NAME.name(), shooter.getName());
        victimCast.setData(CommonDataTags.VICTIM_NAME.name(), victim.getName());

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
            Bukkit.getPluginManager().callEvent(new WeaponKillEntityEvent(weaponTitle, weaponStack, shooter, victim, damageEntityEvent));

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
