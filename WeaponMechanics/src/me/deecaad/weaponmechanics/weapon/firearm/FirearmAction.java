package me.deecaad.weaponmechanics.weapon.firearm;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static me.deecaad.core.MechanicsCore.debug;

public class FirearmAction implements Serializer<FirearmAction> {

    private FirearmType firearmType;
    private int firearmActionFrequency;
    private int openTime;
    private int closeTime;

    /**
     * Empty constructor to be used as serializer
     */
    public FirearmAction() { }

    public FirearmAction(FirearmType firearmType, int firearmActionFrequency, int openTime, int closeTime) {
        this.firearmType = firearmType;
        this.firearmActionFrequency = firearmActionFrequency;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    /**
     * @return whether the state is READY
     */
    public boolean hasReadyFirearmActions(ItemStack weaponStack) {
        return getState(weaponStack) == FirearmState.READY;
    }

    public boolean hasReloadState(ItemStack weaponStack) {
        FirearmState state = getState(weaponStack);
        return state == FirearmState.RELOAD_OPEN || state == FirearmState.RELOAD || state == FirearmState.RELOAD_CLOSE;
    }

    public boolean hasShootState(ItemStack weaponStack) {
        FirearmState state = getState(weaponStack);
        return state == FirearmState.SHOOT_OPEN || state == FirearmState.SHOOT_CLOSE;
    }

    public FirearmState getState(ItemStack weaponStack) {
        Integer state = TagHelper.getIntegerTag(weaponStack, CustomTag.FIREARM_ACTION_STATE);

        if (state == null) return FirearmState.READY;
        switch (state) {
            case 1:
                return FirearmState.RELOAD_OPEN;
            case 2:
                return FirearmState.RELOAD;
            case 3:
                return FirearmState.RELOAD_CLOSE;
            case 4:
                return FirearmState.SHOOT_OPEN;
            case 5:
                return FirearmState.SHOOT_CLOSE;
            default:
                return FirearmState.READY;
        }
    }

    public void readyState(ItemStack weaponStack, IEntityWrapper entityWrapper) {
        changeState(weaponStack, entityWrapper, FirearmState.READY);
    }

    public void openReloadState(ItemStack weaponStack, IEntityWrapper entityWrapper) {
        changeState(weaponStack, entityWrapper, FirearmState.RELOAD_OPEN);
    }

    public void reloadState(ItemStack weaponStack, IEntityWrapper entityWrapper) {
        changeState(weaponStack, entityWrapper, FirearmState.RELOAD);
    }

    public void closeReloadState(ItemStack weaponStack, IEntityWrapper entityWrapper) {
        changeState(weaponStack, entityWrapper, FirearmState.RELOAD_CLOSE);
    }

    public void openShootState(ItemStack weaponStack, IEntityWrapper entityWrapper) {
        changeState(weaponStack, entityWrapper, FirearmState.SHOOT_OPEN);
    }

    public void closeShootState(ItemStack weaponStack, IEntityWrapper entityWrapper) {
        changeState(weaponStack, entityWrapper, FirearmState.SHOOT_CLOSE);
    }

    private void changeState(ItemStack weaponStack, IEntityWrapper entityWrapper, FirearmState state) {
        if (entityWrapper instanceof IPlayerWrapper) {
            TagHelper.setIntegerTag(weaponStack, CustomTag.FIREARM_ACTION_STATE, state.getId(), (IPlayerWrapper) entityWrapper, true);
            return;
        }
        TagHelper.setIntegerTag(weaponStack, CustomTag.FIREARM_ACTION_STATE, state.getId());
    }

    public FirearmType getFirearmType() {
        return firearmType;
    }

    public int getOpenTime() {
        return openTime;
    }

    public int getCloseTime() {
        return closeTime;
    }

    public int getFirearmActionFrequency() {
        return firearmActionFrequency;
    }

    @Override
    public String getKeyword() {
        return "Firearm_Action";
    }

    @Override
    public Set<String> allowOtherSerializers() {
        return new HashSet<>(Arrays.asList("Type", "Firearm_Action_Frequency", "Time"));
    }

    @Override
    public FirearmAction serialize(File file, ConfigurationSection configurationSection, String path) {
        String stringType = configurationSection.getString(path + ".Type");
        if (stringType == null) {
            return null;
        }
        FirearmType type;
        try {
            type = FirearmType.valueOf(stringType.toUpperCase());
        } catch (IllegalArgumentException e) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid firearm type in configurations!",
                    "Located at file " + file + " in " + path + ".Type (" + stringType.toUpperCase() + ") in configurations");
            return null;
        }

        // Default to 1 in order to make it easier to use
        int firearmActionFrequency = configurationSection.getInt(path + ".Firearm_Action_Frequency", 1);
        if (firearmActionFrequency < 1) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid firearm action frequency in configurations!",
                    "Make sure the value is 1 or more, not it was " + firearmActionFrequency + ".",
                    "Located at file " + file + " in " + path + ".Firearm_Action_Frequency in configurations");
            return null;
        }

        int openTime = configurationSection.getInt(path + ".Open.Time");
        if (openTime < 1) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid firearm action open time in configurations!",
                    "Make sure the value is 1 or more, not it was " + openTime + ".",
                    "Located at file " + file + " in " + path + ".Open.Time in configurations");
            return null;
        }

        int closeTime = configurationSection.getInt(path + ".Close.Time");
        if (closeTime < 1) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid firearm action close time in configurations!",
                    "Make sure the value is 1 or more, not it was " + closeTime + ".",
                    "Located at file " + file + " in " + path + ".Open.Time in configurations");
            return null;
        }

        return new FirearmAction(type, firearmActionFrequency, openTime, closeTime);
    }
}