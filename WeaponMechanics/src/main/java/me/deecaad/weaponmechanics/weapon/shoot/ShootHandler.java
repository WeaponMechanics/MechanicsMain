package me.deecaad.weaponmechanics.weapon.shoot;

import co.aikar.timings.lib.MCTiming;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.worldguard.WorldGuardCompatibility;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmSound;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmState;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.Projectile;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import me.deecaad.weaponmechanics.weapon.reload.ReloadHandler;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.Recoil;
import me.deecaad.weaponmechanics.weapon.shoot.spread.Spread;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponPreShootEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponShootEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.vivecraft.VSE;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.*;

public class ShootHandler implements IValidator {

    private WeaponHandler weaponHandler;

    /**
     * Hardcoded full auto values
     */
    private static final int[][] AUTO = new int[][] {
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // 1 good
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // 2 good
            {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, // 3
            {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0}, // 4 good
            {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, // 5 good

            {1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0}, // 6
            {1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0}, // 7
            {1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0}, // 8
            {1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0}, // 9
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0}, // 10 good

            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1}, // 11
            {1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1}, // 12
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1}, // 13
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1}, // 14
            {1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0}, // 15 good

            {1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1}, // 16
            {1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1}, // 17
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, // 18
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
     * @param dualWield     whether this was dual wield
     * @return true if was able to shoot
     */
    public boolean tryUse(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield, @Nullable LivingEntity knownVictim) {

        if (triggerType == TriggerType.MELEE && slot == EquipmentSlot.HAND) {
            return weaponHandler.getMeleeHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield, knownVictim);
        }

        Trigger trigger = getConfigurations().getObject(weaponTitle + ".Shoot.Trigger", Trigger.class);
        if (trigger == null || !trigger.check(triggerType, slot, entityWrapper)) return false;

        MCTiming shootHandlerTiming = WeaponMechanics.timing("Shoot Handler").startTiming();
        boolean result = shootWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield);
        shootHandlerTiming.stopTiming();

        return result;
    }

    /**
     * @return true if was able to shoot
     */
    public boolean shootWithoutTrigger(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield) {
        HandData handData = slot == EquipmentSlot.HAND ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();

        // Don't even try if slot is already being used for full auto or burst
        if (handData.isUsingFullAuto() || handData.isUsingBurst()) return false;

        Configuration config = getConfigurations();

        WeaponPreShootEvent preShootEvent = new WeaponPreShootEvent(weaponTitle, weaponStack, entityWrapper.getEntity());
        Bukkit.getPluginManager().callEvent(preShootEvent);
        if (preShootEvent.isCancelled()) return false;

        // Cancel shooting if we can only shoot while scoped.
        if (config.getBool(weaponTitle + ".Shoot.Only_Shoot_While_Scoped") && !handData.getZoomData().isZooming()) return false;

        boolean isMelee = triggerType == TriggerType.MELEE;

        // Handle worldguard flags
        WorldGuardCompatibility worldGuard = CompatibilityAPI.getWorldGuardCompatibility();
        Location loc = entityWrapper.getEntity().getLocation();
        if (!worldGuard.testFlag(loc, entityWrapper instanceof PlayerWrapper ? ((PlayerWrapper) entityWrapper).getPlayer() : null, "weapon-shoot")) {
            Object obj = worldGuard.getValue(loc, "weapon-shoot-message");
            if (obj != null && !obj.toString().isEmpty()) {
                entityWrapper.getEntity().sendMessage(StringUtil.color(obj.toString()));
            }

            return false;
        }

        LivingEntity shooter = entityWrapper.getEntity();

        // Handle permissions
        boolean hasPermission = weaponHandler.getInfoHandler().hasPermission(entityWrapper.getEntity(), weaponTitle);
        if (!hasPermission) {
            if (shooter.getType() == EntityType.PLAYER) {
                String permissionMessage = getBasicConfigurations().getString("Messages.Permissions.Use_Weapon", ChatColor.RED + "You do not have permission to use " + weaponTitle);
                shooter.sendMessage(PlaceholderAPI.applyPlaceholders(permissionMessage, (Player) shooter, weaponStack, weaponTitle, slot));
            }
            return false;
        }

        ReloadHandler reloadHandler = weaponHandler.getReloadHandler();

        if (!getConfigurations().getBool(weaponTitle + ".Shoot.Consume_Item_On_Shoot")) {
            reloadHandler.handleWeaponStackAmount(entityWrapper, weaponStack);
        }

        int ammoLeft = reloadHandler.getAmmoLeft(weaponStack, weaponTitle);

        // RELOAD START

        // Check if other hand is reloading and deny shooting if it is
        if (slot == EquipmentSlot.HAND) {
            if (entityWrapper.getOffHandData().isReloading()) {
                return false;
            }
        } else if (entityWrapper.getMainHandData().isReloading()) {
            return false;
        }

        // FIREARM START
        FirearmAction firearmAction = config.getObject(weaponTitle + ".Firearm_Action", FirearmAction.class);
        if (firearmAction != null) {
            FirearmState state = firearmAction.getState(weaponStack);
            if (state != FirearmState.READY) {
                // Firearm actions were left OPEN or CLOSE

                if (ammoLeft > 0) {
                    // Since weapon still has ammo, only CLOSE weapon and let it shoot AFTER that

                    // Cancel reload if its running
                    handData.stopReloadingTasks();

                    // Call shoot firearm actions, so they can complete firearm actions
                    doShootFirearmActions(entityWrapper, weaponTitle, weaponStack, handData, slot);
                } else {
                    // Else continue to reload from where it left on...
                    reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield, false);
                }

                // Return false since firearm state wasn't ready, and they need to be completed
                return false;
            }
        }
        // FIREARM END

        // If no ammo left, start reloading
        if (ammoLeft == 0) {
            reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield, false);
            return false;
        } else if (handData.isReloading()) {
            // Else if reloading, cancel it
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

        // Only check if selective fire doesn't have auto selected and it isn't melee
        if (!isSelectiveFireAuto && !isMelee) {
            int delayBetweenShots = config.getInt(weaponTitle + ".Shoot.Delay_Between_Shots");
            if (delayBetweenShots != 0 && !NumberUtil.hasMillisPassed(handData.getLastShotTime(), delayBetweenShots)) return false;
        }

        int weaponEquipDelay = config.getInt(weaponTitle + ".Info.Weapon_Equip_Delay");
        if (weaponEquipDelay != 0 && !NumberUtil.hasMillisPassed(handData.getLastEquipTime(), weaponEquipDelay)) return false;

        int shootDelayAfterScope = config.getInt(weaponTitle + ".Scope.Shoot_Delay_After_Scope");
        if (shootDelayAfterScope != 0 && !NumberUtil.hasMillisPassed(handData.getLastScopeTime(), shootDelayAfterScope)) return false;

        if (isMelee) {
            return singleShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield, isMelee);
        }

        if (usesSelectiveFire) {
            switch (selectiveFire) {
                case (1): // 1 = burst, can't use SelectiveFireState.BURST.getId() here
                    return burstShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield);
                case (2): // 2 = auto, can't use SelectiveFireState.AUTO.getId() here
                    return fullAutoShot(entityWrapper, weaponTitle, weaponStack, handData, slot, triggerType, dualWield);
                default:
                    return singleShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield, isMelee);
            }
        }

        // First try full auto, then burst then single fire
        return fullAutoShot(entityWrapper, weaponTitle, weaponStack, handData, slot, triggerType, dualWield)
                || burstShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield)
                || singleShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield, isMelee);
    }

    private boolean singleShot(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, HandData handData, EquipmentSlot slot, boolean dualWield, boolean isMelee) {
        boolean mainhand = slot == EquipmentSlot.HAND;
        boolean consumeItemOnShoot = getConfigurations().getBool(weaponTitle + ".Shoot.Consume_Item_On_Shoot");

        // START RELOAD STUFF

        ReloadHandler reloadHandler = weaponHandler.getReloadHandler();
        reloadHandler.consumeAmmo(weaponStack, weaponTitle, 1);

        // END RELOAD STUFF

        shoot(entityWrapper, weaponTitle, weaponStack, getShootLocation(entityWrapper.getEntity(), dualWield, mainhand), mainhand, true, isMelee);

        if (consumeItemOnShoot && handleConsumeItemOnShoot(weaponStack)) {
            return true;
        }

        if (reloadHandler.getAmmoLeft(weaponStack, weaponTitle) == 0) {
            reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield, false);
        } else {
            doShootFirearmActions(entityWrapper, weaponTitle, weaponStack, handData, slot);
        }

        return true;
    }

    private boolean burstShot(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, HandData handData, EquipmentSlot slot, boolean dualWield) {
        Configuration config = getConfigurations();
        int shotsPerBurst = config.getInt(weaponTitle + ".Shoot.Burst.Shots_Per_Burst");
        int ticksBetweenEachShot = config.getInt(weaponTitle + ".Shoot.Burst.Ticks_Between_Each_Shot");

        // Not used
        if (shotsPerBurst == 0 || ticksBetweenEachShot == 0) return false;

        boolean mainhand = slot == EquipmentSlot.HAND;
        boolean consumeItemOnShoot = getConfigurations().getBool(weaponTitle + ".Shoot.Consume_Item_On_Shoot");

        handData.setBurstTask(new BukkitRunnable() {
            int shots = 0;

            @Override
            public void run() {
                ItemStack taskReference = mainhand ? entityWrapper.getEntity().getEquipment().getItemInMainHand() : entityWrapper.getEntity().getEquipment().getItemInOffHand();
                if (taskReference == weaponStack) taskReference = weaponStack;

                // START RELOAD STUFF

                ReloadHandler reloadHandler = weaponHandler.getReloadHandler();

                if (entityWrapper.getMainHandData().isReloading() || entityWrapper.getOffHandData().isReloading()) {
                    handData.setBurstTask(0);
                    cancel();
                    return;
                }

                if (!reloadHandler.consumeAmmo(taskReference, weaponTitle, 1)) {
                    handData.setBurstTask(0);
                    cancel();

                    reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, taskReference, slot, dualWield, false);
                    return;
                }

                // END RELOAD STUFF

                // Only make the first projectile of burst modify spread change if its used
                shoot(entityWrapper, weaponTitle, taskReference, getShootLocation(entityWrapper.getEntity(), dualWield, mainhand), mainhand, shots == 0, false);

                if (consumeItemOnShoot && handleConsumeItemOnShoot(taskReference)) {
                    handData.setBurstTask(0);
                    cancel();
                    return;
                }

                if (++shots >= shotsPerBurst) {
                    handData.setBurstTask(0);
                    cancel();

                    if (reloadHandler.getAmmoLeft(taskReference, weaponTitle) == 0) {
                        reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, taskReference, slot, dualWield, false);
                    } else {
                        doShootFirearmActions(entityWrapper, weaponTitle, taskReference, handData, slot);
                    }
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, ticksBetweenEachShot).getTaskId());
        return true;
    }

    private boolean fullAutoShot(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, HandData handData, EquipmentSlot slot, TriggerType triggerType, boolean dualWield) {
        Configuration config = getConfigurations();
        int fullyAutomaticShotsPerSecond = config.getInt(weaponTitle + ".Shoot.Fully_Automatic_Shots_Per_Second");

        // Not used
        if (fullyAutomaticShotsPerSecond == 0) return false;

        int baseAmountPerTick = fullyAutomaticShotsPerSecond / 20;
        int extra = fullyAutomaticShotsPerSecond % 20;
        boolean mainhand = slot == EquipmentSlot.HAND;
        boolean consumeItemOnShoot = getConfigurations().getBool(weaponTitle + ".Shoot.Consume_Item_On_Shoot");
        ReloadHandler reloadHandler = weaponHandler.getReloadHandler();

        handData.setFullAutoTask(new BukkitRunnable() {
            int tick = 0;
            public void run() {
                ItemStack taskReference = mainhand ? entityWrapper.getEntity().getEquipment().getItemInMainHand() : entityWrapper.getEntity().getEquipment().getItemInOffHand();
                if (taskReference == weaponStack) taskReference = weaponStack;

                if (entityWrapper.getMainHandData().isReloading() || entityWrapper.getOffHandData().isReloading()) {
                    handData.setFullAutoTask(0);
                    cancel();
                    return;
                }

                int ammoLeft = reloadHandler.getAmmoLeft(taskReference, weaponTitle);

                if (!keepFullAutoOn(entityWrapper, triggerType)) {
                    handData.setFullAutoTask(0);
                    cancel();

                    if (ammoLeft == 0) {
                        reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, taskReference, slot, dualWield, false);
                    } else {
                        doShootFirearmActions(entityWrapper, weaponTitle, taskReference, handData, slot);
                    }

                    return;
                }

                int shootAmount;
                if (extra != 0) {
                    shootAmount = (baseAmountPerTick + AUTO[extra - 1][tick]);
                } else {
                    shootAmount = baseAmountPerTick;
                }

                // START RELOAD STUFF

                if (ammoLeft != -1) {

                    // Check whether shoot amount of this tick should be changed
                    if (ammoLeft - shootAmount < 0) {
                        shootAmount = ammoLeft;
                    }

                    if (!reloadHandler.consumeAmmo(taskReference, weaponTitle, shootAmount)) {
                        handData.setFullAutoTask(0);
                        cancel();

                        reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, taskReference, slot, dualWield, false);
                        return;
                    }
                }

                // END RELOAD STUFF

                if (shootAmount == 1) {
                    shoot(entityWrapper, weaponTitle, taskReference, getShootLocation(entityWrapper.getEntity(), dualWield, mainhand), mainhand, true, false);
                    if (consumeItemOnShoot && handleConsumeItemOnShoot(taskReference)) {
                        handData.setFullAutoTask(0);
                        cancel();
                        return;
                    }
                } else if (shootAmount > 1) { // Don't try to shoot in this tick if shoot amount is 0
                    for (int i = 0; i < shootAmount; ++i) {
                        shoot(entityWrapper, weaponTitle, taskReference, getShootLocation(entityWrapper.getEntity(), dualWield, mainhand), mainhand, true, false);
                        if (consumeItemOnShoot && handleConsumeItemOnShoot(taskReference)) {
                            handData.setFullAutoTask(0);
                            cancel();
                            return;
                        }
                    }
                }

                if (++tick >= 20) {
                    tick = 0;
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, 0).getTaskId());
        return true;
    }

    public void doShootFirearmActions(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, HandData handData, EquipmentSlot slot) {
        FirearmAction firearmAction = getConfigurations().getObject(weaponTitle + ".Firearm_Action", FirearmAction.class);
        if (firearmAction == null || handData.hasRunningFirearmAction()) return;

        FirearmState state = firearmAction.getState(weaponStack);

        // If state is ready, check if this shot should not cause firearm actions
        if (state == FirearmState.READY
                && (weaponHandler.getReloadHandler().getAmmoLeft(weaponStack, weaponTitle) % firearmAction.getFirearmActionFrequency() != 0
                || !firearmAction.getFirearmType().hasShootActions())) {
            return;
        }

        boolean mainhand = slot == EquipmentSlot.HAND;
        LivingEntity shooter = entityWrapper.getEntity();
        WeaponInfoDisplay weaponInfoDisplay = shooter.getType() != EntityType.PLAYER ? null : getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
        PlayerWrapper playerWrapper = weaponInfoDisplay == null ? null : (PlayerWrapper) entityWrapper;

        // Initiate CLOSE task
        BukkitRunnable closeRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack taskReference = mainhand ? entityWrapper.getEntity().getEquipment().getItemInMainHand() : entityWrapper.getEntity().getEquipment().getItemInOffHand();
                if (taskReference == weaponStack) {
                    taskReference = weaponStack;
                }
                firearmAction.changeState(taskReference, FirearmState.READY);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send(playerWrapper, slot);
                handData.stopFirearmActionTasks();
            }
        };

        // Init cast data
        CastData castData = new CastData(entityWrapper, weaponTitle, weaponStack);
        castData.setData(FirearmSound.getDataKeyword(), mainhand ? FirearmSound.MAIN_HAND.getId() : FirearmSound.OFF_HAND.getId());

        // Check if OPEN state was already completed
        if (state == FirearmState.CLOSE) {
            // Only do CLOSE state

            // Set the extra data so SoundMechanic knows to save task id to hand's firearm action tasks
            firearmAction.useMechanics(castData, false);

            if (weaponInfoDisplay != null) weaponInfoDisplay.send(playerWrapper, slot);

            if (getConfigurations().getBool(weaponTitle + ".Info.Show_Cooldown.Firearm_Actions_Time") && playerWrapper != null) {
                CompatibilityAPI.getEntityCompatibility().setCooldown(playerWrapper.getPlayer(), weaponStack.getType(), firearmAction.getCloseTime());
            }

            handData.addFirearmActionTask(closeRunnable.runTaskLater(WeaponMechanics.getPlugin(), firearmAction.getCloseTime()).getTaskId());

            // Return since we only want to do close state
            return;
        }

        // Else start from OPEN State

        // Update state
        if (state != FirearmState.OPEN) firearmAction.changeState(weaponStack, FirearmState.OPEN);

        // Set the extra data so SoundMechanic knows to save task id to hand's firearm action tasks
        firearmAction.useMechanics(castData, true);

        if (weaponInfoDisplay != null) weaponInfoDisplay.send(playerWrapper, slot);

        if (getConfigurations().getBool(weaponTitle + ".Info.Show_Cooldown.Firearm_Actions_Time") && playerWrapper != null) {
            CompatibilityAPI.getEntityCompatibility().setCooldown(playerWrapper.getPlayer(), weaponStack.getType(),
                    firearmAction.getOpenTime() + firearmAction.getCloseTime());
        }

        // Add the task to shoot firearm action tasks
        handData.addFirearmActionTask(new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack taskReference = mainhand ? entityWrapper.getEntity().getEquipment().getItemInMainHand() : entityWrapper.getEntity().getEquipment().getItemInOffHand();
                if (taskReference == weaponStack) taskReference = weaponStack;

                firearmAction.changeState(taskReference, FirearmState.CLOSE);

                // Set the extra data so SoundMechanic knows to save task id to hand's firearm action tasks
                CastData castData = new CastData(entityWrapper, weaponTitle, taskReference);
                castData.setData(FirearmSound.getDataKeyword(), mainhand ? FirearmSound.MAIN_HAND.getId() : FirearmSound.OFF_HAND.getId());
                firearmAction.useMechanics(castData, false);

                if (weaponInfoDisplay != null) weaponInfoDisplay.send(playerWrapper, slot);

                handData.addFirearmActionTask(closeRunnable.runTaskLater(WeaponMechanics.getPlugin(), firearmAction.getCloseTime()).getTaskId());

            }
        }.runTaskLater(WeaponMechanics.getPlugin(), firearmAction.getOpenTime()).getTaskId());
    }

    /**
     * Checks whether to keep full auto on with given trigger
     */
    private boolean keepFullAutoOn(EntityWrapper entityWrapper, TriggerType triggerType) {
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
    public void shoot(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, Location shootLocation, boolean mainHand, boolean updateSpreadChange, boolean isMelee) {
        Configuration config = getConfigurations();
        LivingEntity livingEntity = entityWrapper.getEntity();

        if (!isMelee) {
            HandData handData = mainHand ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();
            handData.setLastShotTime(System.currentTimeMillis());

            if (getConfigurations().getBool(weaponTitle + ".Info.Show_Cooldown.Delay_Between_Shots") && entityWrapper.getEntity().getType() == EntityType.PLAYER) {
                CompatibilityAPI.getEntityCompatibility().setCooldown((Player) entityWrapper.getEntity(), weaponStack.getType(),
                        config.getInt(weaponTitle + ".Shoot.Delay_Between_Shots") / 50);
            }
        }

        Mechanics shootMechanics = config.getObject(weaponTitle + ".Shoot.Mechanics", Mechanics.class);
        if (shootMechanics != null) shootMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

        if (entityWrapper instanceof PlayerWrapper) {
            WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
            if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
        }

        Projectile projectile = config.getObject(weaponTitle + ".Projectile", Projectile.class);

        if (projectile == null || isMelee) {
            // No projectile defined or was melee trigger
            return;
        }

        Spread spread = config.getObject(weaponTitle + ".Shoot.Spread", Spread.class);
        Recoil recoil = config.getObject(weaponTitle + ".Shoot.Recoil", Recoil.class);
        double projectileSpeed = config.getDouble(weaponTitle + ".Shoot.Projectile_Speed");

        for (int i = 0; i < config.getInt(weaponTitle + ".Shoot.Projectiles_Per_Shot"); ++i) {

            // i == 0
            // -> Only allow spread changing on first shot
            Vector motion = spread != null
                    ? spread.getNormalizedSpreadDirection(entityWrapper, shootLocation, mainHand, i == 0 && updateSpreadChange).multiply(projectileSpeed)
                    : shootLocation.getDirection().multiply(projectileSpeed);

            if (recoil != null && i == 0 && livingEntity instanceof Player) {
                recoil.start((Player) livingEntity, mainHand);
            }

            // Only create bullet first if WeaponShootEvent changes
            WeaponProjectile bullet = projectile.create(livingEntity, shootLocation, motion, weaponStack, weaponTitle);

            WeaponShootEvent shootEvent = new WeaponShootEvent(bullet);
            Bukkit.getPluginManager().callEvent(shootEvent);
            bullet = shootEvent.getProjectile();

            // Shoot the given bullet
            projectile.shoot(bullet, shootLocation);
        }
    }

    /**
     * Shoots using weapon.
     * Does not use ammo nor check for it.
     * Does not apply recoil nor anything that would require EntityWrapper.
     */
    public void shoot(LivingEntity livingEntity, String weaponTitle, Vector normalizedDirection) {
        Configuration config = getConfigurations();

        Mechanics shootMechanics = config.getObject(weaponTitle + ".Shoot.Mechanics", Mechanics.class);
        if (shootMechanics != null) shootMechanics.use(new CastData(livingEntity, weaponTitle, null));

        Projectile projectile = config.getObject(weaponTitle + ".Projectile", Projectile.class);
        if (projectile == null) return;

        Location shootLocation = getShootLocation(livingEntity, false, true);
        double projectileSpeed = config.getDouble(weaponTitle + ".Shoot.Projectile_Speed");

        for (int i = 0; i < config.getInt(weaponTitle + ".Shoot.Projectiles_Per_Shot"); ++i) {

            // Only create bullet first if WeaponShootEvent changes
            WeaponProjectile bullet = projectile.create(livingEntity, shootLocation, normalizedDirection.clone().multiply(projectileSpeed), null, weaponTitle);

            WeaponShootEvent shootEvent = new WeaponShootEvent(bullet);
            Bukkit.getPluginManager().callEvent(shootEvent);
            bullet = shootEvent.getProjectile();

            // Shoot the given bullet
            projectile.shoot(bullet, shootLocation);
        }
    }

    /**
     * Simply removes one from the amount of weapon stack
     *
     * @param weaponStack the weapon stack
     * @return true if weapon stack amount is now 0
     */
    public boolean handleConsumeItemOnShoot(ItemStack weaponStack) {
        int amount = weaponStack.getAmount() - 1;
        weaponStack.setAmount(amount);
        return amount <= 0;
    }

    /**
     * Get the shoot location based on dual wield and main hand
     */
    private Location getShootLocation(LivingEntity livingEntity, boolean dualWield, boolean mainhand) {

        if (Bukkit.getPluginManager().getPlugin("Vivecraft-Spigot-Extensions") != null
                && livingEntity.getType() == EntityType.PLAYER) {
            // Vivecraft support for VR players

            Player player = (Player) livingEntity;
            String getDataFrom = mainhand ? "righthand.pos" : "lefthand.pos";

            if (VSE.isVive(player) && player.hasMetadata(getDataFrom)) {
                // Now we know it's actually VR player

                // Get the position and direction from player metadata
                List<MetadataValue> metadataValueList = player.getMetadata(getDataFrom);
                for (MetadataValue meta : metadataValueList) {
                    if (meta.getOwningPlugin() == VSE.me) {
                        return (Location) meta.value();
                    }
                }

                throw new IllegalArgumentException("VR metadata " + getDataFrom + " from Vivecraft not found when player was VR player...?");
            }

            // Not VR player, let pass to these normal location finders
        }

        if (!dualWield) return livingEntity.getEyeLocation();

        double dividedWidth = WeaponCompatibilityAPI.getWeaponCompatibility().getWidth(livingEntity) / 2.0;

        double distance;
        if (livingEntity.getType() == EntityType.PLAYER && ((Player) livingEntity).getMainHand() == MainHand.LEFT) {
            // This rarely happens, but some players use main hand LEFT
            distance = mainhand ? 0.0 : 2.0;
        } else {
            distance = mainhand ? 2.0 : 0.0;
        }

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

        double projectileSpeed = configuration.getDouble(path + ".Projectile_Speed", 80);
        debug.validate(projectileSpeed > 0, "Projectile_Speed must be a positive number!",
                StringUtil.foundAt(file, path + ".Projectile_Speed"));

        // Convert from more config friendly speed to normal
        // E.g. 80 -> 4.0
        configuration.set(path + ".Projectile_Speed", projectileSpeed / 20);

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

        String defaultSelectiveFire = configuration.getString(path + ".Selective_Fire.Default");
        if (defaultSelectiveFire != null) {
            if (!defaultSelectiveFire.equalsIgnoreCase("SINGLE")
                    && !defaultSelectiveFire.equalsIgnoreCase("BURST")
                    && !defaultSelectiveFire.equalsIgnoreCase("AUTO") ) {
                debug.log(LogLevel.ERROR, "Tried to use selective fire default with invalid type.",
                        "You need to use one of the following: SINGLE, BURST or AUTO, now there was " + defaultSelectiveFire,
                        "Located at file " + file + " in " + path + " in configurations.");
            }
        }
    }
}