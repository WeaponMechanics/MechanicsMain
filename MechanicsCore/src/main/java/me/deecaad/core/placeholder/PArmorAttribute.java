package me.deecaad.core.placeholder;

import org.bukkit.attribute.Attribute;

/**
 * Returns the value of {@link Attribute#ARMOR} on the item.
 */
public class PArmorAttribute extends AttributePlaceholderHandler {

    public PArmorAttribute() {
        super(Attribute.ARMOR);
    }
}
