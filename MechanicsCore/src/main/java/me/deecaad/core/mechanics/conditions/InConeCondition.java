package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.VectorSerializer;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.utils.VectorUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InConeCondition extends Condition {

    private VectorSerializer direction;
    private double cosAngle; // Cosine of the angle

    /**
     * Default constructor for serializer.
     */
    public InConeCondition() {
    }

    public InConeCondition(VectorSerializer direction, double angle) {
        this.direction = direction;
        this.cosAngle = Math.cos(Math.toRadians(angle));
    }

    @Override
    public boolean isAllowed0(CastData cast) {
        Location sourceLocation = cast.getSource().getEyeLocation();
        Vector source = sourceLocation.toVector();
        Vector direction;
        if (this.direction != null) {
            // might be normalized... normalize it
            direction = this.direction.getVector(cast.getSource()).normalize();
        } else {
            // this is always a normalized vector
            direction = sourceLocation.getDirection();
        }

        // Adjust the source location so that the cone's origin is moved back a
        // bit. This helps get overlapping entities.
        VectorUtil.addScaledVector(source, direction, -0.5);

        Vector target = cast.getTargetLocation().toVector();
        Vector toTarget = target.clone().subtract(source).normalize();

        double dot = direction.dot(toTarget);
        return dot >= cosAngle;
    }

    @Override
    public String getKeyword() {
        return "InCone";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/conditions/incone";
    }

    @Override
    public @NotNull Condition serialize(@NotNull SerializeData data) throws SerializerException {
        double angle = data.of("Angle").assertPositive().assertRange(0.0, 180.0).getDouble(30.0);
        VectorSerializer direction = data.of("Direction").serialize(VectorSerializer.class);
        return applyParentArgs(data, new InConeCondition(direction, angle));
    }
}
