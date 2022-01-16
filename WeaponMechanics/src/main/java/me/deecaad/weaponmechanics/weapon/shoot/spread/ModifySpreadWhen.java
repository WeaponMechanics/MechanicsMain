package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerTypeException;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.weapon.shoot.AModifyWhen;
import me.deecaad.weaponmechanics.weapon.shoot.NumberModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class ModifySpreadWhen extends AModifyWhen {

    /**
     * Empty constructor to be used as serializer
     */
    public ModifySpreadWhen() { }

    public ModifySpreadWhen(NumberModifier always, NumberModifier zooming, NumberModifier sneaking, NumberModifier standing, NumberModifier walking, NumberModifier swimming, NumberModifier inMidair, NumberModifier gliding) {
        super(always, zooming, sneaking, standing, walking, swimming, inMidair, gliding);
    }

    @Override
    public String getKeyword() {
        return "Modify_Spread_When";
    }

    @Override
    @Nonnull
    public @NotNull ModifySpreadWhen serialize(SerializeData data) throws SerializerException {
        NumberModifier always   = getModifierHandler(data.move("Always"));
        NumberModifier zooming  = getModifierHandler(data.move("Zooming"));
        NumberModifier sneaking = getModifierHandler(data.move("Sneaking"));
        NumberModifier standing = getModifierHandler(data.move("Standing"));
        NumberModifier walking  = getModifierHandler(data.move("Walking"));
        NumberModifier swimming = getModifierHandler(data.move("Swimming"));
        NumberModifier inMidair = getModifierHandler(data.move("In_Midair"));
        NumberModifier gliding  = getModifierHandler(data.move("Gliding"));

        if (always == null && zooming == null && sneaking == null && standing == null && walking == null
                && swimming == null && inMidair == null && gliding == null) {

            data.throwException(null, "Tried to use Modify_Spread_When without any arguments");
        }
        return new ModifySpreadWhen(always, zooming, sneaking, standing, walking, swimming, inMidair, gliding);
    }

    private NumberModifier getModifierHandler(SerializeData data) throws SerializerException {
        String value = data.of().assertExists().get().toString();
        if (value == null) return null;
        try {
            boolean percentage = value.endsWith("%");
            double number = Double.parseDouble(value.split("%")[0]);
            if (!percentage) {
                number *= 0.01;
            }

            return new NumberModifier(number, percentage);
        } catch (NumberFormatException e) {
            throw new SerializerTypeException(this, Double.class, null, value, data.of().getLocation());
        }
    }
}