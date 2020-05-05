package me.deecaad.core.effects;

import org.bukkit.util.Vector;

/**
 * This interface makes any class that implements it
 * <code>Offsetable</code>, which means that it can be
 * offset, or moved, by a <code>Vector</code>
 */
public interface Offsetable {

    /**
     * Returns this offsetable's offset
     *
     * @return Vector version of offset
     */
    Vector getOffset();

    /**
     * Sets the offset
     *
     * @param offset Vector version of offset
     */
    void setOffset(Vector offset);
}
