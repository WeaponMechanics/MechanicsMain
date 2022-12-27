package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ColorSerializer;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.shoot.SelectiveFireState;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Simple class to handle weapon item serialization a bit differently.
 */
public class WeaponItemSerializer extends ItemSerializer {

    /**
     * Default constructor for serializer
     */
    public WeaponItemSerializer() {
    }

    @Override
    public String getKeyword() {
        return "Weapon_Item";
    }

    @Override
    @Nonnull
    public ItemStack serialize(SerializeData data) throws SerializerException {
        ItemStack weaponStack = super.serializeWithoutRecipe(data);

        // Crossbows are not allowed to be used as the weapon's type. WMC
        // supports "faking" the crossbow for other players. I added this check
        // since it is a commonly asked question, and this should save me some
        // time in the long run.
        if (weaponStack.getType().name().equals("CROSSBOW"))
            throw data.exception("Type", "You cannot use 'CROSSBOW' as a WeaponMechanics weapon!",
                    "YES! We know that you want weapons to be 'held up' like a minecraft crossbow",
                    "Purchase WMC to 'fake' the crossbow animation for other players: https://www.spigotmc.org/resources/104539/");

        String weaponTitle = data.key.split("\\.")[0];
        WeaponMechanics.getWeaponHandler().getInfoHandler().addWeapon(weaponTitle);

        int magazineSize = data.config.getInt(weaponTitle + ".Reload.Magazine_Size", -1);
        if (magazineSize != -1) {
            CustomTag.AMMO_LEFT.setInteger(weaponStack, magazineSize);
        }

        String defaultSelectiveFire = data.config.getString(weaponTitle + ".Shoot.Selective_Fire.Default");
        if (defaultSelectiveFire != null) {

            try {
                SelectiveFireState state = SelectiveFireState.valueOf(defaultSelectiveFire);
                CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, state.ordinal());
            } catch (IllegalArgumentException e) {
                throw data.exception(null, SerializerException.forValue(defaultSelectiveFire),
                        SerializerException.didYouMean(defaultSelectiveFire, Arrays.asList("SINGLE", "BURST", "AUTO"))
                );
            }
        }

        CustomTag.WEAPON_TITLE.setString(weaponStack, weaponTitle);
        weaponStack = super.serializeRecipe(data, weaponStack);
        return weaponStack;
    }
}