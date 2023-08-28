package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.InlineSerializer;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.VectorSerializer;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 * A targeter returns a list of targets. A target can be a {@link org.bukkit.Location},
 * an {@link org.bukkit.entity.Entity}, or a {@link org.bukkit.entity.Player}.
 */
public abstract class Targeter implements InlineSerializer<Targeter> {

    private boolean eye;
    private VectorSerializer offset;

    /**
     * Default constructor for serializers.
     */
    public Targeter() {
    }

    @Nullable
    @Override
    public String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/Mechanics#targeters";
    }

    public boolean isEye() {
        return eye;
    }

    @Nullable
    public VectorSerializer getOffset() {
        return offset;
    }

    /**
     * Returns <code>true</code> if this targeter specifically targets an
     * entity. Entity targeters also target locations by default, but that
     * location will always be the entity's location.
     *
     * <p>There is 1 caveat, when {@link #getOffset()} is non-null, the
     * targeted location will be different from the targeted entity.
     *
     * @return true if this targeter targets entities, not specific locations.
     */
    public abstract boolean isEntity();

    /**
     * Public method to get every possible target for the mechanic using this
     * targeter. The returned targets will have the offset
     * ({@link #getOffset()}) already applied.
     *
     * @param cast The non-null origin of the cast.
     * @return The list of targets.
     */
    public final Iterator<CastData> getTargets(CastData cast) {
        Iterator<CastData> targets = getTargets0(cast);

        // Only modify if we need to
        if (offset != null || eye) {
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return targets.hasNext();
                }

                @Override
                public CastData next() {
                    CastData target = targets.next();

                    Location origin = (eye && target.getTarget() != null) ? target.getTarget().getEyeLocation() : target.getTargetLocation();
                    if (offset != null)
                        origin.add(offset.getVector(target.getTarget()));

                    target.setTargetLocation(origin);
                    return target;
                }
            };
        }

        return targets;
    }

    protected abstract Iterator<CastData> getTargets0(CastData cast);

    protected Targeter applyParentArgs(SerializeData data, Targeter targeter) throws SerializerException {
        VectorSerializer offset = data.of("Offset").serialize(VectorSerializer.class);
        if (!isEntity() && offset != null && offset.isRelative())
            throw data.exception("offset", "Did you try to use relative locations ('~') with '" + getInlineKeyword() + "'?",
                    getInlineKeyword() + " is a LOCATION targeter, so it cannot use relative locations.");

        targeter.offset = offset;
        targeter.eye = data.of("Eye").getBool(false);
        return targeter;
    }
}
