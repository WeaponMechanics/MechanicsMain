package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.general.ItemSerializer;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.File;

/**
 * Simple class to handle weapon item serialization a bit differently.
 */
public class WeaponItemSerializer extends ItemSerializer {

    @Override
    public String getKeyword() {
        return "Weapon_Item";
    }

    @Override
    public ItemStack serialize(File file, ConfigurationSection configurationSection, String path) {
        ItemStack weaponStack = super.serialize(file, configurationSection, path);
        if (weaponStack == null) return null;
        String weaponTitle = path.split("\\.")[0];
        WeaponMechanics.getWeaponHandler().getInfoHandler().addWeapon(weaponTitle);
        return TagHelper.setCustomTag(weaponStack, CustomTag.WEAPON_TITLE, weaponTitle);
    }
}