package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.VectorSerializer;
import me.deecaad.core.mechanics.CastData;
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

        Vector target = cast.getTargetLocation().toVector();
        Vector toTarget = target.clone().subtract(source).normalize();

        // Use vector projection to determine if the target is on the right side of the cone
        //double sourceProjection = source.dot(direction);
        //double targetProjection = target.dot(direction);
        //if (targetProjection < sourceProjection) {
        //    return false;
        //}

        return direction.dot(toTarget) < cosAngle;
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
