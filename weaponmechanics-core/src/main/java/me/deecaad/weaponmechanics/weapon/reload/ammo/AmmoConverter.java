package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.weapon.info.WeaponConverter;
import org.jetbrains.annotations.NotNull;

public class AmmoConverter extends WeaponConverter {

    private boolean preserveItem;

    /**
     * Default constructor for serializer.
     */
    public AmmoConverter() {
        super();
    }

    public AmmoConverter(boolean type, boolean name, boolean lore, boolean enchantments, boolean cmd, boolean preserveItem) {
        super(type, name, lore, enchantments, cmd);
        this.preserveItem = preserveItem;
    }

    /**
     * When <code>true</code>, matching items are tagged as ammo in-place (only the ammo NBT is added)
     * instead of having their type/meta overwritten with the configured Bullet_Item/Magazine_Item
     * template. Use this to allow custom items (e.g. from other plugins) to be used as ammo directly,
     * without WeaponMechanics stripping their custom appearance/data.
     */
    public boolean isPreserveItem() {
        return preserveItem;
    }

    @Override
    public String getKeyword() {
        // We have to set this to null, that way FileReader doesn't try to use/add it
        // to the list of serializers (overriding the WeaponConverter).
        return null;
    }

    @Override
    public @NotNull AmmoConverter serialize(@NotNull SerializeData data) throws SerializerException {
        boolean type = data.of("Type").getBool().orElse(false);
        boolean name = data.of("Name").getBool().orElse(false);
        boolean lore = data.of("Lore").getBool().orElse(false);
        boolean enchantments = data.of("Enchantments").getBool().orElse(false);
        boolean cmd = data.of("Custom_Model_Data").getBool().orElse(false);
        boolean preserveItem = data.of("Preserve_Item").getBool().orElse(false);

        if (!type && !name && !lore && !enchantments && !cmd) {
            throw data.exception(null, "'Type', 'Name', 'Lore', 'Enchantments', 'Custom_Model_Data' are all 'false'",
                "One of them should be 'true' to allow ammo conversion",
                "If you want to remove the ammo conversion feature, remove the 'Ammo_Converter_Check' option from config");
        }

        return new AmmoConverter(type, name, lore, enchantments, cmd, preserveItem);
    }
}