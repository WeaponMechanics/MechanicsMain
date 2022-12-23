package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.primitive.DoubleEntry;
import me.deecaad.core.utils.primitive.DoubleMap;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.MetadataKey;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import me.deecaad.weaponmechanics.weapon.stats.PlayerStat;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponKillEntityEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.StatsData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class DamageHandler {

    private WeaponHandler weaponHandler;

    public DamageHandler() {
    }

    public DamageHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    /**
     * @return false if damaging was cancelled
     */
    public boolean tryUse(LivingEntity victim, WeaponProjectile projectile, double damage, DamagePoint point, boolean isBackstab) {
        return tryUse(victim, damage, point, isBackstab, projectile.getShooter(), projectile.getWeaponTitle(),
                projectile.getWeaponStack(), projectile.getHand(), projectile.getDistanceTravelled());
    }

    /**
     * @return false if damaging was cancelled
     */
    public boolean tryUse(LivingEntity victim, double damage, DamagePoint point, boolean isBackstab,
                          LivingEntity shooter, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, double distanceTravelled) {
        Configuration config = getConfigurations();

        if (!DamageUtil.canHarmScoreboardTeams(shooter, victim) && !config.getBool(weaponTitle + ".Damage.Ignore_Teams")) {
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

        WeaponDamageEntityEvent damageEntityEvent = new WeaponDamageEntityEvent(weaponTitle, weaponStack, shooter, slot, victim,
                damage, isBackstab, isCritical, point, armorDamage, fireTicks, distanceTravelled);
        Bukkit.getPluginManager().callEvent(damageEntityEvent);

        if (damageEntityEvent.isCancelled()) return false;

        fireTicks = damageEntityEvent.getFireTicks();
        point = damageEntityEvent.getPoint();
        double finalDamage = damageEntityEvent.getFinalDamage();

        if (DamageUtil.apply(shooter, victim, finalDamage)) {
            WeaponMechanics.debug.debug("Damage was cancelled");

            // Damage was cancelled
            return false;
        }

        // Don't do WM armor damage when using vanilla damaging
        if (!getBasicConfigurations().getBool("Damage.Use_Vanilla_Damaging", false)) {
            DamageUtil.damageArmor(victim, damageEntityEvent.getArmorDamage(), point);
        }

        // Fire ticks
        if (fireTicks > 0) {
            victim.setFireTicks(fireTicks);
        }

        EntityWrapper shooterWrapper = WeaponMechanics.getEntityWrapper(shooter, true);

        Map<String, String> tempPlaceholders = new HashMap<>();
        tempPlaceholders.put("%shooter%", shooter.getName());
        tempPlaceholders.put("%victim%", victim.getName());

        CastData shooterCast = new CastData(shooter, weaponTitle, weaponStack, tempPlaceholders)
                .setData(DataTag.TARGET_LOCATION.name(), victim.getLocation());

        EntityWrapper victimWrapper = WeaponMechanics.getEntityWrapper(victim, true);
        CastData victimCast = new CastData(victim, weaponTitle, weaponStack, tempPlaceholders)
                .setData(DataTag.TARGET_LOCATION.name(), shooter.getLocation());

        StatsData shooterData = shooter.getType() == EntityType.PLAYER ? ((PlayerWrapper) shooterWrapper).getStatsData() : null;
        StatsData victimData = victim.getType() == EntityType.PLAYER ? ((PlayerWrapper) victimWrapper).getStatsData() : null;

        // On all damage
        useMechanics(config, shooterCast, victimCast, weaponTitle + ".Damage");
        if (shooterData != null) {
            shooterData.add(weaponTitle, WeaponStat.TOTAL_DAMAGE, (float) finalDamage);
            shooterData.set(weaponTitle, WeaponStat.LONGEST_DISTANCE_HIT,
                    (key, value) -> value == null ? (float) distanceTravelled : Math.max((float) value, (float) distanceTravelled));
        }
        if (victimData != null) victimData.add(PlayerStat.DAMAGE_TAKEN, (float) finalDamage);

        boolean killed = false;
        if (victim.isDead() || victim.getHealth() <= 0.0) {
            killed = true;
            Bukkit.getPluginManager().callEvent(new WeaponKillEntityEvent(weaponTitle, weaponStack, shooter, slot, victim, damageEntityEvent));

            // On kill
            useMechanics(config, shooterCast, victimCast, weaponTitle + ".Damage.Kill");
            if (victimData != null) victimData.add(PlayerStat.WEAPON_DEATHS, 1);

            if (shooterData != null) {
                if (victim.getType() == EntityType.PLAYER) {
                    shooterData.add(weaponTitle, WeaponStat.PLAYER_KILLS, 1);
                } else {
                    shooterData.add(weaponTitle, WeaponStat.OTHER_KILLS, 1);
                }
                shooterData.set(weaponTitle, WeaponStat.LONGEST_DISTANCE_KILL,
                        (key, value) -> value == null ? (float) distanceTravelled : Math.max((float) value, (float) distanceTravelled));
            }
        } else if (shooter.getType() == EntityType.PLAYER && getBasicConfigurations().getBool("Assists_Event.Enable", true)
                && (!getBasicConfigurations().getBool("Assists_Event.Only_Players", true) || victim.getType() == EntityType.PLAYER)) {

            // If shot didn't kill entity, log assist damage
            AssistData assistData;
            if (MetadataKey.ASSIST_DATA.has(victim)) {
                assistData = (AssistData) MetadataKey.ASSIST_DATA.get(victim).get(0).value();
            } else {
                MetadataKey.ASSIST_DATA.set(victim, assistData = new AssistData());
            }
            assistData.logDamage((Player) shooter, weaponTitle, weaponStack, finalDamage);

        }

        // On backstab
        if (damageEntityEvent.isBackstab()) {
            useMechanics(config, shooterCast, victimCast, weaponTitle + ".Damage.Backstab");
            if (shooterData != null) {
                shooterData.add(weaponTitle, WeaponStat.BACKSTABS, 1);
                if (killed) shooterData.add(weaponTitle, WeaponStat.BACKSTAB_KILLS, 1);
            }
        }

        // On critical
        if (damageEntityEvent.isCritical()) {
            useMechanics(config, shooterCast, victimCast, weaponTitle + ".Damage.Critical_Hit");
            if (shooterData != null) {
                shooterData.add(weaponTitle, WeaponStat.CRITICAL_HITS, 1);
                if (killed) shooterData.add(weaponTitle, WeaponStat.CRITICAL_KILLS, 1);
            }
        }

        // On point
        if (point != null) {
            useMechanics(config, shooterCast, victimCast, weaponTitle + ".Damage." + point.getReadable());

            if (shooterData != null) {
                switch (point) {
                    case HEAD:
                        shooterData.add(weaponTitle, WeaponStat.HEAD_HITS, 1);
                        if (killed) shooterData.add(weaponTitle, WeaponStat.HEAD_KILLS, 1);
                        break;
                    case BODY:
                        shooterData.add(weaponTitle, WeaponStat.BODY_HITS, 1);
                        if (killed) shooterData.add(weaponTitle, WeaponStat.BODY_KILLS, 1);
                        break;
                    case ARMS:
                        shooterData.add(weaponTitle, WeaponStat.ARM_HITS, 1);
                        if (killed) shooterData.add(weaponTitle, WeaponStat.ARM_KILLS, 1);
                        break;
                    case LEGS:
                        shooterData.add(weaponTitle, WeaponStat.LEG_HITS, 1);
                        if (killed) shooterData.add(weaponTitle, WeaponStat.LEG_KILLS, 1);
                        break;
                    case FEET:
                        shooterData.add(weaponTitle, WeaponStat.FOOT_HITS, 1);
                        if (killed) shooterData.add(weaponTitle, WeaponStat.FOOT_KILLS, 1);
                        break;
                }
            }
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
