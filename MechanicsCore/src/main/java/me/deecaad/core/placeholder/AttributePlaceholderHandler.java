package me.deecaad.core.placeholder;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.utils.AttributeType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class AttributePlaceholderHandler extends NumericPlaceholderHandler {

    private final AttributeType attribute;

    public AttributePlaceholderHandler(AttributeType attribute) {
        super("item_" + attribute.name().toLowerCase(Locale.ROOT));
        this.attribute = attribute;
    }

    @Nullable
    @Override
    public Number requestValue(@NotNull PlaceholderData data) {
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

        return CompatibilityAPI.getNBTCompatibility().getAttributeValue(item, attribute, slot);
    }
}
