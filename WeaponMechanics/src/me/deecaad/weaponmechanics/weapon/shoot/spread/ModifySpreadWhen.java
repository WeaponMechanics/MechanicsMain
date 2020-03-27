package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

public class ModifySpreadWhen implements Serializer<ModifySpreadWhen> {

    private ModifierHolder always;
    private ModifierHolder zooming;
    private ModifierHolder sneaking;
    private ModifierHolder standing;
    private ModifierHolder walking;
    private ModifierHolder swimming;
    private ModifierHolder inMidair;
    private ModifierHolder gliding;

    /**
     * Empty constructor to be used as serializer
     */
    public ModifySpreadWhen() { }

    public ModifySpreadWhen(ModifierHolder always, ModifierHolder zooming, ModifierHolder sneaking, ModifierHolder standing, ModifierHolder walking, ModifierHolder swimming, ModifierHolder inMidair, ModifierHolder gliding) {
        this.always = always;
        this.zooming = zooming;
        this.sneaking = sneaking;
        this.standing = standing;
        this.walking = walking;
        this.swimming = swimming;
        this.inMidair = inMidair;
        this.gliding = gliding;
    }

    /**
     * Applies all changes from this spread modifier to given spread
     *
     * @param entityWrapper the entity wrapper used to check circumstances
     * @param tempSpread the spread
     * @return the spread with updated values
     */
    public double applyChanges(IEntityWrapper entityWrapper, double tempSpread) {
        if (always != null) {
            tempSpread = always.applyTo(tempSpread);
        }
        if (entityWrapper.isZooming() && zooming != null) {
            tempSpread = zooming.applyTo(tempSpread);
        }
        if (entityWrapper.isSneaking() && sneaking != null) {
            tempSpread = sneaking.applyTo(tempSpread);
        }
        if (entityWrapper.isStanding() && standing != null) {
            tempSpread = standing.applyTo(tempSpread);
        }
        if (entityWrapper.isWalking() && walking != null) {
            tempSpread = walking.applyTo(tempSpread);
        }
        if (entityWrapper.isSwimming() && swimming != null) {
            tempSpread = swimming.applyTo(tempSpread);
        }
        if (entityWrapper.isInMidair() && inMidair != null) {
            tempSpread = inMidair.applyTo(tempSpread);
        }
        if (entityWrapper.isGliding() && gliding != null) {
            tempSpread = gliding.applyTo(tempSpread);
        }

        return tempSpread;
    }

    @Override
    public String getKeyword() {
        return "Modify_Spread_When";
    }

    @Override
    public ModifySpreadWhen serialize(File file, ConfigurationSection configurationSection, String path) {
        ModifierHolder always = getModifierHandler(file, configurationSection, path + ".Always");
        ModifierHolder zooming = getModifierHandler(file, configurationSection, path + ".Zooming");
        ModifierHolder sneaking = getModifierHandler(file, configurationSection, path + ".Sneaking");
        ModifierHolder standing = getModifierHandler(file, configurationSection, path + ".Standing");
        ModifierHolder walking = getModifierHandler(file, configurationSection, path + ".Walking");
        ModifierHolder swimming = getModifierHandler(file, configurationSection, path + ".Swimming");
        ModifierHolder inMidair = getModifierHandler(file, configurationSection, path + ".In_Midair");
        ModifierHolder gliding = getModifierHandler(file, configurationSection, path + ".Gliding");
        if (always == null && zooming == null && sneaking == null && standing == null && walking == null
                && swimming == null && inMidair == null && gliding == null) {
            return null;
        }
        return new ModifySpreadWhen(always, zooming, sneaking, standing, walking, swimming, inMidair, gliding);
    }

    private ModifierHolder getModifierHandler(File file, ConfigurationSection configurationSection, String path) {
        String value = configurationSection.getString(path);
        if (value == null) return null;
        try {
            return new ModifierHolder(Double.parseDouble(value.split("%")[0]), value.endsWith("%"));
        } catch (NumberFormatException e) {
            DebugUtil.log(LogLevel.ERROR,
                    "Found an invalid number in configurations!",
                    "Located at file " + file + " in " + path + " (" + value + ") in configurations",
                    "Make sure they're numbers e.g. 1.76, 5.21, 8, 23");
            return null;
        }
    }
}