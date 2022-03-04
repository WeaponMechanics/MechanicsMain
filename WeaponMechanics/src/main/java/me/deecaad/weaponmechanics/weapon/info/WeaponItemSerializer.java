package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Simple class to handle weapon item serialization a bit differently.
 */
public class WeaponItemSerializer extends ItemSerializer {

    @Override
    public String getKeyword() {
        return "Weapon_Item";
    }

    @Override
    @Nonnull
    public ItemStack serialize(SerializeData data) throws SerializerException {
        ItemStack weaponStack = super.serializeWithoutRecipe(data);

        String weaponTitle = data.key.split("\\.")[0];
        WeaponMechanics.getWeaponHandler().getInfoHandler().addWeapon(weaponTitle);

        int magazineSize = data.config.getInt(weaponTitle + ".Reload.Magazine_Size", -1);
        if (magazineSize != -1) {
            CustomTag.AMMO_LEFT.setInteger(weaponStack, magazineSize);
        }

        String defaultSelectiveFire = data.config.getString(weaponTitle + ".Shoot.Selective_Fire.Default");
        if (defaultSelectiveFire != null) {
            if (defaultSelectiveFire.equalsIgnoreCase("BURST")) {
                CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, 1);
            } else if (defaultSelectiveFire.equalsIgnoreCase("AUTO")) {
                CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, 2);
            } else {
                CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, 0);
            }
        }

        CustomTag.WEAPON_TITLE.setString(weaponStack, weaponTitle);
        weaponStack = super.serializeRecipe(data, weaponStack);
        return weaponStack;
    }
}