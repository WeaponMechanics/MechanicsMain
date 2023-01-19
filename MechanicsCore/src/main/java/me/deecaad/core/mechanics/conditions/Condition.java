package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.inline.InlineSerializer;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;

/**
 * A condition is a simple true/false statement that decides whether a
 * {@link Mechanic} is allowed to be used (on a specific entity, in a specific
 * world, etc.).
 */
public abstract class Condition extends InlineSerializer<Condition> {

    /**
     * Returns <code>true</code> if {@link Mechanic} that holds this condition
     * is allowed to be used.
     *
     * @param cast The non-null data involving the who/what/where.
     * @return true if the mechanic can be used.
     */
    public abstract boolean isAllowed(CastData cast);
}
