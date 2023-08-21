package me.deecaad.core.placeholder;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.utils.AttributeType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Returns the value of {@link AttributeType#GENERIC_ARMOR} on the item.
 */
public class PArmorAttribute extends PlaceholderHandler {

    public PArmorAttribute() {
        super("item_armor_attribute");
    }

    @Nullable
    @Override
    public String onRequest(@NotNull PlaceholderData data) {
        ItemStack item = data.item();
        if (item == null || !item.hasItemMeta())
            return null;

        EquipmentSlot equipmentSlot = data.slot();
        NBTCompatibility.AttributeSlot slot = equipmentSlot == null ? null : switch (equipmentSlot) {
            case HAND -> NBTCompatibility.AttributeSlot.MAIN_HAND;
            case OFF_HAND -> NBTCompatibility.AttributeSlot.OFF_HAND;
            case FEET -> NBTCompatibility.AttributeSlot.FEET;
            case LEGS -> NBTCompatibility.AttributeSlot.LEGS;
            case CHEST -> NBTCompatibility.AttributeSlot.CHEST;
            case HEAD -> NBTCompatibility.AttributeSlot.HEAD;
        };

        double value = CompatibilityAPI.getNBTCompatibility().getAttribute(item, AttributeType.GENERIC_ARMOR, slot);
        return String.valueOf(value);
    }
}
