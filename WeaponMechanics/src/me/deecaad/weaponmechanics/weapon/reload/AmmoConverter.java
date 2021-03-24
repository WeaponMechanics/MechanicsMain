package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.weaponmechanics.weapon.info.WeaponConverter;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

public class AmmoConverter extends WeaponConverter {

    public AmmoConverter() {
        super();
    }

    public AmmoConverter(boolean type, boolean name, boolean lore, boolean enchantments) {
        super(type, name, lore, enchantments);
    }

    @Override
    public String getKeyword() {
        return "Ammo_Converter_Check";
    }

    @Override
    public AmmoConverter serialize(File file, ConfigurationSection configurationSection, String path) {
        boolean type = configurationSection.getBoolean(path + ".Type");
        boolean name = configurationSection.getBoolean(path + ".Name");
        boolean lore = configurationSection.getBoolean(path + ".Lore");
        boolean enchantments = configurationSection.getBoolean(path + ".Enchantments");
        if (!type && !name && !lore && !enchantments) {
            return null;
        }

        return new AmmoConverter(type, name, lore, enchantments);
    }
}