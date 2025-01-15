package me.deecaad.core.placeholder;

import org.bukkit.attribute.Attribute;

/**
 * Returns the value of {@link Attribute#MOVEMENT_SPEED} on the item.
 */
public class PMovementSpeedAttribute extends AttributePlaceholderHandler {

    public PMovementSpeedAttribute() {
        super(Attribute.MOVEMENT_SPEED);
    }
}
