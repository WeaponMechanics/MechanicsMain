package me.deecaad.weaponmechanics.weapon.shoot;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.worldguard.WorldGuardCompatibility;
import me.deecaad.core.file.*;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmState;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.Projectile;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import me.deecaad.weaponmechanics.weapon.reload.ReloadHandler;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.Recoil;
import me.deecaad.weaponmechanics.weapon.shoot.spread.Spread;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerListener;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.*;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.*;

public class ShootHandler implements IValidator, TriggerListener {

    private WeaponHandler weaponHandler;

    /**
     * Hardcoded full auto values. For every 1 in the array, the gun will fire
     * on that tick. Some indexes are marked as <i>"perfect"</i>. This means
     * that the delay between shots is exactly equal no matter what. Some
     * indexes are marked as <i>"good"</i>. This means that the distance
     * between zeros are equal.
     * <p>
     * Calculated using python: <blockquote><pre>{@code
     *     from collections import deque
     *
     *     for shotsPerSecond in range(1, 21):
     *         collection = deque([0] * 20)
     *         accumulate = 0
     *         for i in range(0, 20):
     *
     *             accumulate += shotsPerSecond / 20 + 0.00000000001
     *             if accumulate >= 1.0:
     *                 accumulate -= 1.0
     *                 collection[i] = 1
     *
     *         # shift over so the first tick is always a shot
     *         while collection[0] == 0:
     *             collection.rotate(-1)
     *
     *         print("\t{" + ", ".join(map(str, collection)) + "},")
     * }</pre></blockquote>
     * <p>
     * TODO Switch from int -> boolean for 1.6kb -> 400bits of ram
     */
    private static final int[][] AUTO = new int[][]{
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // 1 perfect
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // 2 perfect
            {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0}, // 4 perfect
            {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, // 5 perfect
            {1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0},
            {1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0},
            {1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0},
            {1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0},
            {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0}, // 10 perfect
            {1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0},
            {1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0},
            {1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0},
            {1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0},
            {1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0}, // 15 good
            {1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0}, // 16 good
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, // 18 good
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, // 19 good
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}  // 20 good
    };

    public ShootHandler() {
    }

    public ShootHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    @Override
    public boolean allowOtherTriggers() {
        return false;
    }

    @Override
    public boolean tryUse(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield, @Nullable LivingEntity knownVictim) {

        if (triggerType == TriggerType.MELEE && slot == EquipmentSlot.HAND) {
            return weaponHandler.getMeleeHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield, knownVictim);
        }

        Trigger trigger = getConfigurations().getObject(weaponTitle + ".Shoot.Trigger", Trigger.class);
        if (trigger == null || !trigger.check(triggerType, slot, entityWrapper)) return false;

        boolean result = shootWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield);

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

        WeaponPreShootEvent preShootEvent = new WeaponPreShootEvent(weaponTitle, weaponStack, entityWrapper.getEntity(), slot);
        Bukkit.getPluginManager().callEvent(preShootEvent);
        if (preShootEvent.isCancelled()) return false;

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
                    startReloadIfBothWeaponsEmpty(entityWrapper, weaponTitle, weaponStack, slot, dualWield, false);
                }

                // Return false since firearm state wasn't ready, and they need to be completed
                return false;
            }
        }
        // FIREARM END

        // If no ammo left, start reloading
        if (ammoLeft == 0) {
            startReloadIfBothWeaponsEmpty(entityWrapper, weaponTitle, weaponStack, slot, dualWield, false);
            return false;
        } else if (handData.isReloading()) {
            // Else if reloading, cancel it
            handData.stopReloadingTasks();
        }

        // RELOAD END

        boolean usesSelectiveFire = config.getObject(weaponTitle + ".Shoot.Selective_Fire.Trigger", Trigger.class) != null;
        SelectiveFireState selectiveFireState = SelectiveFireState.SINGLE;
        if (usesSelectiveFire) {
            int selectiveFireStateId = CustomTag.SELECTIVE_FIRE.getInteger(weaponStack);
            if (selectiveFireStateId >= 0 && selectiveFireStateId < SelectiveFireState.count()) {
                selectiveFireState = SelectiveFireState.getState(selectiveFireStateId);
            }
        }

        // Only check if selective fire doesn't have auto selected and it isn't melee
        if (selectiveFireState != SelectiveFireState.AUTO && !isMelee) {
            int delayBetweenShots = config.getInt(weaponTitle + ".Shoot.Delay_Between_Shots");
            if (delayBetweenShots != 0 && !NumberUtil.hasMillisPassed(handData.getLastShotTime(), delayBetweenShots))
                return false;
        }

        int weaponEquipDelay = config.getInt(weaponTitle + ".Info.Weapon_Equip_Delay");
        if (weaponEquipDelay != 0 && !NumberUtil.hasMillisPassed(handData.getLastEquipTime(), weaponEquipDelay))
            return false;

        int shootDelayAfterScope = config.getInt(weaponTitle + ".Scope.Shoot_Delay_After_Scope");
        if (shootDelayAfterScope != 0 && !NumberUtil.hasMillisPassed(handData.getLastScopeTime(), shootDelayAfterScope))
            return false;

        int shootDelayAfterReload = config.getInt(weaponTitle + ".Reload.Shoot_Delay_After_Reload");
        if (shootDelayAfterReload != 0 && !NumberUtil.hasMillisPassed(handData.getLastReloadTime(), shootDelayAfterReload))
            return false;

        if (isMelee) {
            return singleShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield, isMelee);
        }

        if (usesSelectiveFire) {
            return switch (selectiveFireState) {
                case BURST -> burstShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield);
                case AUTO ->
                        fullAutoShot(entityWrapper, weaponTitle, weaponStack, handData, slot, triggerType, dualWield);
                default -> singleShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield, isMelee);
            };
        }

        // First try full auto, then burst, then single fire
        return fullAutoShot(entityWrapper, weaponTitle, weaponStack, handData, slot, triggerType, dualWield)
                || burstShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield)
                || singleShot(entityWrapper, weaponTitle, weaponStack, handData, slot, dualWield, isMelee);
    }

    private boolean singleShot(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, HandData handData, EquipmentSlot slot, boolean dualWield, boolean isMelee) {
        boolean mainhand = slot == EquipmentSlot.HAND;
        boolean consumeItemOnShoot = getConfigurations().getBool(weaponTitle + ".Shoot.Consume_Item_On_Shoot");
        int ammoPerShot = getConfigurations().getInt(weaponTitle + ".Shoot.Ammo_Per_Shot", 1);

        // START RELOAD STUFF

        ReloadHandler reloadHandler = weaponHandler.getReloadHandler();
        if (!reloadHandler.consumeAmmo(weaponStack, weaponTitle, ammoPerShot))
            return false;

        // END RELOAD STUFF

        shoot(entityWrapper, weaponTitle, weaponStack, getShootLocation(entityWrapper.getEntity(), dualWield, mainhand), mainhand, true, isMelee);

        boolean consumeEmpty = getConfigurations().getBool(weaponTitle + ".Shoot.Destroy_When_Empty") && CustomTag.AMMO_LEFT.getInteger(weaponStack) == 0;
        if ((consumeEmpty || consumeItemOnShoot) && handleConsumeItemOnShoot(weaponStack, mainhand ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData())) {
            return true;
        }

        if (reloadHandler.getAmmoLeft(weaponStack, weaponTitle) == 0) {
            startReloadIfBothWeaponsEmpty(entityWrapper, weaponTitle, weaponStack, slot, dualWield, false);
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
        int ammoPerShot = getConfigurations().getInt(weaponTitle + ".Shoot.Ammo_Per_Shot", 1);

        handData.setBurstTask(new BukkitRunnable() {
            int shots = 0;

            @Override
            public void run() {
                ItemStack taskReference = mainhand ? entityWrapper.getEntity().getEquipment().getItemInMainHand() : entityWrapper.getEntity().getEquipment().getItemInOffHand();
                if (!taskReference.hasItemMeta()) {
                    handData.setBurstTask(0);
                    cancel();
                    return;
                }

                // START RELOAD STUFF

                ReloadHandler reloadHandler = weaponHandler.getReloadHandler();

                if (entityWrapper.getMainHandData().isReloading() || entityWrapper.getOffHandData().isReloading()) {
                    handData.setBurstTask(0);
                    cancel();
                    return;
                }

                if (!reloadHandler.consumeAmmo(taskReference, weaponTitle, ammoPerShot)) {
                    handData.setBurstTask(0);
                    cancel();

                    startReloadIfBothWeaponsEmpty(entityWrapper, weaponTitle, taskReference, slot, dualWield, false);
                    return;
                }

                // END RELOAD STUFF

                // Only make the first projectile of burst modify spread change if its used
                shoot(entityWrapper, weaponTitle, taskReference, getShootLocation(entityWrapper.getEntity(), dualWield, mainhand), mainhand, shots == 0, false);

                boolean consumeEmpty = getConfigurations().getBool(weaponTitle + ".Shoot.Destroy_When_Empty") && CustomTag.AMMO_LEFT.getInteger(weaponStack) == 0;
                if ((consumeEmpty || consumeItemOnShoot) && handleConsumeItemOnShoot(weaponStack, mainhand ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData())) {
                    return;
                }


                if (++shots >= shotsPerBurst) {
                    handData.setBurstTask(0);
                    cancel();

                    if (reloadHandler.getAmmoLeft(taskReference, weaponTitle) == 0) {
                        startReloadIfBothWeaponsEmpty(entityWrapper, weaponTitle, taskReference, slot, dualWield, false);
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

        Trigger trigger = config.getObject(weaponTitle + ".Shoot.Trigger", Trigger.class);

        // Not used
        if (fullyAutomaticShotsPerSecond == 0) return false;

        int baseAmountPerTick = fullyAutomaticShotsPerSecond / 20;
        int extra = fullyAutomaticShotsPerSecond % 20;
        boolean mainhand = slot == EquipmentSlot.HAND;
        boolean consumeItemOnShoot = getConfigurations().getBool(weaponTitle + ".Shoot.Consume_Item_On_Shoot");
        int ammoPerShot = getConfigurations().getInt(weaponTitle + ".Shoot.Ammo_Per_Shot", 1);
        ReloadHandler reloadHandler = weaponHandler.getReloadHandler();

        handData.setFullAutoTask(new BukkitRunnable() {
            int tick = 0;

            public void run() {
                ItemStack taskReference = mainhand ? entityWrapper.getEntity().getEquipment().getItemInMainHand() : entityWrapper.getEntity().getEquipment().getItemInOffHand();
                if (!taskReference.hasItemMeta()) {
                    handData.setFullAutoTask(0);
                    cancel();
                    return;
                }

                if (entityWrapper.getMainHandData().isReloading() || entityWrapper.getOffHandData().isReloading()) {
                    handData.setFullAutoTask(0);
                    cancel();
                    return;
                }

                int ammoLeft = reloadHandler.getAmmoLeft(taskReference, weaponTitle);

                if (!keepFullAutoOn(entityWrapper, triggerType, trigger)) {
                    handData.setFullAutoTask(0);
                    cancel();

                    if (ammoLeft == 0) {
                        startReloadIfBothWeaponsEmpty(entityWrapper, weaponTitle, taskReference, slot, dualWield, false);
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

                    if (!reloadHandler.consumeAmmo(taskReference, weaponTitle, shootAmount * ammoPerShot)) {
                        handData.setFullAutoTask(0);
                        cancel();

                        startReloadIfBothWeaponsEmpty(entityWrapper, weaponTitle, taskReference, slot, dualWield, false);
                        return;
                    }
                }

                // END RELOAD STUFF

                for (int i = 0; i < shootAmount; ++i) {
                    shoot(entityWrapper, weaponTitle, taskReference, getShootLocation(entityWrapper.getEntity(), dualWield, mainhand), mainhand, true, false);
                    boolean consumeEmpty = getConfigurations().getBool(weaponTitle + ".Shoot.Destroy_When_Empty") && CustomTag.AMMO_LEFT.getInteger(weaponStack) == 0;
                    if ((consumeEmpty || consumeItemOnShoot) && handleConsumeItemOnShoot(weaponStack, mainhand ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData())) {
                        return;
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
        PlayerWrapper playerWrapper = shooter.getType() != EntityType.PLAYER ? null : (PlayerWrapper) entityWrapper;
        WeaponInfoDisplay weaponInfoDisplay = playerWrapper == null ? null : getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);

        // Initiate CLOSE task
        BukkitRunnable closeRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack taskReference = mainhand ? entityWrapper.getEntity().getEquipment().getItemInMainHand() : entityWrapper.getEntity().getEquipment().getItemInOffHand();
                if (!taskReference.hasItemMeta()) {
                    handData.stopFirearmActionTasks();
                    return;
                }

                firearmAction.changeState(taskReference, FirearmState.READY);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send(playerWrapper, slot);
                handData.stopFirearmActionTasks();
            }
        };

        // Init cast data
        CastData castData = new CastData(shooter, weaponTitle, weaponStack, handData::addFirearmActionTask);

        // Check if OPEN state was already completed
        if (state == FirearmState.CLOSE) {
            // Only do CLOSE state

            WeaponFirearmEvent event = new WeaponFirearmEvent(weaponTitle, weaponStack, shooter, slot, firearmAction, state);
            Bukkit.getPluginManager().callEvent(event);

            // Set the extra data so SoundMechanic knows to save task id to hand's firearm action tasks
            event.useMechanics(castData, false);

            if (weaponInfoDisplay != null) weaponInfoDisplay.send(playerWrapper, slot);

            handData.addFirearmActionTask(closeRunnable.runTaskLater(WeaponMechanics.getPlugin(), event.getTime()).getTaskId());

            // Return since we only want to do close state
            return;
        }

        // Else start from OPEN State

        // Update state
        if (state != FirearmState.OPEN) firearmAction.changeState(weaponStack, FirearmState.OPEN);

        WeaponFirearmEvent event = new WeaponFirearmEvent(weaponTitle, weaponStack, shooter, slot, firearmAction, state);
        Bukkit.getPluginManager().callEvent(event);

        event.useMechanics(castData, true);

        if (weaponInfoDisplay != null) weaponInfoDisplay.send(playerWrapper, slot);

        // Add the task to shoot firearm action tasks
        handData.addFirearmActionTask(new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack taskReference = mainhand ? entityWrapper.getEntity().getEquipment().getItemInMainHand() : entityWrapper.getEntity().getEquipment().getItemInOffHand();
                if (!taskReference.hasItemMeta()) {
                    handData.stopFirearmActionTasks();
                    return;
                }

                firearmAction.changeState(taskReference, FirearmState.CLOSE);

                WeaponFirearmEvent event = new WeaponFirearmEvent(weaponTitle, weaponStack, shooter, slot, firearmAction, state);
                Bukkit.getPluginManager().callEvent(event);

                event.useMechanics(new CastData(shooter, weaponTitle, taskReference, handData::addFirearmActionTask), false);

                if (weaponInfoDisplay != null) weaponInfoDisplay.send(playerWrapper, slot);

                handData.addFirearmActionTask(closeRunnable.runTaskLater(WeaponMechanics.getPlugin(), event.getTime()).getTaskId());

            }
        }.runTaskLater(WeaponMechanics.getPlugin(), event.getTime()).getTaskId());
    }

    /**
     * Checks whether to keep full auto on with given trigger
     */
    private boolean keepFullAutoOn(EntityWrapper entityWrapper, TriggerType triggerType, Trigger trigger) {

        if (!trigger.checkCircumstances(entityWrapper)) {
            return false;
        }

        return switch (triggerType) {
            case START_SNEAK -> entityWrapper.isSneaking();
            case START_SPRINT -> entityWrapper.isSprinting();
            case RIGHT_CLICK -> entityWrapper.isRightClicking();
            case START_SWIM -> entityWrapper.isSwimming();
            case START_GLIDE -> entityWrapper.isGliding();
            case START_WALK -> entityWrapper.isWalking();
            case START_IN_MIDAIR -> entityWrapper.isInMidair();
            case START_STAND -> entityWrapper.isStanding();
            default -> false;
        };
    }

    private void startReloadIfBothWeaponsEmpty(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, boolean dualWield, boolean isReloadLoop) {
        if (entityWrapper.isReloading()) return;

        ReloadHandler reloadHandler = weaponHandler.getReloadHandler();

        HandData handData = slot == EquipmentSlot.HAND ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();

        if (!dualWield) {
            handData.cancelTasks();
            if (!reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield, isReloadLoop)) {
                // Only update skin if reload was cancelled
                weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);
            }

            return;
        }

        if (slot == EquipmentSlot.HAND ?
                reloadHandler.getAmmoLeft(entityWrapper.getEntity().getEquipment().getItemInOffHand(), null) == 0
                : reloadHandler.getAmmoLeft(entityWrapper.getEntity().getEquipment().getItemInMainHand(), null) == 0) {
            // Now we know that both weapons are empty assuming the other weapon's ammo amount is already checked before this

            handData.cancelTasks();

            if (!reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield, isReloadLoop)) {
                // Only update skin if reload was cancelled
                weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);
            }
        }
    }

    /**
     * Shoots using weapon.
     * Does not use ammo nor check for it.
     */
    public void shoot(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, Location shootLocation, boolean mainHand, boolean updateSpreadChange, boolean isMelee) {
        Configuration config = getConfigurations();
        LivingEntity livingEntity = entityWrapper.getEntity();

        Mechanics shootMechanics = config.getObject(weaponTitle + ".Shoot.Mechanics", Mechanics.class);
        boolean resetFallDistance = config.getBool(weaponTitle + ".Shoot.Reset_Fall_Distance");
        Projectile projectile = config.getObject(weaponTitle + ".Projectile", Projectile.class);
        double projectileSpeed = config.getDouble(weaponTitle + ".Shoot.Projectile_Speed");
        int projectileAmount = config.getInt(weaponTitle + ".Shoot.Projectiles_Per_Shot");

        PrepareWeaponShootEvent prepareEvent = new PrepareWeaponShootEvent(weaponTitle, weaponStack, entityWrapper.getEntity(), mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND, shootMechanics, resetFallDistance, projectile, projectileSpeed, projectileAmount);
        Bukkit.getPluginManager().callEvent(prepareEvent);
        if (prepareEvent.isCancelled())
            return;

        if (prepareEvent.getShootMechanics() != null) {
            prepareEvent.getShootMechanics().use(new CastData(livingEntity, weaponTitle, weaponStack));
            prepareEvent.getShootMechanics().clearDirty();
        }

        // Reset fall distance for #134
        if (prepareEvent.isResetFallDistance())
            livingEntity.setFallDistance(0.0f);

        if (entityWrapper instanceof PlayerWrapper playerWrapper) {
            // Counts melees as shots also
            if (playerWrapper.getStatsData() != null)
                playerWrapper.getStatsData().add(weaponTitle, WeaponStat.SHOTS, 1);

            WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
            if (weaponInfoDisplay != null)
                weaponInfoDisplay.send(playerWrapper, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
        }

        if (projectile == null || isMelee) {
            debug.debug("Missing projectile/isMelee for " + weaponTitle);
            // No projectile defined or was melee trigger

            // Update this AFTER shot (e.g. spread reset time won't work properly otherwise
            if (!isMelee) {
                WeaponPostShootEvent event = new WeaponPostShootEvent(weaponTitle, weaponStack, entityWrapper.getEntity(), mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
                Bukkit.getPluginManager().callEvent(event);

                HandData handData = mainHand ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();
                handData.setLastShotTime(System.currentTimeMillis());
            }

            return;
        }

        Spread spread = config.getObject(weaponTitle + ".Shoot.Spread", Spread.class);
        Recoil recoil = config.getObject(weaponTitle + ".Shoot.Recoil", Recoil.class);

        if (prepareEvent.getProjectileAmount() < 1) {
            debug.error(weaponTitle + ".Shoot.Projectiles_Per_Shot should be at least 1, got " + prepareEvent.getProjectileAmount());
        }

        for (int i = 0; i < prepareEvent.getProjectileAmount(); i++) {

            Location perProjectileShootLocation = shootLocation.clone();

            // i == prepareEvent.getProjectileAmount()
            // Change the spread after all pellets are shot
            Vector motion = spread != null
                    ? spread.getNormalizedSpreadDirection(entityWrapper, perProjectileShootLocation, mainHand, i == prepareEvent.getProjectileAmount() - 1 && updateSpreadChange).multiply(prepareEvent.getProjectileSpeed())
                    : perProjectileShootLocation.getDirection().multiply(prepareEvent.getProjectileSpeed());

            if (recoil != null && i == 0 && livingEntity instanceof Player) {
                recoil.start((Player) livingEntity, mainHand);
            }

            // Only create bullet first if WeaponShootEvent changes
            WeaponProjectile bullet = prepareEvent.getProjectile().create(livingEntity, perProjectileShootLocation, motion, weaponStack, weaponTitle, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);

            WeaponShootEvent shootEvent = new WeaponShootEvent(bullet);
            Bukkit.getPluginManager().callEvent(shootEvent);
            bullet = shootEvent.getProjectile();

            // Shoot the given bullet
            prepareEvent.getProjectile().shoot(bullet, perProjectileShootLocation);
        }

        // Apply custom durability
        CustomDurability durability = config.getObject(weaponTitle + ".Shoot.Custom_Durability", CustomDurability.class);
        if (durability != null) {
            boolean broke = durability.use(livingEntity, weaponStack, weaponTitle);

            if (broke)
                entityWrapper.getHandData(mainHand).cancelTasks();
        }

        WeaponPostShootEvent event = new WeaponPostShootEvent(weaponTitle, weaponStack, entityWrapper.getEntity(), mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
        Bukkit.getPluginManager().callEvent(event);

        // Update this AFTER shot (e.g. spread reset time won't work properly otherwise
        if (!isMelee) {
            HandData handData = mainHand ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();
            handData.setLastShotTime(System.currentTimeMillis());
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

            Location perProjectileShootLocation = shootLocation.clone();

            // Only create bullet first if WeaponShootEvent changes
            WeaponProjectile bullet = projectile.create(livingEntity, perProjectileShootLocation, normalizedDirection.clone().multiply(projectileSpeed), null, weaponTitle, null);

            WeaponShootEvent shootEvent = new WeaponShootEvent(bullet);
            Bukkit.getPluginManager().callEvent(shootEvent);
            bullet = shootEvent.getProjectile();

            // Shoot the given bullet
            projectile.shoot(bullet, perProjectileShootLocation);
        }

        WeaponPostShootEvent event = new WeaponPostShootEvent(weaponTitle, null, livingEntity, null);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Removes one from the amount of weapon stack.
     * If stack is now empty also cancels all hand tasks.
     *
     * @param weaponStack the weapon stack
     * @return true if weapon stack amount is now 0
     */
    public boolean handleConsumeItemOnShoot(ItemStack weaponStack, HandData handData) {
        int amount = weaponStack.getAmount() - 1;
        weaponStack.setAmount(amount);

        if (amount <= 0) {
            handData.cancelTasks();
            return true;
        }

        return false;
    }

    /**
     * Get the shoot location based on dual wield and main hand
     */
    private Location getShootLocation(LivingEntity livingEntity, boolean dualWield, boolean mainhand) {

        if (Bukkit.getPluginManager().getPlugin("Vivecraft-Spigot-Extensions") != null
                && livingEntity.getType() == EntityType.PLAYER) {
            // Vivecraft support for VR players

            VivePlayer vive = VSE.vivePlayers.get(livingEntity.getUniqueId());
            if (vive != null && vive.isVR()) {
                // Now we know it's actually VR player

                // Get the position and direction from player metadata
                return vive.getControllerPos(mainhand ? 0 : 1);
            }

            // Not VR player, let pass to these normal location finders
        }

        if (!dualWield) return livingEntity.getEyeLocation();

        double dividedWidth = CompatibilityAPI.getEntityCompatibility().getWidth(livingEntity) / 2.0;

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
    public List<String> getAllowedPaths() {
        return Collections.singletonList(".Shoot");
    }

    @Override
    public void validate(Configuration configuration, SerializeData data) throws SerializerException {
        Trigger trigger = configuration.getObject(data.key + ".Trigger", Trigger.class);
        if (trigger == null)
            throw new SerializerMissingKeyException(data.serializer, data.key + ".Trigger", data.of("Trigger").getLocation());

        double projectileSpeed = data.of("Projectile_Speed").assertPositive().getDouble(80);

        // Convert from more config friendly speed to normal
        // E.g. 80 -> 4.0
        configuration.set(data.key + ".Projectile_Speed", projectileSpeed / 20);

        int delayBetweenShots = data.of("Delay_Between_Shots").assertPositive().getInt(0);
        if (delayBetweenShots != 0) {
            // Convert to millis
            configuration.set(data.key + ".Delay_Between_Shots", delayBetweenShots * 50);
        }

        int projectilesPerShot = data.of("Projectiles_Per_Shot").assertRange(1, 100).getInt(1);
        configuration.set(data.key + ".Projectiles_Per_Shot", projectilesPerShot);

        boolean hasBurst = false;
        boolean hasAuto = false;

        int shotsPerBurst = data.of("Burst.Shots_Per_Burst").assertRange(1, 100).getInt(0);
        int ticksBetweenEachShot = data.of("Burst.Ticks_Between_Each_Shot").assertPositive().getInt(0);
        if (shotsPerBurst != 0 || ticksBetweenEachShot != 0) {
            hasBurst = true;
        }

        int fullyAutomaticShotsPerSecond = data.of("Fully_Automatic_Shots_Per_Second").assertRange(0, 120).getInt(0);
        if (fullyAutomaticShotsPerSecond != 0) {
            hasAuto = true;
        }

        boolean usesSelectiveFire = configuration.getObject(data.key + ".Selective_Fire.Trigger", Trigger.class) != null;
        if (usesSelectiveFire && !hasBurst && !hasAuto) {
            throw data.exception("Selective_Fire", "When using selective fire, make sure to set up 2 of: 'Burst' and/or 'Fully_Automatic_Shots_Per_Second' and/or 'Delay_Between_Shots'");
        }

        if (hasAuto && (isInvalidFullAuto(trigger.getMainhand())
                || isInvalidFullAuto(trigger.getOffhand())
                || isInvalidFullAuto(trigger.getDualWieldMainHand())
                || isInvalidFullAuto(trigger.getDualWieldOffHand()))) {
            throw data.exception("Trigger", "Tried to use full auto with invalid trigger.",
                    "When using full auto trigger supports following trigger types:",
                    "START_SNEAK, START_SPRINT, RIGHT_CLICK, START_SWIM, START_GLIDE, START_WALK, START_IN_MIDAIR and START_STAND.");
        }

        String defaultSelectiveFire = configuration.getString(data.key + ".Selective_Fire.Default");
        if (defaultSelectiveFire != null) {
            if (!defaultSelectiveFire.equalsIgnoreCase("SINGLE")
                    && !defaultSelectiveFire.equalsIgnoreCase("BURST")
                    && !defaultSelectiveFire.equalsIgnoreCase("AUTO")) {

                throw new SerializerOptionsException(data.serializer, "Selective Fire Default", Arrays.asList("SINGLE", "BURST", "AUTO"), defaultSelectiveFire, data.of("Selective_Fire.Default").getLocation());
            }
        }

        CustomDurability durability = data.of("Custom_Durability").serialize(CustomDurability.class);
        if (durability != null) configuration.set(data.key + ".Custom_Durability", durability);

        configuration.set(data.key + ".Reset_Fall_Distance", data.of("Reset_Fall_Distance").getBool(false));
    }

    private boolean isInvalidFullAuto(TriggerType triggerType) {
        if (triggerType == null) return false;

        return switch (triggerType) {
            case START_SNEAK, START_SPRINT, RIGHT_CLICK,
                    START_SWIM, START_GLIDE, START_WALK,
                    START_IN_MIDAIR, START_STAND -> false;
            default -> true;
        };
    }
}
