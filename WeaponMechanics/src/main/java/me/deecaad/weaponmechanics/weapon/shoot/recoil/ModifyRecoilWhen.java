package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.weapon.shoot.AModifyWhen;
import me.deecaad.weaponmechanics.weapon.shoot.NumberModifier;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class ModifyRecoilWhen extends AModifyWhen {

    /**
     * Empty constructor to be used as serializer
     */
    public ModifyRecoilWhen() { }

    public ModifyRecoilWhen(NumberModifier always, NumberModifier zooming, NumberModifier sneaking, NumberModifier standing, NumberModifier walking, NumberModifier swimming, NumberModifier inMidair, NumberModifier gliding) {
        super(always, zooming, sneaking, standing, walking, swimming, inMidair, gliding);
    }

    @Override
    public String getKeyword() {
        return "Modify_Recoil_When";
    }

    @Override
    public ModifyRecoilWhen serialize(File file, ConfigurationSection configurationSection, String path) {

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
        return new ModifyRecoilWhen(always, zooming, sneaking, standing, walking, swimming, inMidair, gliding);
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
                    "Make sure they're numbers e.g. 17.6, 52.1, 8, 23");
            return null;
        }
    }
}
