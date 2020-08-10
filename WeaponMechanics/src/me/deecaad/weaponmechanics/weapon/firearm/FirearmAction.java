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

import static me.deecaad.core.MechanicsCore.debug;

public class FirearmAction implements Serializer<FirearmAction> {

    private FirearmType firearmType;
    private int firearmActionFrequency;

    private int openTime;
    // todo: Mechanics for open

    private int closeTime;
    // todo: Mechanics for close

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
        return getState(weaponStack).equals("READY");
    }

    public String getState(ItemStack weaponStack) {
        return TagHelper.getStringTag(weaponStack, CustomTag.FIREARM_ACTION_STATE);
    }

    public void openState(ItemStack weaponStack, IEntityWrapper entityWrapper) {
        changeState(weaponStack, entityWrapper, "OPEN");
    }

    public void reloadState(ItemStack weaponStack, IEntityWrapper entityWrapper) {
        changeState(weaponStack, entityWrapper, "RELOAD");
    }

    public void closeState(ItemStack weaponStack, IEntityWrapper entityWrapper) {
        changeState(weaponStack, entityWrapper, "CLOSE");
    }

    public void readyState(ItemStack weaponStack, IEntityWrapper entityWrapper) {
        changeState(weaponStack, entityWrapper, "READY");
    }

    private void changeState(ItemStack weaponStack, IEntityWrapper entityWrapper, String state) {
        if (entityWrapper instanceof IPlayerWrapper) {
            TagHelper.setStringTag(weaponStack, CustomTag.FIREARM_ACTION_STATE, state, (IPlayerWrapper) entityWrapper, true);
            return;
        }
        TagHelper.setStringTag(weaponStack, CustomTag.FIREARM_ACTION_STATE, state);
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