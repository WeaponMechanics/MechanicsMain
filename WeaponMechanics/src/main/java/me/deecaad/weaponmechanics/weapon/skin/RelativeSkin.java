package me.deecaad.weaponmechanics.weapon.skin;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerTypeException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RelativeSkin implements Skin, Serializer<RelativeSkin> {

    private int customModelData;

    /**
     * Default constructor for serializer.
     */
    public RelativeSkin() {
    }

    public RelativeSkin(int customModelData) {
        this.customModelData = customModelData;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    @Override
    public void apply(@NotNull ItemStack item) {
        // Instead of using Skin#apply use SkinSelector#apply
        throw new UnsupportedOperationException("apply");
    }

    @NotNull
    @Override
    public RelativeSkin serialize(SerializeData data) throws SerializerException {

        if (data.of().is(ConfigurationSection.class)) {
            throw data.exception(null, "Tried to override a 'Relative Skin' with 'Normal Skin'",
                    "When using the '+10' feature of skins, ALL of your skins on this weapon must use the + feature");
        }

        String str = data.of().assertExists().get().toString().trim().toLowerCase();

        if (str.startsWith("+"))
            str = str.substring(1).trim();
        if (str.startsWith("add"))
            str = str.substring(3).trim();

        try {
            return new RelativeSkin(Integer.parseInt(str));
        } catch (NumberFormatException ex) {
            throw new SerializerTypeException(this, Integer.class, String.class, str, data.of().getLocation());
        }
    }
}
