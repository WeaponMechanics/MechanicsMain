package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.shoot.SelectiveFireState;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.regex.Pattern;

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
    @NotNull
    public ItemStack serialize(@NotNull SerializeData data) throws SerializerException {
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

        // Ensure the weapon title uses the correct format, mostly for other plugin compatibility
        Pattern pattern = Pattern.compile("[A-Za-z0-9_]+");
        if (!pattern.matcher(weaponTitle).matches()) {
            throw data.exception(null, "Weapon title must only contain letters, numbers, and underscores!",
                    "For example, AK-47 is not allowed, but AK_47 is fine",
                    "This is only for the weapon title (the name defined in config), NOT the display name of the weapon. The display can be whatever you want.",
                    SerializerException.forValue(weaponTitle));
        }

        WeaponMechanics.getWeaponHandler().getInfoHandler().addWeapon(weaponTitle);

        int magazineSize = (Integer) data.config.get(weaponTitle + ".Reload.Magazine_Size", -1);
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