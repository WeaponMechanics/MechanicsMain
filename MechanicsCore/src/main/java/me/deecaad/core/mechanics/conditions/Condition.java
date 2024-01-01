package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.InlineSerializer;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import org.jetbrains.annotations.Nullable;

/**
 * A condition is a simple true/false statement that decides whether a
 * {@link Mechanic} is allowed to be used (on a specific entity, in a specific
 * world, etc.).
 */
public abstract class Condition implements InlineSerializer<Condition> {

    private boolean isInverted;

    @Nullable
    @Override
    public String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/#conditions";
    }

    /**
     * Returns <code>true</code> if {@link Mechanic} that holds this condition
     * is allowed to be used.
     *
     * @param cast The non-null data involving the who/what/where.
     * @return true if the mechanic can be used.
     */
    public final boolean isAllowed(CastData cast) {
        return isInverted != isAllowed0(cast);
    }

    protected abstract boolean isAllowed0(CastData cast);

    protected Condition applyParentArgs(SerializeData data, Condition condition) throws SerializerException {
        condition.isInverted = data.of("Inverted").getBool(false);
        return condition;
    }
}
