package me.deecaad.weaponmechanics.weapon.skin;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalInt;

public class BaseSkin implements Skin, Serializer<BaseSkin>  {

    private Optional<Material> type;
    private OptionalInt data;
    private OptionalInt durability;
    private OptionalInt customModelData;

    /**
     * Default constructor for Serializer
     */
    public BaseSkin() {
    }

    public BaseSkin(int customModelData) {
        this.type = Optional.empty();
        this.data = OptionalInt.empty();
        this.durability = OptionalInt.empty();
        this.customModelData = OptionalInt.of(customModelData);
    }

    public BaseSkin(
            @NotNull Optional<Material> type,
            @NotNull OptionalInt data,
            @NotNull OptionalInt durability,
            @NotNull OptionalInt customModelData
    ) {
        this.type = type;
        this.data = data;
        this.durability = durability;
        this.customModelData = customModelData;
    }

    public boolean hasType() {
        return type.isPresent();
    }

    public Material getType() {
        return type.orElse(null);
    }

    public boolean hasData() {
        return data.isPresent();
    }

    public byte getData() {
        return (byte) data.orElse(Byte.MIN_VALUE);
    }

    public boolean hasDurability() {
        return durability.isPresent();
    }

    public short getDurability() {
        return (short) durability.orElse(Short.MIN_VALUE);
    }

    public boolean hasCustomModelData() {
        return customModelData.isPresent();
    }

    public int getCustomModelData() {
        return customModelData.orElse(Integer.MIN_VALUE);
    }

    /**
     * Applies this skin to given item stack
     *
     * @param weapon the item stack for which to apply skin
     */
    public void apply(@NotNull ItemStack weapon) {
        int version = ReflectionUtil.getMCVersion();

        if (type.isPresent() && weapon.getType() != type.get())
            weapon.setType(type.get());

        if (data.isPresent())
            weapon.getData().setData((byte) data.getAsInt());

        boolean hasMetaChanges = false;
        ItemMeta meta = weapon.getItemMeta();

        // Happens when somebody tries to apply a skin to air
        if (meta == null)
            throw new IllegalArgumentException("Tried to apply skin to item without meta: " + weapon);

        if (durability.isPresent()) {
            if (version >= 13) {
                if (((org.bukkit.inventory.meta.Damageable) meta).getDamage() != durability.getAsInt()) {
                    ((org.bukkit.inventory.meta.Damageable) meta).setDamage(durability.getAsInt());
                    hasMetaChanges = true;
                }
            } else if (weapon.getDurability() != durability.getAsInt()) {
                weapon.setDurability((short) durability.getAsInt());
                hasMetaChanges = true;
            }
        }

        if (customModelData.isPresent() && (!meta.hasCustomModelData() || meta.getCustomModelData() != customModelData.getAsInt())) {
            meta.setCustomModelData(customModelData.getAsInt());
            hasMetaChanges = true;
        }

        if (hasMetaChanges) {
            weapon.setItemMeta(meta);
        }
    }

    @Override
    @NotNull
    public BaseSkin serialize(@NotNull SerializeData data) throws SerializerException {
        int version = ReflectionUtil.getMCVersion();
        boolean shouldUseCmd = version >= 14;

        Optional<Material> type = Optional.ofNullable(data.of("Type").getEnum(Material.class, null));
        OptionalInt legacyData = data.has("Legacy_Data") ? OptionalInt.of(data.of("Legacy_Data").assertExists().assertRange(0, Byte.MAX_VALUE).getInt()) : OptionalInt.empty();
        OptionalInt durability = data.has("Durability") ? OptionalInt.of(data.of("Durability").assertExists().assertRange(0, Short.MAX_VALUE).getInt()) : OptionalInt.empty();
        OptionalInt customModelData = data.has("Custom_Model_Data") ? OptionalInt.of(data.of("Custom_Model_Data").assertExists().getInt()) : OptionalInt.empty();

        if (legacyData.isPresent() && version > 12) {
            throw data.exception("Legacy_Data", "Cannot use 'Legacy_Data' on MC version 1." + version,
                    "Instead, use the '" + (shouldUseCmd ? "Custom_Model_Data" : "Durability") + "' feature",
                    "Wiki: https://cjcrafter.gitbook.io/weaponmechanics/weapon-modules/skin");
        }

        // Cannot use Custom_Model_Data before 1.14
        if (!shouldUseCmd && customModelData.isPresent()) {
            throw data.exception("Custom_Model_Data", "Cannot use 'Custom_Model_Data' on MC version 1." + version,
                    "Custom_Model_Data was added in Minecraft 1.14",
                    "To fix, you can either update your server, or use 'Durability' instead");
        }

        // Should use at least 1
        if (type.isEmpty() && legacyData.isEmpty() && durability.isEmpty() && customModelData.isEmpty()) {
            throw data.exception(null, "Tried to create a skin without using 'Durability' or 'Custom_Model_Data'... did you misspell something?",
                    "Double check your configs to make sure it is spelled right.");
        }

        return new BaseSkin(type, legacyData, durability, customModelData);
    }
}