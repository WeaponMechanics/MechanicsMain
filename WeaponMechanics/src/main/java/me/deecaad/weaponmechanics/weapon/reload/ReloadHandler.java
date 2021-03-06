package me.deecaad.weaponmechanics.weapon.reload;

import co.aikar.timings.lib.MCTiming;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmState;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmType;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.reload.ammo.AmmoTypes;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponPreReloadEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponReloadEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.*;

public class ReloadHandler implements IValidator {

    private WeaponHandler weaponHandler;

    public ReloadHandler() { }

    public ReloadHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    /**
     * Tries to use reload
     *
     * @param entityWrapper the entity who used trigger
     * @param weaponTitle the weapon title
     * @param weaponStack the weapon stack
     * @param slot the slot used on trigger
     * @param triggerType the trigger type trying to activate reload
     * @param dualWield whether this was dual wield
     * @return true if was able to start reloading
     */
    public boolean tryUse(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield) {
        Trigger trigger = getConfigurations().getObject(weaponTitle + ".Reload.Trigger", Trigger.class);
        if (trigger == null || !trigger.check(triggerType, slot, entityWrapper)) return false;

        return startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield, false);
    }

    /**
     * Starts reloading without checking for trigger.
     * Used for example when trying to shoot without ammo.
     *
     * @param entityWrapper the entity who used reload
     * @param weaponTitle the weapon title
     * @param weaponStack the weapon stack
     * @param slot the slot used on reload
     * @param dualWield whether this was dual wield
     * @param isReloadLoop whether this is reloading loop
     * @return true if was able to start reloading
     */
    public boolean startReloadWithoutTrigger(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, boolean dualWield, boolean isReloadLoop) {

        // This method is called from many places in reload handler and shoot handler as well
        // so that's why even startReloadWithoutTriggerAndWithoutTiming() is a separated method

        MCTiming reloadHandlerTiming = WeaponMechanics.timing("Reload Handler").startTiming();
        boolean result = startReloadWithoutTriggerAndWithoutTiming(entityWrapper, weaponTitle, weaponStack, slot, dualWield, isReloadLoop);
        reloadHandlerTiming.stopTiming();

        return result;
    }

    private boolean startReloadWithoutTriggerAndWithoutTiming(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, boolean dualWield, boolean isReloadLoop) {

        // Don't try to reload if either one of the hands is already reloading / full autoing
        HandData mainHandData = entityWrapper.getMainHandData();
        HandData offHandData = entityWrapper.getOffHandData();
        if (mainHandData.isReloading() || mainHandData.isUsingFullAuto() || mainHandData.isUsingBurst()
                || offHandData.isReloading() || offHandData.isUsingFullAuto() || offHandData.isUsingBurst()) {
            return false;
        }

        WeaponPreReloadEvent preReloadEvent = new WeaponPreReloadEvent(weaponTitle, weaponStack, entityWrapper.getEntity());
        Bukkit.getPluginManager().callEvent(preReloadEvent);
        if (preReloadEvent.isCancelled()) return false;

        Configuration config = getConfigurations();

        int reloadDuration = config.getInt(weaponTitle + ".Reload.Reload_Duration");
        int tempMagazineSize = config.getInt(weaponTitle + ".Reload.Magazine_Size");
        if (tempMagazineSize <= 0 || reloadDuration <= 0) {
            // This ensures that non intended reloads doesn't occur from ShootHandler for example
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

        int ammoLeft = getAmmoLeft(weaponStack, weaponTitle);
        if (ammoLeft == -1) { // This shouldn't be -1 at this point since reload should be used, perhaps ammo was added for weapon in configs later in server...
            CustomTag.AMMO_LEFT.setInteger(weaponStack, 0);
            ammoLeft = 0;
        }

        // On reload force zoom out
        entityWrapper.getMainHandData().ifZoomingForceZoomOut();
        entityWrapper.getOffHandData().ifZoomingForceZoomOut();

        boolean mainhand = slot == EquipmentSlot.HAND;
        HandData handData = mainhand ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();

        int ammoPerReload = config.getInt(weaponTitle + ".Reload.Ammo_Per_Reload", -1);

        // Check how much ammo should be added during this reload iteration
        int tempAmmoToAdd;
        if (ammoPerReload != -1) {
            tempAmmoToAdd = ammoPerReload;
            if (ammoLeft + tempAmmoToAdd > tempMagazineSize) {
                tempAmmoToAdd = tempMagazineSize - ammoLeft;
            }

        } else {
            tempAmmoToAdd = tempMagazineSize - ammoLeft;
        }

        WeaponInfoDisplay weaponInfoDisplay = shooter.getType() != EntityType.PLAYER ? null : getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
        PlayerWrapper playerWrapper = weaponInfoDisplay == null ? null : (PlayerWrapper) entityWrapper;

        FirearmAction firearmAction = config.getObject(weaponTitle + ".Firearm_Action", FirearmAction.class);
        FirearmState state = null;
        boolean isRevolver = false;
        boolean isPump = false;
        int firearmOpenTime = 0;
        int firearmCloseTime = 0;

        if (firearmAction != null) {
            state =  firearmAction.getState(weaponStack);
            isRevolver = firearmAction.getFirearmType() == FirearmType.REVOLVER;
            isPump = firearmAction.getFirearmType() == FirearmType.PUMP;

            // We want to do firearm actions IF
            // Is not reload loop
            // AND
            // Is revolver or ammo is 0
            if (!isReloadLoop && (isRevolver || ammoLeft <= 0)) {

                firearmOpenTime = firearmAction.getOpenTime();
                firearmCloseTime = firearmAction.getCloseTime();

                switch (state) {
                    case OPEN:
                        if (isPump) reloadDuration = 0;
                        break;
                    case CLOSE:
                        firearmOpenTime = 0;
                        reloadDuration = 0;
                        break;
                    default:
                        break;
                }
            }
        }

        WeaponReloadEvent reloadEvent = new WeaponReloadEvent(weaponTitle, weaponStack, entityWrapper.getEntity(),
                reloadDuration, tempAmmoToAdd, tempMagazineSize, firearmOpenTime, firearmCloseTime);
        Bukkit.getPluginManager().callEvent(reloadEvent);

        reloadDuration = reloadEvent.getReloadTime();
        tempAmmoToAdd = reloadEvent.getReloadAmount();
        tempMagazineSize = reloadEvent.getMagazineSize();
        firearmOpenTime = reloadEvent.getFirearmOpenTime();
        firearmCloseTime = reloadEvent.getFirearmCloseTime();

        final int finalAmmoToAdd = tempAmmoToAdd;
        final int magazineSize = tempMagazineSize;

        if (ammoLeft >= magazineSize || reloadDuration == 0) {
            // Don't try to reload if already full
            // Or reload duration is 0 because of firearm states

            if (state != null && state != FirearmState.READY) {
                // If state isn't already ready, complete it
                // This is most likely to happen when using Ammo_Per_Reload option -> isReloadLoop = true here

                if (!isPump) {
                    // Ensure that the state is set to CLOSE if the firearm actions isn't pump
                    // Since with pump we want to first OPEN and then CLOSE
                    if (state != FirearmState.CLOSE) firearmAction.changeState(weaponStack, FirearmState.CLOSE);
                }

                // Simply CLOSE weapon or OPEN CLOSE if pump
                weaponHandler.getShootHandler().doShootFirearmActions(entityWrapper, weaponTitle, weaponStack, handData, slot);

                // Here true because firearm actions started
                return true;
            }

            return false;
        }

        AmmoTypes ammoTypes = playerWrapper != null ? config.getObject(weaponTitle + ".Reload.Ammo.Ammo_Types", AmmoTypes.class) : null;
        if (ammoTypes != null && !ammoTypes.hasAmmo(weaponTitle, weaponStack, playerWrapper)) {
            Mechanics outOfAmmoMechanics = getConfigurations().getObject(weaponTitle + ".Reload.Ammo.Out_Of_Ammo", Mechanics.class);
            if (outOfAmmoMechanics != null) outOfAmmoMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));
            return false;
        }

        boolean unloadAmmoOnReload = config.getBool(weaponTitle + ".Reload.Unload_Ammo_On_Reload");

        // This is necessary for events to be used correctly
        handData.setReloadData(weaponTitle, weaponStack);

        ChainTask reloadTask = new ChainTask(reloadDuration) {

            private int unloadedAmount;

            @Override
            public void task() {
                ItemStack taskReference = mainhand ? entityWrapper.getEntity().getEquipment().getItemInMainHand() : entityWrapper.getEntity().getEquipment().getItemInOffHand();
                if (taskReference == weaponStack) {
                    taskReference = weaponStack;
                } else {
                    handData.setReloadData(weaponTitle, taskReference);
                }
                if (!taskReference.hasItemMeta()) {
                    handData.stopReloadingTasks();
                    return;
                }

                int ammoLeft = getAmmoLeft(taskReference, weaponTitle);

                // Here creating this again since this may change if there isn't enough ammo...
                int ammoToAdd = finalAmmoToAdd + unloadedAmount;

                if (ammoTypes != null) {

                    int removedAmount = ammoTypes.removeAmmo(taskReference, playerWrapper, ammoToAdd, magazineSize);

                    // Just check if for some reason ammo disappeared from entity before reaching reload "complete" state
                    if (removedAmount <= 0) {
                        Mechanics outOfAmmoMechanics = getConfigurations().getObject(weaponTitle + ".Reload.Ammo.Out_Of_Ammo", Mechanics.class);
                        if (outOfAmmoMechanics != null) outOfAmmoMechanics.use(new CastData(entityWrapper, weaponTitle, taskReference));

                        // Remove next task as reload can't be finished
                        setNextTask(null);

                        handData.stopReloadingTasks();
                        return;
                    }

                    // Else simply set ammo to add value to removed amount
                    // Removed amount will be less than ammo to add amount IF player didn't have that much ammo
                    ammoToAdd = removedAmount;
                }

                int finalAmmoSet = ammoLeft + ammoToAdd;

                handleWeaponStackAmount(entityWrapper, taskReference);

                CustomTag.AMMO_LEFT.setInteger(taskReference, finalAmmoSet);

                finishReload(entityWrapper, weaponTitle, taskReference, handData, slot);

                if (ammoPerReload != -1) {
                    // Start the loop
                    startReloadWithoutTrigger(entityWrapper, weaponTitle, taskReference, slot, dualWield, true);
                }
            }

            @Override
            public void setup() {
                handData.addReloadTask(getTaskId());

                int ammoLeft = CustomTag.AMMO_LEFT.getInteger(weaponStack);

                if (unloadAmmoOnReload && ammoLeft > 0) {
                    // unload weapon and give ammo back to given entity

                    if (ammoTypes != null) ammoTypes.giveAmmo(weaponStack, playerWrapper, ammoLeft, magazineSize);
                    unloadedAmount = ammoLeft;

                    handleWeaponStackAmount(entityWrapper, weaponStack);

                    CustomTag.AMMO_LEFT.setInteger(weaponStack, 0);
                }

                CastData castData = new CastData(entityWrapper, weaponTitle, weaponStack);
                // Set the extra data so SoundMechanic knows to save task id to hand's reload tasks
                castData.setData(ReloadSound.getDataKeyword(), mainhand ? ReloadSound.MAIN_HAND.getId() : ReloadSound.OFF_HAND.getId());
                Mechanics reloadStartMechanics = config.getObject(weaponTitle + ".Reload.Start_Mechanics", Mechanics.class);
                if (reloadStartMechanics != null) reloadStartMechanics.use(castData);

                if (weaponInfoDisplay != null) weaponInfoDisplay.send(playerWrapper, slot);

                weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);
            }
        };

        if (getConfigurations().getBool(weaponTitle + ".Info.Show_Cooldown.Reload_Time") && entityWrapper.getEntity().getType() == EntityType.PLAYER) {
            CompatibilityAPI.getEntityCompatibility().setCooldown((Player) entityWrapper.getEntity(), weaponStack.getType(), reloadEvent.getReloadCompleteTime());
        }

        // If loop OR firearm actions aren't used
        // OR ammo left is above 0 and revolver isn't used (when using revolver firearm actions should always occur)
        if (isReloadLoop || state == null || (ammoLeft > 0 && !isRevolver)) {
            reloadTask.startChain();
            return true;
        }

        ChainTask closeTask = getCloseTask(firearmCloseTime, firearmAction, weaponStack, handData, entityWrapper, weaponTitle, mainhand, slot);

        if (state == FirearmState.CLOSE) {
            closeTask.startChain();
            return true;
        }

        ChainTask openTask = getOpenTask(firearmOpenTime, firearmAction, weaponStack, handData, entityWrapper, weaponTitle, mainhand, slot);

        if (isPump) {
            firearmAction.changeState(weaponStack, FirearmState.OPEN);
            if (ammoPerReload != -1) {
                reloadTask.startChain();
            } else {
                reloadTask.setNextTask(openTask).setNextTask(closeTask);
                reloadTask.startChain();
            }
        } else {
            if (ammoPerReload != -1) {
                openTask.setNextTask(reloadTask);
                openTask.startChain();
            } else {
                openTask.setNextTask(reloadTask).setNextTask(closeTask);
                openTask.startChain();
            }
        }

        return true;
    }

    private ChainTask getOpenTask(int firearmOpenTime, FirearmAction firearmAction, ItemStack weaponStack, HandData handData,
                                  EntityWrapper entityWrapper, String weaponTitle, boolean mainhand, EquipmentSlot slot) {
        return new ChainTask(firearmOpenTime) {

            @Override
            public void task() {
                ItemStack taskReference = mainhand ? entityWrapper.getEntity().getEquipment().getItemInMainHand() : entityWrapper.getEntity().getEquipment().getItemInOffHand();
                if (taskReference != weaponStack) {
                    handData.setReloadData(weaponTitle, taskReference);
                }
                if (!taskReference.hasItemMeta()) {
                    handData.stopReloadingTasks();
                }
            }

            @Override
            public void setup() {
                handData.addReloadTask(getTaskId());

                firearmAction.changeState(weaponStack, FirearmState.OPEN);

                CastData castData = new CastData(entityWrapper, weaponTitle, weaponStack);
                // Set the extra data so SoundMechanic knows to save task id to hand's reload tasks
                castData.setData(ReloadSound.getDataKeyword(), mainhand ? ReloadSound.MAIN_HAND.getId() : ReloadSound.OFF_HAND.getId());
                firearmAction.useMechanics(castData, true);

                if (entityWrapper instanceof PlayerWrapper) {
                    WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                    if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, slot);
                }

                weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);
            }
        };
    }

    private ChainTask getCloseTask(int firearmCloseTime, FirearmAction firearmAction, ItemStack weaponStack, HandData handData, EntityWrapper entityWrapper,
                                   String weaponTitle, boolean mainhand, EquipmentSlot slot) {
        return new ChainTask(firearmCloseTime) {

            @Override
            public void task() {
                ItemStack taskReference = mainhand ? entityWrapper.getEntity().getEquipment().getItemInMainHand() : entityWrapper.getEntity().getEquipment().getItemInOffHand();
                if (taskReference == weaponStack) {
                    taskReference = weaponStack;
                } else {
                    handData.setReloadData(weaponTitle, taskReference);
                }
                if (!taskReference.hasItemMeta()) {
                    handData.stopReloadingTasks();
                    return;
                }

                firearmAction.changeState(taskReference, FirearmState.READY);
                finishReload(entityWrapper, weaponTitle, taskReference, handData, slot);
            }

            @Override
            public void setup() {
                handData.addReloadTask(getTaskId());

                firearmAction.changeState(weaponStack, FirearmState.CLOSE);

                CastData castData = new CastData(entityWrapper, weaponTitle, weaponStack);
                // Set the extra data so SoundMechanic knows to save task id to hand's reload tasks
                castData.setData(ReloadSound.getDataKeyword(), mainhand ? ReloadSound.MAIN_HAND.getId() : ReloadSound.OFF_HAND.getId());
                firearmAction.useMechanics(castData, false);

                if (entityWrapper instanceof PlayerWrapper) {
                    WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                    if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, slot);
                }

                weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);
            }
        };
    }

    public void finishReload(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, HandData handData, EquipmentSlot slot) {
        if (!weaponStack.hasItemMeta()) {
            handData.stopReloadingTasks();
            return;
        }

        handData.finishReload();

        Mechanics reloadFinishMechanics = getConfigurations().getObject(weaponTitle + ".Reload.Finish_Mechanics", Mechanics.class);
        if (reloadFinishMechanics != null) reloadFinishMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

        if (entityWrapper instanceof PlayerWrapper) {
            WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
            if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, slot);
        }

        weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);
    }

    /**
     * Returns ammo left in weapon.
     * If returned value is -1, then ammo is not used in this weapon stack
     *
     * @param weaponStack the weapon stack
     * @param weaponTitle the weapon title
     * @return -1 if infinity, otherwise current ammo amount
     */
    public int getAmmoLeft(ItemStack weaponStack, String weaponTitle) {
        if (CustomTag.AMMO_LEFT.hasInteger(weaponStack) && getConfigurations().getInt(weaponTitle + ".Reload.Magazine_Size") != 0) {
            return CustomTag.AMMO_LEFT.getInteger(weaponStack);
        } else {
            return -1;
        }
    }

    /**
     * @return false if can't consume ammo (no enough ammo left)
     */
    public boolean consumeAmmo(ItemStack weaponStack, String weaponTitle, int amount) {
        int ammoLeft = getAmmoLeft(weaponStack, weaponTitle);

        // -1 means infinite ammo
        if (ammoLeft != -1) {
            int ammoToSet = ammoLeft - amount;

            if (ammoLeft == 0 || ammoToSet <= -1) {
                // Can't consume more ammo

                return false;
            }

            CustomTag.AMMO_LEFT.setInteger(weaponStack, ammoToSet);
        }
        return true;
    }

    /**
     * Drop item stacks to entity location if the
     * weapon stack amount is more than 1
     *
     * @param entityWrapper the entity
     * @param weaponStack the item stack to give
     */
    public void handleWeaponStackAmount(EntityWrapper entityWrapper, ItemStack weaponStack) {

        int weaponStackAmount = weaponStack.getAmount();
        if (weaponStackAmount > 1) {
            // If the amount is above 1, we want to split the stack
            weaponStack.setAmount(weaponStackAmount - 1);

            // Drop the clone of weapon stack item with amount - 1
            LivingEntity entity = entityWrapper.getEntity();
            entity.getWorld().dropItemNaturally(entity.getLocation().add(0, 1, 0), weaponStack.clone());

            weaponStack.setAmount(1);
        }
    }

    @Override
    public String getKeyword() {
        return "Reload";
    }

    @Override
    public void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path) {
        Trigger trigger = configuration.getObject(path + ".Trigger", Trigger.class);
        if (trigger == null) {
            debug.log(LogLevel.ERROR, "Tried to use shoot without defining trigger for it.",
                    StringUtil.foundAt(file, path + ".Trigger"));
        }

        int magazineSize = configurationSection.getInt(path + ".Magazine_Size");
        debug.validate(magazineSize > 0, "Magazine_Size must be positive",
                SerializerException.forValue(magazineSize),
                StringUtil.foundAt(file, path + ".Magazine_Size"));

        int reloadDuration = configurationSection.getInt(path + ".Reload_Duration");
        debug.validate(reloadDuration > 0, "Reload_Duration must be positive",
                SerializerException.forValue(reloadDuration),
                StringUtil.foundAt(file, path + ".Reload_Duration"));

        int ammoPerReload = configurationSection.getInt(path + ".Ammo_Per_Reload", -99);
        if (ammoPerReload != -99) {
            debug.validate(ammoPerReload > 0, "Ammo_Per_Reload must be positive",
                    SerializerException.forValue(ammoPerReload),
                    StringUtil.foundAt(file, path + ".Ammo_Per_Reload"));
        }
        boolean unloadAmmoOnReload = configurationSection.getBoolean(path + ".Unload_Ammo_On_Reload");
        if (unloadAmmoOnReload && ammoPerReload != -99) {
            // Using ammo per reload and unload ammo on reload at same time is considered as error
            debug.error("Cannot use Ammo_Per_Reload and Unload_Ammo_On_Reload at the same time",
                    StringUtil.foundAt(file, path + ".Unload_Ammo_On_Reload"));
        }
    }
}