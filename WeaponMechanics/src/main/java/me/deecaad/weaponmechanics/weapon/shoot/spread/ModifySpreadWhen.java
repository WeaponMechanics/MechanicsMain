package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class ModifySpreadWhen implements Serializer<ModifySpreadWhen> {

    private NumberModifier always;
    private NumberModifier zooming;
    private NumberModifier sneaking;
    private NumberModifier standing;
    private NumberModifier walking;
    private NumberModifier swimming;
    private NumberModifier inMidair;
    private NumberModifier gliding;

    /**
     * Empty constructor to be used as serializer
     */
    public ModifySpreadWhen() { }

    public ModifySpreadWhen(NumberModifier always, NumberModifier zooming, NumberModifier sneaking, NumberModifier standing, NumberModifier walking, NumberModifier swimming, NumberModifier inMidair, NumberModifier gliding) {
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
        if (zooming != null && (entityWrapper.getMainHandData().getZoomData().isZooming() || entityWrapper.getOffHandData().getZoomData().isZooming())) {
            tempSpread = zooming.applyTo(tempSpread);
        }
        if (sneaking != null && entityWrapper.isSneaking()) {
            tempSpread = sneaking.applyTo(tempSpread);
        }
        if (standing != null && entityWrapper.isStanding()) {
            tempSpread = standing.applyTo(tempSpread);
        }
        if (walking != null && entityWrapper.isWalking()) {
            tempSpread = walking.applyTo(tempSpread);
        }
        if (swimming != null && entityWrapper.isSwimming()) {
            tempSpread = swimming.applyTo(tempSpread);
        }
        if (inMidair != null && entityWrapper.isInMidair()) {
            tempSpread = inMidair.applyTo(tempSpread);
        }
        if (gliding != null && entityWrapper.isGliding()) {
            tempSpread = gliding.applyTo(tempSpread);
        }

        return Math.max(tempSpread, 0.0);
    }

    @Override
    public String getKeyword() {
        return "Modify_Spread_When";
    }

    @Override
    public ModifySpreadWhen serialize(File file, ConfigurationSection configurationSection, String path) {
        NumberModifier always = getModifierHandler(file, configurationSection, path + ".Always");
        NumberModifier zooming = getModifierHandler(file, configurationSection, path + ".Zooming");
        NumberModifier sneaking = getModifierHandler(file, configurationSection, path + ".Sneaking");
        NumberModifier standing = getModifierHandler(file, configurationSection, path + ".Standing");
        NumberModifier walking = getModifierHandler(file, configurationSection, path + ".Walking");
        NumberModifier swimming = getModifierHandler(file, configurationSection, path + ".Swimming");
        NumberModifier inMidair = getModifierHandler(file, configurationSection, path + ".In_Midair");
        NumberModifier gliding = getModifierHandler(file, configurationSection, path + ".Gliding");
        if (always == null && zooming == null && sneaking == null && standing == null && walking == null
                && swimming == null && inMidair == null && gliding == null) {
            return null;
        }
        return new ModifySpreadWhen(always, zooming, sneaking, standing, walking, swimming, inMidair, gliding);
    }

    private NumberModifier getModifierHandler(File file, ConfigurationSection configurationSection, String path) {
        String value = configurationSection.getString(path);
        if (value == null) return null;
        try {
            return new NumberModifier(Double.parseDouble(value.split("%")[0]), value.endsWith("%"));
        } catch (NumberFormatException e) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid number in configurations!",
                    "Located at file " + file + " in " + path + " (" + value + ") in configurations",
                    "Make sure they're numbers e.g. 1.76, 5.21, 8, 23");
            return null;
        }
    }
}