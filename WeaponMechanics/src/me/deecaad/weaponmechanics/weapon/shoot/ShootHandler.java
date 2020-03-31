package me.deecaad.weaponmechanics.weapon.shoot;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.worldguard.WorldGuardAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.projectile.Projectile;
import me.deecaad.weaponmechanics.weapon.shoot.spread.Spread;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class ShootHandler {

    private WeaponHandler weaponHandler;

    /**
     * Hardcoded full auto values
     */
    private static final int[][] auto = new int[][] {
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // 1 shot per second
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // 2
            {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, // 3
            {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0}, // 4
            {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, // 5

            {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0}, // 6
            {1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0}, // 7
            {1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0}, // 8
            {1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0}, // 9
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0}, // 10

            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1}, // 11
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1}, // 12
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1}, // 13
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1}, // 14
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // 15

            {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // 16
            {1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // 17
            {1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // 18
            {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // 19
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1} // 20
    };

    public ShootHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    /**
     * Tries to use shoot
     *
     * @param entityWrapper the entity who used trigger
     * @param weaponTitle   the weapon title
     * @param weaponStack   the weapon stack
     * @param slot          the slot used on trigger
     * @param triggerType   the trigger type trying to activate shoot
     * @return true if was able to shoot
     */
    public boolean tryUse(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield) {

        // Don't even try if slot is already being used for full auto
        if (entityWrapper.isUsingFullAuto(slot)) return false;

        Configuration config = getConfigurations();
        boolean mainHand = slot == EquipmentSlot.HAND;

        Trigger trigger = config.getObject(weaponTitle + ".Shoot.Trigger", Trigger.class);
        if (trigger == null) return false;

        double projectileSpeed = config.getDouble(weaponTitle + ".Shoot.Projectile_Speed") * 0.1;
        int projectilesPerShot = config.getInt(weaponTitle + ".Shoot.Projectiles_Per_Shot");

        boolean selectiveFire = config.getBool(weaponTitle + ".Shoot.Selective_Fire");
        int delayBetweenShots = config.getInt(weaponTitle + ".Shoot.Delay_Between_Shots");

        return true;
    }

    private void shootOneProjectile(IEntityWrapper entityWrapper, String weaponTitle, Location shootLocation, boolean updateSpreadChange) {
        Configuration config = getConfigurations();
        Projectile projectile = config.getObject(weaponTitle + ".Shoot.Projectile", Projectile.class);
        if (projectile == null) {
            DebugUtil.log(LogLevel.ERROR,
                    "Tried to shoot with weapon, but projectile was null?",
                    "Located at " + weaponTitle + ".Shoot.Projectile in configurations");
        }
        Spread spread = config.getObject(weaponTitle + ".Shoot.Spread", Spread.class);
        double projectileSpeed = config.getDouble(weaponTitle + ".Shoot.Projectile_Speed") * 0.1;

        LivingEntity livingEntity = entityWrapper.getEntity();
        Vector motion = spread != null
                ? spread.getNormalizedSpreadDirection(entityWrapper, updateSpreadChange).multiply(projectileSpeed)
                : livingEntity.getLocation().getDirection().multiply(projectileSpeed);

        projectile.shoot(livingEntity, shootLocation, motion);
    }

    private void shootProjectiles(IEntityWrapper entityWrapper, String weaponTitle, Location shootLocation, int amount, boolean allProjectilesUpdateSpreadChange) {
        for (int i = 0; i < amount; ++i) {
            if (i == 0) {
                // Only make the first projectile modify spread change if its used
                shootOneProjectile(entityWrapper, weaponTitle, shootLocation, true);
            } else {
                shootOneProjectile(entityWrapper, weaponTitle, shootLocation, allProjectilesUpdateSpreadChange);
            }
        }
    }

    private void shootBurst(IEntityWrapper entityWrapper, String weaponTitle, boolean dualWield, boolean mainhand) {
        Configuration config = getConfigurations();
        int shotsPerBurst = config.getInt(weaponTitle + ".Shoot.Burst.Shots_Per_Burst");
        int ticksBetweenEachShot = config.getInt(weaponTitle + ".Shoot.Burst.Ticks_Between_Each_Shot");
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i == 0) {
                    // Only make the first projectile of burst modify spread change if its used
                    shootOneProjectile(entityWrapper, weaponTitle, getShootLocation(entityWrapper, dualWield, mainhand), true);
                } else {
                    shootOneProjectile(entityWrapper, weaponTitle, getShootLocation(entityWrapper, dualWield, mainhand), false);
                }

                if (++i >= shotsPerBurst) {
                    cancel();
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, ticksBetweenEachShot);
    }

    private void shootFullAuto(IEntityWrapper entityWrapper, String weaponTitle, EquipmentSlot slot, TriggerType triggerType, boolean dualWield) {
        Configuration config = getConfigurations();
        int fullyAutomaticShotsPerSecond = config.getInt(weaponTitle + ".Shoot.Fully_Automatic_Shots_Per_Second");

        int baseAmountPerTick = fullyAutomaticShotsPerSecond / 20;
        int extra = fullyAutomaticShotsPerSecond % 20;
        boolean mainhand = slot == EquipmentSlot.HAND;

        entityWrapper.setUsingFullAuto(slot, true);
        new BukkitRunnable() {
            int tick = 0;
            public void run() {

                if (!entityWrapper.isUsingFullAuto(slot) || !keepFullAutoOn(entityWrapper, triggerType)) {
                    entityWrapper.setUsingFullAuto(slot, false);
                    cancel();
                    return;
                }

                int shootAmount;
                if (extra != 0) {
                    shootAmount = (baseAmountPerTick + auto[extra - 1][tick]);
                } else {
                    shootAmount = baseAmountPerTick;
                }

                if (shootAmount == 1) {
                    shootOneProjectile(entityWrapper, weaponTitle, getShootLocation(entityWrapper, dualWield, mainhand), true);
                } else if (shootAmount > 1) { // Don't try to shoot in this tick if shoot amount is 0
                    shootProjectiles(entityWrapper, weaponTitle, getShootLocation(entityWrapper, dualWield, mainhand), shootAmount, true);
                }

                if (++tick >= 20) {
                    tick = 0;
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, 0);
    }

    private boolean keepFullAutoOn(IEntityWrapper entityWrapper, TriggerType triggerType) {
        switch (triggerType) {
            case START_SNEAK:
                return entityWrapper.isSneaking();
            case START_SPRINT:
                return entityWrapper.isSprinting();
            case RIGHT_CLICK:
                return entityWrapper.isRightClicking();
            case START_SWIM_MODE:
                return entityWrapper.isSwimming();
            case START_GLIDE:
                return entityWrapper.isGliding();
            case START_WALK:
                return entityWrapper.isWalking();
            case START_IN_MIDAIR:
                return entityWrapper.isInMidair();
            case START_STAND:
                return entityWrapper.isStanding();
            default:
                return false;
        }
    }

    private Location getShootLocation(IEntityWrapper entityWrapper, boolean dualWield, boolean mainhand) {
        LivingEntity livingEntity = entityWrapper.getEntity();

        if (!dualWield) return livingEntity.getEyeLocation();

        double dividedWidth = CompatibilityAPI.getCompatibility().getShootCompatibility().getWidth(livingEntity) / 2.0;
        double distance = mainhand ? 2.0 : 0.0;

        Location eyeLocation = livingEntity.getEyeLocation();
        double yawToRad = Math.toRadians(eyeLocation.getYaw() + 90 * distance);
        eyeLocation.setX(eyeLocation.getX() + (dividedWidth * Math.cos(yawToRad)));
        eyeLocation.setZ(eyeLocation.getZ() + (dividedWidth * Math.sin(yawToRad)));
        return eyeLocation;
    }
}