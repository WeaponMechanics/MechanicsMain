package me.deecaad.core.placeholder;

import me.deecaad.core.utils.AttributeType;

/**
 * Returns the value of {@link AttributeType#GENERIC_MAX_HEALTH} on the item.
 */
public class PHealthAttribute extends AttributePlaceholderHandler {

    public PHealthAttribute() {
        super(AttributeType.GENERIC_MAX_HEALTH);
    }
}
