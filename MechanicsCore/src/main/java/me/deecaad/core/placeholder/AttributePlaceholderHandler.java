package me.deecaad.core.placeholder;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;

public abstract class AttributePlaceholderHandler extends NumericPlaceholderHandler {

    private final Attribute attribute;

    public AttributePlaceholderHandler(Attribute attribute) {
        super("item_" + attribute.name().toLowerCase(Locale.ROOT));
        this.attribute = attribute;
    }

    @Override
    public @Nullable Number requestValue(@NotNull PlaceholderData data) {
        ItemStack item = data.item();
        if (item == null || !item.hasItemMeta())
            return null;

        EquipmentSlot equipmentSlot = data.slot();
        return getAttributeValue(item, attribute, equipmentSlot);
    }

    public static double getAttributeValue(@NotNull ItemStack item, @NotNull Attribute attribute, @NotNull EquipmentSlot slot) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return 0.0;

        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
        if (modifiers == null)
            return 0.0;

        double value = 0.0;
        for (AttributeModifier modifier : modifiers) {
            if (!modifier.getSlotGroup().test(slot))
                continue;

            switch (modifier.getOperation()) {
                case ADD_NUMBER:
                    value += modifier.getAmount();
                    break;
                case ADD_SCALAR:
                    value += modifier.getAmount() * item.getAmount();
                    break;
                case MULTIPLY_SCALAR_1:
                    value *= modifier.getAmount();
                    break;
            }
        }

        return value;
    }
}
