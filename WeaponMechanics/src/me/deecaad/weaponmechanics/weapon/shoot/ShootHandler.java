package me.deecaad.weaponmechanics.weapon.shoot;

import me.deecaad.compatibility.worldguard.IWorldGuardCompatibility;
import me.deecaad.compatibility.worldguard.WorldGuardAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmSound;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmState;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmType;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.Projectile;
import me.deecaad.weaponmechanics.weapon.reload.ReloadHandler;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.Recoil;
import me.deecaad.weaponmechanics.weapon.shoot.spread.Spread;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponPreShootEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponShootEvent;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class ShootHandler implements IValidator {

    private WeaponHandler weaponHandler;

    /**
     * Hardcoded reset millis time for things like recoil reset, spread reset, etc.
     */
    public static long RESET_MILLIS = 1000;

    /**
     * Hardcoded full auto values
     */
    private static final int[][] auto = new int[][] {
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // 1 good
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // 2 good
            {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, // 3
            {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0}, // 4 good
            {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, // 5 good

            {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0}, // 6
            {1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0}, // 7
            {1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0}, // 8
            {1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0}, // 9
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0}, // 10 good

            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1}, // 11
            {1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1}, // 12
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1}, // 13
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1}, // 14
            {1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0}, // 15 good

            {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // 16
            {1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // 17
            {1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // 18
            {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // 19
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}  // 20 good
    };

    public ShootHandler() { }

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
        HandData handData = slot == EquipmentSlot.HAND ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();

        // Don't even try if slot is already being used for full auto or burst
        if (handData.isUsingFullAuto() || handData.isUsingBurst()) return false;

        Configuration config = getConfigurations();

        Trigger trigger = config.getObject(weaponTitle + ".Shoot.Trigger", Trigger.class);
        if (trigger == null || !trigger.check(triggerType, slot, entityWrapper)) return false;

        WeaponPreShootEvent preShootEvent = new WeaponPreShootEvent(weaponTitle, weaponStack, entityWrapper.getEntity());
        Bukkit.getPluginManager().callEvent(preShootEvent);
        if (preShootEvent.isCancelled()) return false;

        boolean mainhand = slot == EquipmentSlot.HAND;

        // Handle worldguard flags
        IWorldGuardCompatibility worldGuard = WorldGuardAPI.getWorldGuardCompatibility();
        Location loc = entityWrapper.getEntity().getLocation();
        boolean isCancelled;
        if (entityWrapper instanceof IPlayerWrapper) {
            isCancelled = !worldGuard.testFlag(loc, ((IPlayerWrapper) entityWrapper).getPlayer(), "weapon-shoot");
        } else {
            isCancelled = !worldGuard.testFlag(loc, null, "weapon-shoot");
        }

        if (isCancelled) {
            Object obj = worldGuard.getValue(loc, "weapon-shoot-message");
            if (obj != null && !obj.toString().isEmpty()) {
                entityWrapper.getEntity().sendMessage(StringUtil.color(obj.toString()));
            }

            return false;
        }

        ReloadHandler reloadHandler = weaponHandler.getReloadHandler();
        int ammoLeft = reloadHandler.getAmmoLeft(weaponStack);

        // FIREARM START

        FirearmAction firearmAction = config.getObject(weaponTitle + ".Firearm_Action", FirearmAction.class);

        if (firearmAction != null && !firearmAction.hasReadyFirearmActions(weaponStack)) {
            // Don't let shoot if they aren't ready

            if (firearmAction.hasReloadState(weaponStack)) {
                if (ammoLeft > 0) {

                    // Close if ammo left is more than 0
                    handData.stopReloadingTasks();

                    firearmAction.closeShootState(weaponStack, entityWrapper);

                    CastData castData = new CastData(entityWrapper, weaponTitle, weaponStack);
                    // Set the extra data so SoundMechanic knows to save task id to hand's firearm action tasks
                    castData.setData(FirearmSound.getDataKeyword(), mainhand ? FirearmSound.MAIN_HAND.getId() : FirearmSound.OFF_HAND.getId());
                    firearmAction.useMechanics(castData, false);

                    WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                    if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);

                    handData.addFirearmActionTask(new BukkitRunnable() {
                        @Override
                        public void run() {
                            firearmAction.readyState(weaponStack, entityWrapper);
                            handData.stopFirearmActionTasks();
                        }
                    }.runTaskLater(WeaponMechanics.getPlugin(), firearmAction.getCloseTime()).getTaskId());

                } else {
                    reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
                }
            } else if (!handData.hasRunningFirearmAction()) {
                // Meaning that firearm actions were probably cancelled by switching hand
                // -> Continue where they left on
                doShootFirearmActions(entityWrapper, weaponTitle, weaponStack, handData, mainhand);
            }

            return false;
        }

        // FIREARM END

        // RELOAD START

        // Check if other hand is reloading and deny shooting if it is
        if (slot == EquipmentSlot.HAND) {
            if (entityWrapper.getOffHandData().isReloading()) {
                return false;
            }
        } else if (entityWrapper.getMainHandData().isReloading()) {
            return false;
        }

        // If no ammo left, start reloading
        if (ammoLeft == 0) {
            reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
            return false;
        } else if (handData.isReloading()) {
            handData.stopReloadingTasks();
        }

        // RELOAD END

        boolean usesSelectiveFire = config.getObject(weaponTitle + ".Shoot.Selective_Fire.Trigger", Trigger.class) != null;
        boolean isSelectiveFireAuto = false;
        int selectiveFire = 0;
        if (usesSelectiveFire) {
            selectiveFire = CustomTag.SELECTIVE_FIRE.getInteger(weaponStack);
            if (CustomTag.SELECTIVE_FIRE.hasInteger(weaponStack) && selectiveFire == SelectiveFireState.AUTO.getId()) {
                isSelectiveFireAuto = true;
            }
        }

        // Only check if selective fire doesn't have auto selected
        if (!isSelectiveFireAuto) {
            int delayBetweenShots = config.getInt(weaponTitle + ".Shoot.Delay_Between_Shots");
            if (delayBetweenShots != 0 && !NumberUtil.hasMillisPassed(handData.getLastShotTime(), delayBetweenShots)) return false;
        }

        if (usesSelectiveFire) {
            switch (selectiveFire) {
                case (1): // 1 = burst, can't use SelectiveFireState.BURST.getId() here
                    return burstShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield);
                case (2): // 2 = auto, can't use SelectiveFireState.AUTO.getId() here
                    return fullAutoShot(entityWrapper, weaponTitle, weaponStack, handData, slot, triggerType, dualWield);
                default:
                    return singleShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield);
            }
        }

        // First try full auto, then burst then single fire
        return fullAutoShot(entityWrapper, weaponTitle, weaponStack, handData, slot, triggerType, dualWield)
                || burstShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield)
                || singleShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield);
    }

    private boolean singleShot(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, HandData handData, EquipmentSlot slot, boolean dualWield) {
        boolean mainhand = slot == EquipmentSlot.HAND;

        // START RELOAD STUFF

        ReloadHandler reloadHandler = weaponHandler.getReloadHandler();
        reloadHandler.consumeAmmo(entityWrapper, weaponStack, 1);

        // END RELOAD STUFF

        shoot(entityWrapper, weaponTitle, weaponStack, getShootLocation(entityWrapper, dualWield, mainhand), mainhand, true);

        if (reloadHandler.getAmmoLeft(weaponStack) == 0) {
            reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
        } else {
            doShootFirearmActions(entityWrapper, weaponTitle, weaponStack, handData, mainhand);
        }

        return true;
    }

    private boolean burstShot(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, HandData handData, EquipmentSlot slot, boolean dualWield) {
        Configuration config = getConfigurations();
        int shotsPerBurst = config.getInt(weaponTitle + ".Shoot.Burst.Shots_Per_Burst");
        int ticksBetweenEachShot = config.getInt(weaponTitle + ".Shoot.Burst.Ticks_Between_Each_Shot");

        // Not used
        if (shotsPerBurst == 0 || ticksBetweenEachShot == 0) return false;

        boolean mainhand = slot == EquipmentSlot.HAND;

        handData.setBurstTask(new BukkitRunnable() {
            int shots = 0;

            @Override
            public void run() {

                // START RELOAD STUFF

                ReloadHandler reloadHandler = weaponHandler.getReloadHandler();
                if (!reloadHandler.consumeAmmo(entityWrapper, weaponStack, 1)) {
                    handData.setBurstTask(0);
                    cancel();

                    reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
                    return;
                }

                // END RELOAD STUFF

                // Only make the first projectile of burst modify spread change if its used
                shoot(entityWrapper, weaponTitle, weaponStack, getShootLocation(entityWrapper, dualWield, mainhand), mainhand, shots == 0);

                if (++shots >= shotsPerBurst) {
                    handData.setBurstTask(0);
                    cancel();

                    if (reloadHandler.getAmmoLeft(weaponStack) == 0) {
                        reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
                    } else {
                        doShootFirearmActions(entityWrapper, weaponTitle, weaponStack, handData, mainhand);
                    }
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, ticksBetweenEachShot).getTaskId());
        return true;
    }

    private boolean fullAutoShot(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, HandData handData, EquipmentSlot slot, TriggerType triggerType, boolean dualWield) {
        Configuration config = getConfigurations();
        int fullyAutomaticShotsPerSecond = config.getInt(weaponTitle + ".Shoot.Fully_Automatic_Shots_Per_Second");

        // Not used
        if (fullyAutomaticShotsPerSecond == 0) return false;

        int baseAmountPerTick = fullyAutomaticShotsPerSecond / 20;
        int extra = fullyAutomaticShotsPerSecond % 20;
        boolean mainhand = slot == EquipmentSlot.HAND;
        ReloadHandler reloadHandler = weaponHandler.getReloadHandler();

        handData.setFullAutoTask(new BukkitRunnable() {
            int tick = 0;
            public void run() {

                int ammoLeft = reloadHandler.getAmmoLeft(weaponStack);

                if (!keepFullAutoOn(entityWrapper, triggerType)) {
                    handData.setFullAutoTask(0);
                    cancel();

                    if (ammoLeft == 0) {
                        reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
                    } else {
                        doShootFirearmActions(entityWrapper, weaponTitle, weaponStack, handData, mainhand);
                    }

                    return;
                }

                int shootAmount;
                if (extra != 0) {
                    shootAmount = (baseAmountPerTick + auto[extra - 1][tick]);
                } else {
                    shootAmount = baseAmountPerTick;
                }

                // START RELOAD STUFF

                if (ammoLeft != -1) {

                    // Check whether shoot amount of this tick should be changed
                    if (ammoLeft - shootAmount < 0) {
                        shootAmount = ammoLeft;
                    }

                    if (!reloadHandler.consumeAmmo(entityWrapper, weaponStack, shootAmount)) {
                        handData.setFullAutoTask(0);
                        cancel();

                        reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
                        return;
                    }
                }

                // END RELOAD STUFF

                if (shootAmount == 1) {
                    shoot(entityWrapper, weaponTitle, weaponStack, getShootLocation(entityWrapper, dualWield, mainhand), mainhand, true);
                } else if (shootAmount > 1) { // Don't try to shoot in this tick if shoot amount is 0
                    for (int i = 0; i < shootAmount; ++i) {
                        shoot(entityWrapper, weaponTitle, weaponStack, getShootLocation(entityWrapper, dualWield, mainhand), mainhand, true);
                    }
                }

                if (++tick >= 20) {
                    tick = 0;
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, 0).getTaskId());
        return true;
    }

    public void doShootFirearmActions(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, HandData handData, boolean mainhand) {

        Configuration config = getConfigurations();
        FirearmAction firearmAction = config.getObject(weaponTitle + ".Firearm_Action", FirearmAction.class);
        if (firearmAction == null || handData.hasRunningFirearmAction()) return;

        // Return if firearm actions should not be done in this shot
        if (weaponHandler.getReloadHandler().getAmmoLeft(weaponStack) % firearmAction.getFirearmActionFrequency() != 0) return;

        // No need to do any firearm actions if its REVOLVER
        if (firearmAction.getFirearmType() == FirearmType.REVOLVER) return;

        // Otherwise open and close

        BukkitRunnable closeRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                firearmAction.readyState(weaponStack, entityWrapper);

                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);


                handData.stopFirearmActionTasks();
            }
        };

        // Check if OPEN state was already completed, but was cancelled on CLOSE state
        if (firearmAction.getState(weaponStack) == FirearmState.SHOOT_CLOSE) {

            // Only do CLOSE state

            CastData castData = new CastData(entityWrapper, weaponTitle, weaponStack);
            // Set the extra data so SoundMechanic knows to save task id to hand's firearm action tasks
            castData.setData(FirearmSound.getDataKeyword(), mainhand ? FirearmSound.MAIN_HAND.getId() : FirearmSound.OFF_HAND.getId());
            firearmAction.useMechanics(castData, false);

            WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
            if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);

            handData.addFirearmActionTask(closeRunnable.runTaskLater(WeaponMechanics.getPlugin(), firearmAction.getCloseTime()).getTaskId());

            return;
        }

        firearmAction.openShootState(weaponStack, entityWrapper);

        CastData castData = new CastData(entityWrapper, weaponTitle, weaponStack);
        // Set the extra data so SoundMechanic knows to save task id to hand's firearm action tasks
        castData.setData(FirearmSound.getDataKeyword(), mainhand ? FirearmSound.MAIN_HAND.getId() : FirearmSound.OFF_HAND.getId());
        firearmAction.useMechanics(castData, true);

        WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
        if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);

        handData.addFirearmActionTask(new BukkitRunnable() {
            @Override
            public void run() {

                firearmAction.closeShootState(weaponStack, entityWrapper);

                CastData castData = new CastData(entityWrapper, weaponTitle, weaponStack);
                // Set the extra data so SoundMechanic knows to save task id to hand's firearm action tasks
                castData.setData(FirearmSound.getDataKeyword(), mainhand ? FirearmSound.MAIN_HAND.getId() : FirearmSound.OFF_HAND.getId());
                firearmAction.useMechanics(castData, false);

                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);

                handData.addFirearmActionTask(closeRunnable.runTaskLater(WeaponMechanics.getPlugin(), firearmAction.getCloseTime()).getTaskId());

            }
        }.runTaskLater(WeaponMechanics.getPlugin(), firearmAction.getOpenTime()).getTaskId());
    }

    /**
     * Checks whether to keep full auto on with given trigger
     */
    private boolean keepFullAutoOn(IEntityWrapper entityWrapper, TriggerType triggerType) {
        switch (triggerType) {
            case START_SNEAK:
                return entityWrapper.isSneaking();
            case START_SPRINT:
                return entityWrapper.isSprinting();
            case RIGHT_CLICK:
                return entityWrapper.isRightClicking();
            case START_SWIM:
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

    /**
     * Shoots using weapon.
     * Does not use ammo nor check for it.
     */
    private void shoot(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, Location shootLocation, boolean mainHand, boolean updateSpreadChange) {
        Configuration config = getConfigurations();
        Projectile projectile = config.getObject(weaponTitle + ".Shoot.Projectile", Projectile.class);
        Spread spread = config.getObject(weaponTitle + ".Shoot.Spread", Spread.class);
        Recoil recoil = config.getObject(weaponTitle + ".Shoot.Recoil", Recoil.class);
        double projectileSpeed = config.getDouble(weaponTitle + ".Shoot.Projectile_Speed");
        LivingEntity livingEntity = entityWrapper.getEntity();

        HandData handData = mainHand ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();
        handData.setLastShotTime(System.currentTimeMillis());

        Mechanics shootMechanics = config.getObject(weaponTitle + ".Shoot.Mechanics", Mechanics.class);
        if (shootMechanics != null) shootMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

        WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
        if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);

        // Handle explosions
        Explosion explosion = config.getObject(weaponTitle + ".Explosion", Explosion.class);

        for (int i = 0; i < config.getInt(weaponTitle + ".Shoot.Projectiles_Per_Shot"); ++i) {

            // i == 0
            // -> Only allow spread changing on first shot
            Vector motion = spread != null
                    ? spread.getNormalizedSpreadDirection(entityWrapper, mainHand, i == 0 && updateSpreadChange).multiply(projectileSpeed)
                    : livingEntity.getLocation().getDirection().multiply(projectileSpeed);

            if (recoil != null && i == 0 && entityWrapper.getEntity() instanceof Player) {
                recoil.start((Player) entityWrapper.getEntity(), mainHand);
            }

            ICustomProjectile bullet = projectile.shoot(livingEntity, shootLocation, motion, weaponStack, weaponTitle);
            WeaponShootEvent shootEvent = new WeaponShootEvent(bullet);
            Bukkit.getPluginManager().callEvent(shootEvent);
            bullet = shootEvent.getProjectile();

            // Handle worldguard flags
            IWorldGuardCompatibility worldGuard = WorldGuardAPI.getWorldGuardCompatibility();
            Location loc = entityWrapper.getEntity().getLocation();
            boolean isCancelled;
            if (entityWrapper instanceof IPlayerWrapper) {
                isCancelled = !worldGuard.testFlag(loc, ((IPlayerWrapper) entityWrapper).getPlayer(), "weapon-explode");
            } else {
                isCancelled = !worldGuard.testFlag(loc, null, "weapon-explode");
            }

            if (isCancelled) {
                Object obj = worldGuard.getValue(loc, "weapon-explode-message");
                if (obj != null && !obj.toString().isEmpty()) {
                    entityWrapper.getEntity().sendMessage(StringUtil.color(obj.toString()));
                }
            }

            boolean canExplode = bullet.getTag("explosion-detonation") == null;
            if (!isCancelled && explosion != null && canExplode && explosion.getTriggers().contains(Explosion.ExplosionTrigger.SHOOT)) {

                ICustomProjectile finalBullet = bullet;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Vector v = finalBullet.getLocation();
                        Location origin = new Location(shootLocation.getWorld(), v.getX(), v.getY(), v.getZ());
                        explosion.explode(entityWrapper.getEntity(), origin, finalBullet);

                        finalBullet.setTag("explosion-detonation", "true");
                    }
                }.runTaskLater(WeaponMechanics.getPlugin(), explosion.getDelay());
            }

        }
    }

    /**
     * Get the shoot location based on dual wield and main hand
     */
    private Location getShootLocation(IEntityWrapper entityWrapper, boolean dualWield, boolean mainhand) {
        LivingEntity livingEntity = entityWrapper.getEntity();

        if (!dualWield) return livingEntity.getEyeLocation();

        double dividedWidth = WeaponCompatibilityAPI.getShootCompatibility().getWidth(livingEntity) / 2.0;
        double distance = mainhand ? 2.0 : 0.0;

        Location eyeLocation = livingEntity.getEyeLocation();
        double yawToRad = Math.toRadians(eyeLocation.getYaw() + 90 * distance);
        eyeLocation.setX(eyeLocation.getX() + (dividedWidth * Math.cos(yawToRad)));
        eyeLocation.setZ(eyeLocation.getZ() + (dividedWidth * Math.sin(yawToRad)));
        return eyeLocation;
    }

    @Override
    public String getKeyword() {
        return "Shoot";
    }

    @Override
    public void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path) {
        Trigger trigger = configuration.getObject(path + ".Trigger", Trigger.class);
        if (trigger == null) {
            debug.log(LogLevel.ERROR, "Tried to use shoot without defining trigger for it.",
                    "Located at file " + file + " in " + path + ".Trigger in configurations.");
        }

        Projectile projectile = configuration.getObject(path + ".Projectile", Projectile.class);
        if (projectile == null) {
            debug.log(LogLevel.ERROR, "Tried to use shoot without defining projectile for it.",
                    "Located at file " + file + " in " + path + ".Projectile in configurations.");
        }

        double projectileSpeed = configuration.getDouble(path + ".Projectile_Speed");
        if (projectileSpeed == 0) {
            debug.log(LogLevel.ERROR, "Tried to use shoot without defining projectile speed or it was 0.",
                    "Located at file " + file + " in " + path + ".Projectile_Speed in configurations.");
        } else {
            // Convert from more config friendly speed to normal
            // E.g. 40 -> 4.0
            configuration.set(path + ".Projectile_Speed", projectileSpeed * 0.1);
        }

        int delayBetweenShots = configuration.getInt(path + ".Delay_Between_Shots");
        if (delayBetweenShots != 0) {
            // Convert to millis
            configuration.set(path + ".Delay_Between_Shots", delayBetweenShots * 50);
        }

        int projectilesPerShot = configuration.getInt(path + ".Projectiles_Per_Shot");
        if (projectilesPerShot == 0) {
            configuration.set(path + ".Projectiles_Per_Shot", 1);
        } else if (projectilesPerShot < 1) {
            debug.log(LogLevel.ERROR, "Tried to use shoot where projectiles per shot was less than 1.",
                    "Located at file " + file + " in " + path + ".Projectiles_Per_Shot in configurations.");
        }

        boolean hasBurst = false;
        boolean hasAuto = false;

        int shotsPerBurst = configuration.getInt(path + ".Burst.Shots_Per_Burst");
        int ticksBetweenEachShot = configuration.getInt(path + ".Burst.Ticks_Between_Each_Shot");
        if (shotsPerBurst != 0 || ticksBetweenEachShot != 0) {
            hasBurst = true;
            if (shotsPerBurst < 1) {
                debug.log(LogLevel.ERROR, "Tried to use shots per burst with value less than 1.",
                        "Located at file " + file + " in " + path + ".Burst.Shots_Per_Burst in configurations.");
            }
            if (ticksBetweenEachShot < 1) {
                debug.log(LogLevel.ERROR, "Tried to use ticks between each shot with value less than 1.",
                        "Located at file " + file + " in " + path + ".Burst.Ticks_Between_Each_Shot in configurations.");
            }
        }

        int fullyAutomaticShotsPerSecond = configuration.getInt(path + ".Fully_Automatic_Shots_Per_Second");
        if (fullyAutomaticShotsPerSecond != 0) {
            hasAuto = true;
            if (fullyAutomaticShotsPerSecond < 1) {
                debug.log(LogLevel.ERROR, "Tried to use full auto with value less than 1.",
                        "Located at file " + file + " in " + path + ".Fully_Automatic_Shots_Per_Second in configurations.");
            }
        }

        boolean usesSelectiveFire = configuration.getObject(path + ".Selective_Fire.Trigger", Trigger.class) != null;
        if (usesSelectiveFire && !hasBurst && !hasAuto) {
            debug.log(LogLevel.ERROR, "Tried to use selective fire without defining full auto or burst.",
                    "You need to define at least other of them.",
                    "Located at file " + file + " in " + path + " in configurations.");
        }
    }
}