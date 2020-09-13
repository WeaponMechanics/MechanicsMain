package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmState;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmType;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class ReloadHandler implements IValidator {

    private WeaponHandler weaponHandler;

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

        int ammoLeft = getAmmoLeft(weaponStack);
        if (ammoLeft == -1) return false;

        Configuration config = getConfigurations();

        HandData handData = slot == EquipmentSlot.HAND ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();

        int magazineSize = config.getInt(weaponTitle + ".Reload.Magazine_Size");

        // Don't try to reload if already full
        if (ammoLeft >= magazineSize) return false;

        int reloadDuration = config.getInt(weaponTitle + ".Reload.Reload_Duration");
        int ammoPerReload = config.getInt(weaponTitle + ".Reload.Ammo_Per_Reload", -1);
        boolean unloadAmmoOnReload = config.getBool(weaponTitle + ".Reload.Unload_Ammo_On_Reload");

        FirearmAction firearmAction = config.getObject(weaponTitle + ".Firearm_Action", FirearmAction.class);

        if (firearmAction != null && firearmAction.hasShootState(weaponStack)) {

            // Call this again to make sure firearm actions are running
            weaponHandler.getShootHandler().doShootFirearmActions(entityWrapper, weaponTitle, weaponStack, handData);

            // ... and deny reload while has shoot firearm actions
            return false;
        }

        Ammo ammo = config.getObject(weaponTitle + ".Reload.Ammo", Ammo.class);
        if (ammo != null && ammo.getAmount(entityWrapper) <= 0) {

            // todo out of ammo effects

            return false;
        }

        boolean isPump = firearmAction != null && firearmAction.getFirearmType() == FirearmType.PUMP;

        ChainTask reloadTask = new ChainTask(reloadDuration) {

            @Override
            public void task() {

                int ammoLeft = getAmmoLeft(weaponStack);

                // Variable which decides how much ammo is added
                int ammoToAdd;

                if (ammoPerReload != -1) {

                    ammoToAdd = ammoPerReload;
                    if (ammoLeft + ammoToAdd > magazineSize) {
                        ammoToAdd = magazineSize - ammoLeft;
                    }

                } else {
                    ammoToAdd = magazineSize - ammoLeft;
                }

                if (ammo != null) {

                    int removedAmount = ammo.remove(entityWrapper, ammoToAdd);

                    // Just check if for some reason ammo disappeared from entity before reaching reload state
                    if (removedAmount <= 0) {

                        // todo out of ammo effects

                        // Remove next task as reload can't be finished
                        setNextTask(null);

                        return;
                    }

                    // Else simply set ammo to add value to removed amount
                    // Removed amount will be less than ammo to add amount IF there wasn't enough ammo in entity
                    ammoToAdd = removedAmount;

                }

                int finalAmmoSet = ammoLeft + ammoToAdd;

                if (entityWrapper instanceof IPlayerWrapper) {
                    // Set the tag silently
                    TagHelper.setIntegerTag(weaponStack, CustomTag.AMMO_LEFT, finalAmmoSet, (IPlayerWrapper) entityWrapper, true);
                } else {
                    TagHelper.setIntegerTag(weaponStack, CustomTag.AMMO_LEFT, finalAmmoSet);
                }

                if (firearmAction != null) {
                    if (isPump) {
                        firearmAction.openReloadState(weaponStack, entityWrapper);
                    } else {
                        firearmAction.closeReloadState(weaponStack, entityWrapper);
                    }

                } else {
                    finishReload(handData);

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
                if (unloadAmmoOnReload) {

                    if (ammo != null) {
                        ammo.give(entityWrapper, TagHelper.getIntegerTag(weaponStack, CustomTag.AMMO_LEFT));
                    }

                    // unload weapon and give ammo back to given entity
                    if (entityWrapper instanceof IPlayerWrapper) {
                        TagHelper.setIntegerTag(weaponStack, CustomTag.AMMO_LEFT, 0, (IPlayerWrapper) entityWrapper, true);
                    } else {
                        TagHelper.setIntegerTag(weaponStack, CustomTag.AMMO_LEFT, 0);
                    }
                }
            }
        };

        if (firearmAction == null) {

            // Doesn't run any chain in this case as there isn't next task
            reloadTask.startChain();

            return true;
        }

        ChainTask closeTask = new ChainTask(firearmAction.getCloseTime()) {

            @Override
            public void task() {
                firearmAction.readyState(weaponStack, entityWrapper);
                finishReload(handData);

                if (ammoPerReload != -1 && getAmmoLeft(weaponStack) < magazineSize) {
                    startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
                }
            }

            @Override
            public void setup() {
                handData.addReloadTask(getTaskId());
            }
        };

        ChainTask openTask = new ChainTask(firearmAction.getOpenTime()) {

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
            }
        };

        FirearmState state = firearmAction.getState(weaponStack);
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

    public void finishReload(HandData handData) {
        handData.stopReloadingTasks();
        // todo event + mechanics
    }

    /**
     * Returns ammo left in weapon.
     * If returned value is -1, then ammo is not used in this weapon stack
     *
     * @param weaponStack the weapon stack
     * @return -1 if infinity, otherwise current ammo amount
     */
    public int getAmmoLeft(ItemStack weaponStack) {
        Integer ammoLeft = TagHelper.getIntegerTag(weaponStack, CustomTag.AMMO_LEFT);
        if (ammoLeft == null) {
            // -1 means infinity
            return -1;
        }
        return ammoLeft;
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
                TagHelper.setIntegerTag(weaponStack, CustomTag.AMMO_LEFT, ammoToSet, (IPlayerWrapper) entityWrapper, true);
            } else {
                TagHelper.setIntegerTag(weaponStack, CustomTag.AMMO_LEFT, ammoToSet);
            }
        }
        return true;
    }

    @Override
    public String getKeyword() {
        return "Reload";
    }

    @Override
    public void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path) {

    }
}