package me.deecaad.weaponmechanics.weapon.damage;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.mechanics.scope.CastScope;
import me.deecaad.core.mechanics.scope.Context;
import me.deecaad.core.mechanics.scope.Target;
import me.deecaad.core.mechanics.program.Program;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.MetadataKey;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
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
import org.jetbrains.annotations.NotNull;

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
    public boolean tryUse(@NotNull WeaponDamageSource source, @NotNull LivingEntity victim, double damage, @NotNull EquipmentSlot slot) {
        Configuration config = WeaponMechanics.getInstance().getWeaponConfigurations();

        if (source.getShooter() != null && !DamageUtil.canHarmScoreboardTeams(source.getShooter(), victim) && !config.getBoolean(source.getWeaponTitle() + ".Damage.Ignore_Teams"))
            return false;

        boolean isOwnerImmune = config.getBoolean(source.getWeaponTitle() + ".Damage.Enable_Owner_Immunity");
        if (isOwnerImmune && victim.equals(source.getShooter()))
            return false;

        // Critical Hit chance
        double critChance = config.getDouble(source.getWeaponTitle() + ".Damage.Critical_Hit.Chance", 0.0) / 100.0;

        int armorDamage = config.getInt(source.getWeaponTitle() + ".Damage.Armor_Damage");
        int fireTicks = config.getInt(source.getWeaponTitle() + ".Damage.Fire_Ticks");

        // Check for per-weapon damage modifier. Otherwise, use default
        DamageModifier damageModifier;
        if (source instanceof ExplosionDamageSource) {
            damageModifier = config.getObject(source.getWeaponTitle() + ".Damage.Explosion.Damage_Modifiers", DamageModifier.class);
            if (damageModifier == null)
                damageModifier = WeaponMechanics.getInstance().getConfiguration().getObject("Damage.Explosion.Damage_Modifiers", DamageModifier.class);
        } else {
            damageModifier = config.getObject(source.getWeaponTitle() + ".Damage.Damage_Modifiers", DamageModifier.class);
            if (damageModifier == null)
                damageModifier = WeaponMechanics.getInstance().getConfiguration().getObject("Damage.Damage_Modifiers", DamageModifier.class);
        }

        // Make sure legacy-users have updated their config.yml file
        if (damageModifier == null) {
            WeaponMechanics.getInstance().debugger.severe("Could not find the default DamageModifiers... Are you using the outdated system?",
                "In WeaponMechanics-2.5.0, we added a new damage system. Check the patch notes for an easy copy-paste solution",
                "Your damage will not work until this is addressed");
            return false;
        }

        // Get Mechanics so attachments can modify them in the damage event
        Program damageMechanics = config.getObject(source.getWeaponTitle() + ".Damage.Mechanics", Program.class);
        Program killMechanics = config.getObject(source.getWeaponTitle() + ".Damage.Kill.Mechanics", Program.class);
        Program backstabMechanics = config.getObject(source.getWeaponTitle() + ".Damage.Backstab.Mechanics", Program.class);
        Program criticalHitMechanics = config.getObject(source.getWeaponTitle() + ".Damage.Critical_Hit.Mechanics", Program.class);
        Program headMechanics = config.getObject(source.getWeaponTitle() + ".Damage.Head.Mechanics", Program.class);
        Program bodyMechanics = config.getObject(source.getWeaponTitle() + ".Damage.Body.Mechanics", Program.class);
        Program armsMechanics = config.getObject(source.getWeaponTitle() + ".Damage.Arms.Mechanics", Program.class);
        Program legsMechanics = config.getObject(source.getWeaponTitle() + ".Damage.Legs.Mechanics", Program.class);
        Program feetMechanics = config.getObject(source.getWeaponTitle() + ".Damage.Feet.Mechanics", Program.class);

        WeaponDamageEntityEvent damageEntityEvent = new WeaponDamageEntityEvent(source, slot, victim, damage,
            critChance, armorDamage, fireTicks, damageModifier, damageMechanics, killMechanics, backstabMechanics,
            criticalHitMechanics, headMechanics, bodyMechanics, armsMechanics, legsMechanics, feetMechanics);
        Bukkit.getPluginManager().callEvent(damageEntityEvent);
        if (damageEntityEvent.isCancelled())
            return false;

        fireTicks = damageEntityEvent.getFireTicks();
        double finalDamage = damageEntityEvent.getFinalDamage();

        if (DamageUtil.apply(source, victim, finalDamage)) {
            WeaponMechanics.getInstance().debugger.fine("Damage was cancelled");

            // Damage was cancelled
            return false;
        }

        // Don't do WM armor damage when using vanilla damaging
        if (!WeaponMechanics.getInstance().getConfiguration().getBoolean("Damage.Use_Vanilla_Damaging", false)) {
            DamageUtil.damageArmor(victim, source, damageEntityEvent.getArmorDamage());
        }

        // Fire ticks
        if (fireTicks > 0) {
            victim.setFireTicks(fireTicks);
        }

        CastScope cast = source.getShooter() == null ? null : CastScope.builder(source.getShooter()).itemTitle(source.getWeaponTitle()).item(source.getWeaponStack()).build();
        if (cast != null) {
            cast.setContext(CastScope.TARGET, Context.of(Target.of(victim)));
        }

        boolean isShooterPlayer = source.getShooter() != null && source.getShooter().getType() == EntityType.PLAYER;
        EntityWrapper shooterWrapper = source.getShooter() == null ? null : WeaponMechanics.getInstance().getEntityWrapper(source.getShooter(), true);
        EntityWrapper victimWrapper = WeaponMechanics.getInstance().getEntityWrapper(victim, true);
        StatsData shooterData = isShooterPlayer ? ((PlayerWrapper) shooterWrapper).getStatsData() : null;
        StatsData victimData = victim.getType() == EntityType.PLAYER ? ((PlayerWrapper) victimWrapper).getStatsData() : null;

        // On all damage
        if (damageEntityEvent.getDamageMechanics() != null)
            damageEntityEvent.getDamageMechanics().run(cast);
        if (shooterData != null) {
            shooterData.add(source.getWeaponTitle(), WeaponStat.TOTAL_DAMAGE, (float) finalDamage);

            if (source instanceof ProjectileDamageSource projectileSource) {
                float distanceTravelled = (float) projectileSource.getProjectile().getDistanceTravelled();
                shooterData.set(source.getWeaponTitle(), WeaponStat.LONGEST_DISTANCE_HIT,
                    (key, value) -> value == null ? distanceTravelled : Math.max((float) value, distanceTravelled));
            }
        }
        if (victimData != null)
            victimData.add(PlayerStat.DAMAGE_TAKEN, (float) finalDamage);

        boolean killed = false;
        if (victim.isDead() || victim.getHealth() <= 0.0) {
            killed = true;
            var weaponKillEvent = new WeaponKillEntityEvent(source.getWeaponTitle(), source.getWeaponStack(), source.getShooter(), slot, victim, damageEntityEvent);
            Bukkit.getPluginManager().callEvent(weaponKillEvent);

            if (damageEntityEvent.getKillMechanics() != null)
                damageEntityEvent.getKillMechanics().run(cast);

            if (victimData != null)
                victimData.add(PlayerStat.WEAPON_DEATHS, 1);

            if (shooterData != null) {
                if (victim.getType() == EntityType.PLAYER) {
                    shooterData.add(source.getWeaponTitle(), WeaponStat.PLAYER_KILLS, 1);
                } else {
                    shooterData.add(source.getWeaponTitle(), WeaponStat.OTHER_KILLS, 1);
                }
                if (source instanceof ProjectileDamageSource projectileSource) {
                    float distanceTravelled = (float) projectileSource.getProjectile().getDistanceTravelled();
                    shooterData.set(source.getWeaponTitle(), WeaponStat.LONGEST_DISTANCE_KILL,
                        (key, value) -> value == null ? distanceTravelled : Math.max((float) value, distanceTravelled));
                }
            }
        } else if (isShooterPlayer && config.getBoolean("Assists_Event.Enable", true)
            && (!config.getBoolean("Assists_Event.Only_Players", true) || victim.getType() == EntityType.PLAYER)) {

            // If shot didn't kill entity, log assist damage
            AssistData assistData;
            if (MetadataKey.ASSIST_DATA.has(victim)) {
                assistData = (AssistData) MetadataKey.ASSIST_DATA.get(victim).getFirst().value();
            } else {
                MetadataKey.ASSIST_DATA.set(victim, assistData = new AssistData());
            }
            assistData.logDamage((Player) source.getShooter(), source.getWeaponTitle(), source.getWeaponStack(), finalDamage);

        }

        // On backstab
        if (source instanceof MeleeDamageSource meleeSource && meleeSource.isBackStab()) {
            if (damageEntityEvent.getBackstabMechanics() != null)
                damageEntityEvent.getBackstabMechanics().run(cast);

            if (shooterData != null) {
                shooterData.add(source.getWeaponTitle(), WeaponStat.BACKSTABS, 1);
                if (killed)
                    shooterData.add(source.getWeaponTitle(), WeaponStat.BACKSTAB_KILLS, 1);
            }
        }

        // On critical
        if (damageEntityEvent.wasCritical()) {
            if (damageEntityEvent.getCriticalHitMechanics() != null)
                damageEntityEvent.getCriticalHitMechanics().run(cast);

            if (shooterData != null) {
                shooterData.add(source.getWeaponTitle(), WeaponStat.CRITICAL_HITS, 1);
                if (killed)
                    shooterData.add(source.getWeaponTitle(), WeaponStat.CRITICAL_KILLS, 1);
            }
        }

        // On point
        if (source.getDamagePoint() != null) {
            Program mechanics = switch (source.getDamagePoint()) {
                case HEAD -> damageEntityEvent.getHeadMechanics();
                case BODY -> damageEntityEvent.getBodyMechanics();
                case ARMS -> damageEntityEvent.getArmsMechanics();
                case LEGS -> damageEntityEvent.getLegsMechanics();
                case FEET -> damageEntityEvent.getFeetMechanics();
            };
            if (mechanics != null)
                mechanics.run(cast);

            if (shooterData != null) {
                switch (source.getDamagePoint()) {
                    case HEAD -> {
                        shooterData.add(source.getWeaponTitle(), WeaponStat.HEAD_HITS, 1);
                        if (killed)
                            shooterData.add(source.getWeaponTitle(), WeaponStat.HEAD_KILLS, 1);
                    }
                    case BODY -> {
                        shooterData.add(source.getWeaponTitle(), WeaponStat.BODY_HITS, 1);
                        if (killed)
                            shooterData.add(source.getWeaponTitle(), WeaponStat.BODY_KILLS, 1);
                    }
                    case ARMS -> {
                        shooterData.add(source.getWeaponTitle(), WeaponStat.ARM_HITS, 1);
                        if (killed)
                            shooterData.add(source.getWeaponTitle(), WeaponStat.ARM_KILLS, 1);
                    }
                    case LEGS -> {
                        shooterData.add(source.getWeaponTitle(), WeaponStat.LEG_HITS, 1);
                        if (killed)
                            shooterData.add(source.getWeaponTitle(), WeaponStat.LEG_KILLS, 1);
                    }
                    case FEET -> {
                        shooterData.add(source.getWeaponTitle(), WeaponStat.FOOT_HITS, 1);
                        if (killed)
                            shooterData.add(source.getWeaponTitle(), WeaponStat.FOOT_KILLS, 1);
                    }
                }
            }
        }

        return true;
    }

    public void tryUseExplosion(Explosion explosion, WeaponProjectile projectile, Location origin, Object2DoubleMap<LivingEntity> exposures) {
        Configuration config = WeaponMechanics.getInstance().getWeaponConfigurations();

        String weaponTitle = projectile.getWeaponTitle();
        WeaponDamageSource source = new ExplosionDamageSource(
            explosion,
            projectile.getShooter(),
            weaponTitle,
            projectile.getWeaponStack(),
            origin);
        double damage = config.getDouble(weaponTitle + ".Damage.Base_Explosion_Damage");
        if (damage == 0) {
            // If explosion damage isn't used, use Base_Damage
            damage = config.getDouble(weaponTitle + ".Damage.Base_Damage");
        }

        final double finalDamage = damage;
        exposures.forEach((entity, exposure) -> {
            tryUse(source, entity, finalDamage * exposure, projectile.getHand());
        });
    }
}
