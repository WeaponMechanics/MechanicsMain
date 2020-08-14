package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmType;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
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
        int bulletsPerReload = config.getInt(weaponTitle + ".Bullets_Per_Reload", -1);

        FirearmAction firearmAction = config.getObject(weaponTitle + ".Firearm_Action", FirearmAction.class);

        if (firearmAction != null) {

            if (!firearmAction.hasReadyFirearmActions(weaponStack)) {
                // Meaning that shoot firearm actions are most likely in progress
                // -> Deny reload
                return false;
            }


        }

        BukkitRunnable closeTask = new BukkitRunnable() {
            @Override
            public void run() {
                firearmAction.readyState(weaponStack, entityWrapper);
                finishReload(handData);
                if (bulletsPerReload != -1 && getAmmoLeft(weaponStack) < magazineSize) {
                    startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
                }
            }
        };

        BukkitRunnable openTask = null;

        boolean isPump = firearmAction.getFirearmType() == FirearmType.PUMP;
        if (isPump) {
            openTask = new BukkitRunnable() {
                @Override
                public void run() {
                    firearmAction.closeState(weaponStack, entityWrapper);
                    handData.addReloadTask(closeTask.runTaskLater(WeaponMechanics.getPlugin(), firearmAction.getCloseTime()).getTaskId());
                }
            };
        }

        BukkitRunnable finalOpenTask = openTask;
        BukkitRunnable reloadTask = new BukkitRunnable() {
            @Override
            public void run() {

                // Variable which decides the final ammo amount set to weapon after reload
                int ammoToSet;

                if (bulletsPerReload != -1) {
                    ammoToSet = ammoLeft + bulletsPerReload;
                    if (ammoToSet > magazineSize) {
                        ammoToSet = magazineSize;
                    }
                } else {
                    ammoToSet = magazineSize;
                }


                if (entityWrapper instanceof IPlayerWrapper) {
                    // Set the tag silently
                    TagHelper.setIntegerTag(weaponStack, CustomTag.AMMO_LEFT, ammoToSet, (IPlayerWrapper) entityWrapper, true);
                } else {
                    TagHelper.setIntegerTag(weaponStack, CustomTag.AMMO_LEFT, ammoToSet);
                }

                if (firearmAction != null) {
                    if (isPump) {
                        firearmAction.openState(weaponStack, entityWrapper);
                        handData.addReloadTask(finalOpenTask.runTaskLater(WeaponMechanics.getPlugin(), firearmAction.getOpenTime()).getTaskId());
                    } else {
                        firearmAction.closeState(weaponStack, entityWrapper);
                        handData.addReloadTask(closeTask.runTaskLater(WeaponMechanics.getPlugin(), firearmAction.getCloseTime()).getTaskId());
                    }

                } else {
                    finishReload(handData);

                    if (bulletsPerReload != -1 && getAmmoLeft(weaponStack) < magazineSize) {
                        startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
                    }
                }
            }
        };

        if (!isPump) {
            openTask = new BukkitRunnable() {
                @Override
                public void run() {
                    firearmAction.reloadState(weaponStack, entityWrapper);
                    handData.addReloadTask(reloadTask.runTaskLater(WeaponMechanics.getPlugin(), reloadDuration).getTaskId());
                }
            };

            firearmAction.openState(weaponStack, entityWrapper);
            handData.addReloadTask(reloadTask.runTaskLater(WeaponMechanics.getPlugin(), firearmAction.getOpenTime()).getTaskId());

        } else {
            firearmAction.reloadState(weaponStack, entityWrapper);
            handData.addReloadTask(reloadTask.runTaskLater(WeaponMechanics.getPlugin(), reloadDuration).getTaskId());
        }



        return true;
    }

    private void finishReload(HandData handData) {
        handData.stopReloadingTasks();
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
    public boolean consumeAmmo(IEntityWrapper entityWrapper, ItemStack weaponStack, EquipmentSlot slot, int amount) {
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