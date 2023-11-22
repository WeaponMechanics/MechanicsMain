package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
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

import static me.deecaad.weaponmechanics.WeaponMechanics.*;

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

    public boolean tryUse(LivingEntity victim, double damage, DamagePoint point, boolean isBackstab,
                          LivingEntity shooter, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, double distanceTravelled) {
        return tryUse(victim, damage, point, isBackstab, shooter, weaponTitle, weaponStack, slot, distanceTravelled, false);
    }

    /**
     * @return false if damaging was cancelled
     */
    public boolean tryUse(LivingEntity victim, double damage, DamagePoint point, boolean isBackstab,
                          LivingEntity shooter, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, double distanceTravelled, boolean isExplosion) {
        Configuration config = getConfigurations();

        if (!DamageUtil.canHarmScoreboardTeams(shooter, victim) && !config.getBool(weaponTitle + ".Damage.Ignore_Teams"))
            return false;

        boolean isOwnerImmune = config.getBool(weaponTitle + ".Damage.Enable_Owner_Immunity");
        if (isOwnerImmune && victim.equals(shooter))
            return false;

        // Critical Hit chance
        double critChance = config.getDouble(weaponTitle + ".Damage.Critical_Hit.Chance", -1) / 100;

        int armorDamage = config.getInt(weaponTitle + ".Damage.Armor_Damage");
        int fireTicks = config.getInt(weaponTitle + ".Damage.Fire_Ticks");

        // Check for per-weapon damage modifier. Otherwise, use default
        DamageModifier damageModifier;
        if (isExplosion) {
            damageModifier = config.getObject(weaponTitle + ".Damage.Explosion.Damage_Modifiers", DamageModifier.class);
            if (damageModifier == null)
                damageModifier = WeaponMechanics.getBasicConfigurations().getObject("Damage.Explosion.Damage_Modifiers", DamageModifier.class);
        } else {
            damageModifier = config.getObject(weaponTitle + ".Damage.Damage_Modifiers", DamageModifier.class);
            if (damageModifier == null)
                damageModifier = WeaponMechanics.getBasicConfigurations().getObject("Damage.Damage_Modifiers", DamageModifier.class);
        }

        // Make sure legacy-users have updated their config.yml file
        if (damageModifier == null) {
            debug.error("Could not find the default DamageModifiers... Are you using the outdated system?",
                    "In WeaponMechanics-2.5.0, we added a new damage system. Check the patch notes for an easy copy-paste solution",
                    "Your damage will not work until this is addressed");
            return false;
        }

        // Get Mechanics so attachments can modify them in the damage event
        Mechanics damageMechanics = config.getObject(weaponTitle + ".Damage.Mechanics", Mechanics.class);
        Mechanics killMechanics = config.getObject(weaponTitle + ".Damage.Kill.Mechanics", Mechanics.class);
        Mechanics backstabMechanics = config.getObject(weaponTitle + ".Damage.Backstab.Mechanics", Mechanics.class);
        Mechanics criticalHitMechanics = config.getObject(weaponTitle + ".Damage.Critical_Hit.Mechanics", Mechanics.class);
        Mechanics headMechanics = config.getObject(weaponTitle + ".Damage.Head.Mechanics", Mechanics.class);
        Mechanics bodyMechanics = config.getObject(weaponTitle + ".Damage.Body.Mechanics", Mechanics.class);
        Mechanics armsMechanics = config.getObject(weaponTitle + ".Damage.Arms.Mechanics", Mechanics.class);
        Mechanics legsMechanics = config.getObject(weaponTitle + ".Damage.Legs.Mechanics", Mechanics.class);
        Mechanics feetMechanics = config.getObject(weaponTitle + ".Damage.Feet.Mechanics", Mechanics.class);

        WeaponDamageEntityEvent damageEntityEvent = new WeaponDamageEntityEvent(weaponTitle, weaponStack, shooter, slot, victim,
                damage, isBackstab, critChance, point, armorDamage, fireTicks, isExplosion, distanceTravelled, damageModifier,
                damageMechanics, killMechanics, backstabMechanics, criticalHitMechanics, headMechanics, bodyMechanics,
                armsMechanics, legsMechanics, feetMechanics);
        Bukkit.getPluginManager().callEvent(damageEntityEvent);
        if (damageEntityEvent.isCancelled())
            return false;

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

        CastData cast = new CastData(shooter, weaponTitle, weaponStack);
        cast.setTargetEntity(victim);

        EntityWrapper victimWrapper = WeaponMechanics.getEntityWrapper(victim, true);
        StatsData shooterData = shooter.getType() == EntityType.PLAYER ? ((PlayerWrapper) shooterWrapper).getStatsData() : null;
        StatsData victimData = victim.getType() == EntityType.PLAYER ? ((PlayerWrapper) victimWrapper).getStatsData() : null;

        // On all damage
        if (damageEntityEvent.getDamageMechanics() != null)
            damageEntityEvent.getDamageMechanics().use(cast);
        if (shooterData != null) {
            shooterData.add(weaponTitle, WeaponStat.TOTAL_DAMAGE, (float) finalDamage);
            shooterData.set(weaponTitle, WeaponStat.LONGEST_DISTANCE_HIT,
                    (key, value) -> value == null ? (float) distanceTravelled : Math.max((float) value, (float) distanceTravelled));
        }
        if (victimData != null)
            victimData.add(PlayerStat.DAMAGE_TAKEN, (float) finalDamage);

        boolean killed = false;
        if (victim.isDead() || victim.getHealth() <= 0.0) {
            killed = true;
            Bukkit.getPluginManager().callEvent(new WeaponKillEntityEvent(weaponTitle, weaponStack, shooter, slot, victim, damageEntityEvent));

            if (damageEntityEvent.getKillMechanics() != null)
                damageEntityEvent.getKillMechanics().use(cast);

            if (victimData != null)
                victimData.add(PlayerStat.WEAPON_DEATHS, 1);

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
            if (damageEntityEvent.getBackstabMechanics() != null)
                damageEntityEvent.getBackstabMechanics().use(cast);

            if (shooterData != null) {
                shooterData.add(weaponTitle, WeaponStat.BACKSTABS, 1);
                if (killed) shooterData.add(weaponTitle, WeaponStat.BACKSTAB_KILLS, 1);
            }
        }

        // On critical
        if (damageEntityEvent.isWasCritical()) {
            if (damageEntityEvent.getCriticalHitMechanics() != null)
                damageEntityEvent.getCriticalHitMechanics().use(cast);

            if (shooterData != null) {
                shooterData.add(weaponTitle, WeaponStat.CRITICAL_HITS, 1);
                if (killed) shooterData.add(weaponTitle, WeaponStat.CRITICAL_KILLS, 1);
            }
        }

        // On point
        if (point != null) {
            Mechanics mechanics = switch (point) {
                case HEAD -> damageEntityEvent.getHeadMechanics();
                case BODY -> damageEntityEvent.getBodyMechanics();
                case ARMS -> damageEntityEvent.getArmsMechanics();
                case LEGS -> damageEntityEvent.getLegsMechanics();
                case FEET -> damageEntityEvent.getFeetMechanics();
            };
            if (mechanics != null)
                mechanics.use(cast);

            if (shooterData != null) {
                switch (point) {
                    case HEAD -> {
                        shooterData.add(weaponTitle, WeaponStat.HEAD_HITS, 1);
                        if (killed) shooterData.add(weaponTitle, WeaponStat.HEAD_KILLS, 1);
                    }
                    case BODY -> {
                        shooterData.add(weaponTitle, WeaponStat.BODY_HITS, 1);
                        if (killed) shooterData.add(weaponTitle, WeaponStat.BODY_KILLS, 1);
                    }
                    case ARMS -> {
                        shooterData.add(weaponTitle, WeaponStat.ARM_HITS, 1);
                        if (killed) shooterData.add(weaponTitle, WeaponStat.ARM_KILLS, 1);
                    }
                    case LEGS -> {
                        shooterData.add(weaponTitle, WeaponStat.LEG_HITS, 1);
                        if (killed) shooterData.add(weaponTitle, WeaponStat.LEG_KILLS, 1);
                    }
                    case FEET -> {
                        shooterData.add(weaponTitle, WeaponStat.FOOT_HITS, 1);
                        if (killed) shooterData.add(weaponTitle, WeaponStat.FOOT_KILLS, 1);
                    }
                }
            }
        }

        return true;
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

            tryUse(victim, damage * entry.getValue(), null, backstab, projectile.getShooter(), weaponTitle, projectile.getWeaponStack(), projectile.getHand(), projectile.getDistanceTravelled(), true);
        }
    }
}
