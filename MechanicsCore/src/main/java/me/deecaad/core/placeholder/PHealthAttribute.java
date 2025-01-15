package me.deecaad.core.placeholder;

import org.bukkit.attribute.Attribute;

/**
 * Returns the value of {@link Attribute#MAX_HEALTH} on the item.
 */
public class PHealthAttribute extends AttributePlaceholderHandler {

    public PHealthAttribute() {
        super(Attribute.MAX_HEALTH);
    }
}
