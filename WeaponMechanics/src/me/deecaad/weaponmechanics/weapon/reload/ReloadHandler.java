package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmState;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmType;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponPreReloadEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponReloadCompleteEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponReloadEvent;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import net.minecraft.server.v1_16_R3.PacketPlayOutSetSlot;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

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
     * @param dualWield whether or not this was dual wield
     * @return true if was able to start reloading
     */
    public boolean tryUse(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield) {
        Trigger trigger = getConfigurations().getObject(weaponTitle + ".Reload.Trigger", Trigger.class);
        if (trigger == null || !trigger.check(triggerType, slot, entityWrapper)) return false;

        return startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
    }

    /**
     * Starts reloading without checking for trigger
     *
     * @param entityWrapper the entity who used reload
     * @param weaponTitle the weapon title
     * @param weaponStack the weapon stack
     * @param slot the slot used on reload
     * @param dualWield whether or not this was dual wield
     * @return true if was able to start reloading
     */
    public boolean startReloadWithoutTrigger(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, boolean dualWield) {

        // Don't try to reload if either one of the hands is already reloading
        if (entityWrapper.getMainHandData().isReloading() || entityWrapper.getOffHandData().isReloading()) {
            return false;
        }

        WeaponPreReloadEvent preReloadEvent = new WeaponPreReloadEvent(weaponTitle, weaponStack, entityWrapper.getEntity());
        Bukkit.getPluginManager().callEvent(preReloadEvent);
        if (preReloadEvent.isCancelled()) return false;

        Configuration config = getConfigurations();

        int ammoLeft = getAmmoLeft(weaponStack);
        if (ammoLeft == -1) { // This shouldn't be -1, perhaps ammo was added for weapon in configs later in server...
            if (entityWrapper instanceof IPlayerWrapper) {
                TagHelper.setIntegerTag(weaponStack, CustomTag.AMMO_LEFT, 0, (IPlayerWrapper) entityWrapper, true);
            } else {
                TagHelper.setIntegerTag(weaponStack, CustomTag.AMMO_LEFT, 0);
            }
            ammoLeft = 0;
        }

        boolean mainhand = slot == EquipmentSlot.HAND;
        HandData handData = mainhand ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();
        int tempMagazineSize = config.getInt(weaponTitle + ".Reload.Magazine_Size");
        int reloadDuration = config.getInt(weaponTitle + ".Reload.Reload_Duration");

        FirearmAction firearmAction = config.getObject(weaponTitle + ".Firearm_Action", FirearmAction.class);
        FirearmState state = null;
        int firearmOpenTime = 0;
        int firearmCloseTime = 0;

        if (firearmAction != null) {
            if (firearmAction.hasShootState(weaponStack)) {
                // Call this again to make sure firearm actions are running
                weaponHandler.getShootHandler().doShootFirearmActions(entityWrapper, weaponTitle, weaponStack, handData, mainhand);

                // ... and deny reload while has shoot firearm actions
                return false;
            }
            firearmOpenTime = firearmAction.getOpenTime();
            firearmCloseTime = firearmAction.getCloseTime();
            state = firearmAction.getState(weaponStack);
        }

        boolean isPump = firearmAction != null && firearmAction.getFirearmType() == FirearmType.PUMP;
        int ammoPerReload = config.getInt(weaponTitle + ".Reload.Ammo_Per_Reload", -1);

        int tempAmmoToAdd;
        if (ammoPerReload != -1) {
            tempAmmoToAdd = ammoPerReload;
            if (ammoLeft + tempAmmoToAdd > tempMagazineSize) {
                tempAmmoToAdd = tempMagazineSize - ammoLeft;
            }

        } else {
            tempAmmoToAdd = tempMagazineSize - ammoLeft;
        }

        if (state != null) {
            // Modify reload times based on current firearm state
            // Since reload will continue from place it left on

            switch (state) {
                case RELOAD_OPEN:
                    if (isPump) {
                        reloadDuration = 0;
                    }
                    break;
                case RELOAD:
                    if (!isPump) {
                        firearmOpenTime = 0;
                    }
                    break;
                case RELOAD_CLOSE:
                    firearmOpenTime = 0;
                    reloadDuration = 0;
                    break;
                default:
                    break;
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

        // Don't try to reload if already full
        if (ammoLeft >= magazineSize) return false;

        Ammo ammo = config.getObject(weaponTitle + ".Reload.Ammo", Ammo.class);
        if (ammo != null && !ammo.hasAmmo(entityWrapper)) {
            ammo.useOutOfAmmo(new CastData(entityWrapper, weaponTitle, weaponStack));
            return false;
        }

        boolean unloadAmmoOnReload = config.getBool(weaponTitle + ".Reload.Unload_Ammo_On_Reload");

        // This is necessary for events to be used correctly
        handData.setReloadData(weaponTitle, weaponStack);

        ChainTask reloadTask = new ChainTask(reloadDuration) {

            private int unloadedAmount;

            @Override
            public void task() {

                int ammoLeft = getAmmoLeft(weaponStack);

                // Here creating this again since this may change if there isn't enough ammo...
                int ammoToAdd = finalAmmoToAdd + unloadedAmount;

                if (ammo != null) {

                    int removedAmount = ammo.remove(entityWrapper, ammoToAdd, magazineSize);

                    // Just check if for some reason ammo disappeared from entity before reaching reload state
                    if (removedAmount <= 0) {
                        ammo.useOutOfAmmo(new CastData(entityWrapper, weaponTitle, weaponStack));

                        // Remove next task as reload can't be finished
                        setNextTask(null);

                        handData.stopReloadingTasks();
                        return;
                    }

                    // Else simply set ammo to add value to removed amount
                    // Removed amount will be less than ammo to add amount IF there wasn't enough ammo in entity
                    ammoToAdd = removedAmount;

                    if (ammo.isItemMagazineAmmo()) {
                        int itemMagazineNum = CustomTag.HAS_ITEM_MAGAZINE.getInteger(weaponStack);
                        // 0 = true
                        // 1 = false

                        if (itemMagazineNum == 0) {
                            // give current mag for player (even if there is ammo left)
                            ammo.give(entityWrapper, ammoLeft, magazineSize);
                        } else {
                            // Set to value 0 to indicate that the mag is in again
                            if (entityWrapper instanceof IPlayerWrapper) {
                                CustomTag.HAS_ITEM_MAGAZINE.setInteger(weaponStack, 0, (IPlayerWrapper) entityWrapper, true);
                            } else {
                                CustomTag.HAS_ITEM_MAGAZINE.setInteger(weaponStack, 0);
                            }
                        }
                    }
                }

                int finalAmmoSet = ammoLeft + ammoToAdd;

                if (entityWrapper instanceof IPlayerWrapper) {
                    // Set the tag silently
                    CustomTag.AMMO_LEFT.setInteger(weaponStack, finalAmmoSet, (IPlayerWrapper) entityWrapper, true);
                } else {
                    CustomTag.AMMO_LEFT.setInteger(weaponStack, finalAmmoSet);
                }

                if (firearmAction != null) {
                    if (isPump) {
                        firearmAction.openReloadState(weaponStack, entityWrapper);
                    } else {
                        firearmAction.closeReloadState(weaponStack, entityWrapper);
                    }

                } else {
                    finishReload(entityWrapper, weaponTitle, weaponStack, handData);

                    if (ammoPerReload != -1 && getAmmoLeft(weaponStack) < magazineSize) {
                        startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
                    }
                }
            }

            @Override
            public void setup() {
                handData.addReloadTask(getTaskId());

                if (isPump) {
                    firearmAction.reloadState(weaponStack, entityWrapper);
                }
                int ammoLeft = CustomTag.AMMO_LEFT.getInteger(weaponStack);
                if (unloadAmmoOnReload) {

                    if (ammo != null) {

                        if (ammo.isItemMagazineAmmo()) {
                            int itemMagazineNum = CustomTag.HAS_ITEM_MAGAZINE.getInteger(weaponStack);
                            // 0 = true
                            // 1 = false

                            if (itemMagazineNum == 0) {
                                // Set to value 1 to indicate that the mag is now removed
                                if (entityWrapper instanceof IPlayerWrapper) {
                                    CustomTag.HAS_ITEM_MAGAZINE.setInteger(weaponStack, 1, (IPlayerWrapper) entityWrapper, true);
                                } else {
                                    CustomTag.HAS_ITEM_MAGAZINE.setInteger(weaponStack, 1);
                                }
                                // give mag back
                                ammo.give(entityWrapper, ammoLeft, magazineSize);
                            }
                        } else if (ammoLeft > 0) {
                            ammo.give(entityWrapper, ammoLeft, magazineSize);
                        }
                    }

                    if (ammoLeft > 0) {

                        unloadedAmount = ammoLeft;

                        // unload weapon and give ammo back to given entity
                        if (entityWrapper instanceof IPlayerWrapper) {
                            CustomTag.AMMO_LEFT.setInteger(weaponStack, 0, (IPlayerWrapper) entityWrapper, true);
                        } else {
                            CustomTag.AMMO_LEFT.setInteger(weaponStack, 0);
                        }
                    }
                }

                CastData castData = new CastData(entityWrapper, weaponTitle, weaponStack);
                // Set the extra data so SoundMechanic knows to save task id to hand's reload tasks
                castData.setData(ReloadSound.getDataKeyword(), mainhand ? ReloadSound.MAIN_HAND.getId() : ReloadSound.OFF_HAND.getId());
                Mechanics.use(weaponTitle + ".Reload.Start", castData);

                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);
            }
        };

        if (firearmAction == null) {

            // Doesn't run any chain in this case as there isn't next task
            reloadTask.startChain();

            return true;
        }

        ChainTask closeTask = new ChainTask(firearmCloseTime) {

            @Override
            public void task() {
                firearmAction.readyState(weaponStack, entityWrapper);
                finishReload(entityWrapper, weaponTitle, weaponStack, handData);

                if (ammoPerReload != -1 && getAmmoLeft(weaponStack) < magazineSize) {
                    startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
                }
            }

            @Override
            public void setup() {
                handData.addReloadTask(getTaskId());

                CastData castData = new CastData(entityWrapper, weaponTitle, weaponStack);
                // Set the extra data so SoundMechanic knows to save task id to hand's reload tasks
                castData.setData(ReloadSound.getDataKeyword(), mainhand ? ReloadSound.MAIN_HAND.getId() : ReloadSound.OFF_HAND.getId());
                firearmAction.useMechanics(castData, false);

                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);
            }
        };

        ChainTask openTask = new ChainTask(firearmOpenTime) {

            @Override
            public void task() {
                if (isPump) {
                    firearmAction.closeReloadState(weaponStack, entityWrapper);
                } else {
                    firearmAction.reloadState(weaponStack, entityWrapper);
                }
            }

            @Override
            public void setup() {
                handData.addReloadTask(getTaskId());

                if (!isPump) {
                    firearmAction.openReloadState(weaponStack, entityWrapper);
                }

                CastData castData = new CastData(entityWrapper, weaponTitle, weaponStack);
                // Set the extra data so SoundMechanic knows to save task id to hand's reload tasks
                castData.setData(ReloadSound.getDataKeyword(), mainhand ? ReloadSound.MAIN_HAND.getId() : ReloadSound.OFF_HAND.getId());
                firearmAction.useMechanics(castData, true);

                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);
            }
        };

        if (isPump) {
            // reload, open, close
            reloadTask.setNextTask(openTask);
            openTask.setNextTask(closeTask);

            switch (state) {
                case RELOAD_OPEN:
                    openTask.startChain();
                    break;
                case RELOAD_CLOSE:
                    closeTask.startChain();
                    break;
                default:
                    reloadTask.startChain();
                    break;
            }
        } else {
            // open, reload, close
            openTask.setNextTask(reloadTask);
            reloadTask.setNextTask(closeTask);

            switch (state) {
                case RELOAD:
                    reloadTask.startChain();
                    break;
                case RELOAD_CLOSE:
                    closeTask.startChain();
                    break;
                default:
                    openTask.startChain();
                    break;
            }
        }

        return true;
    }

    public void finishReload(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, HandData handData) {

        handData.finishReload();

        Mechanics.use(weaponTitle + ".Reload.Finish", new CastData(entityWrapper, weaponTitle, weaponStack));

        WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
        if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);
    }

    /**
     * Returns ammo left in weapon.
     * If returned value is -1, then ammo is not used in this weapon stack
     *
     * @param weaponStack the weapon stack
     * @return -1 if infinity, otherwise current ammo amount
     */
    public int getAmmoLeft(ItemStack weaponStack) {
        if (CustomTag.AMMO_LEFT.hasInteger(weaponStack)) {
            return CustomTag.AMMO_LEFT.getInteger(weaponStack);
        } else {
            return -1;
        }
    }

    /**
     * @return false if can't consume ammo (no enough ammo left)
     */
    public boolean consumeAmmo(IEntityWrapper entityWrapper, ItemStack weaponStack, int amount) {
        int ammoLeft = getAmmoLeft(weaponStack);

        // -1 means infinite ammo
        if (ammoLeft != -1) {
            int ammoToSet = ammoLeft - amount;

            if (ammoToSet <= -1) {
                // Can't consume more ammo
                return false;
            }

            if (entityWrapper instanceof IPlayerWrapper) {
                // Set the tag silently
                CustomTag.AMMO_LEFT.setInteger(weaponStack, ammoToSet, (IPlayerWrapper) entityWrapper, true);
            } else {
                CustomTag.AMMO_LEFT.setInteger(weaponStack, ammoToSet);
            }
        }
        return true;
    }

    /**
     * @return the amount in ticks required to finish reload, this takes current firearm action to account
     */
    public int getReloadFinishTime(FirearmState state, boolean isPump, int reloadTime, int open, int close) {
        if (state == null) return reloadTime;
        switch (state) {
            case RELOAD_OPEN:
                return isPump ? open + close : open + reloadTime + close;
            case RELOAD:
                return isPump ? reloadTime + open + close : reloadTime + close;
            case RELOAD_CLOSE:
                return close;
            default:
                return open + reloadTime + close;
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
        debug.validate(magazineSize > 0, StringUtil.foundInvalid("magazine size"),
                StringUtil.foundAt(file, path + ".Magazine_Size", magazineSize),
                "It either didn't exist or it was missing.");

        int reloadDuration = configurationSection.getInt(path + ".Reload_Duration");
        debug.validate(reloadDuration > 0, StringUtil.foundInvalid("reload duration"),
                StringUtil.foundAt(file, path + ".Reload_Duration", reloadDuration),
                "It either didn't exist or it was missing.");
    }
}