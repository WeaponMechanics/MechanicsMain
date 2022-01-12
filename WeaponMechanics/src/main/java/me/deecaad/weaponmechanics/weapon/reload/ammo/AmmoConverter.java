package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.weapon.info.WeaponConverter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull AmmoConverter serialize(SerializeData data) throws SerializerException {
        boolean type = data.of("Type").assertType(Boolean.class).get(false);
        boolean name = data.of("Name").assertType(Boolean.class).get(false);
        boolean lore = data.of("Lore").assertType(Boolean.class).get(false);
        boolean enchantments = data.of("Enchantments").assertType(Boolean.class).get(false);

        if (!type && !name && !lore && !enchantments) {
            data.throwException("'Type', 'Name', 'Lore', and 'Enchantments' are all 'false'",
                    "One of them should be 'true' to allow ammo conversion",
                    "If you want to remove the ammo conversion feature, remove the '" + getKeyword() + "' option from config");
        }

        return new AmmoConverter(type, name, lore, enchantments);
    }
}