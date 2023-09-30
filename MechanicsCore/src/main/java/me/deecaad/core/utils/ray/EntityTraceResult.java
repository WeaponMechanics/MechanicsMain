package me.deecaad.core.utils.ray;

import me.deecaad.core.compatibility.HitBox;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class EntityTraceResult extends RayTraceResult {

    private final @NotNull LivingEntity entity;

    public EntityTraceResult(
            @NotNull Vector origin,
            @NotNull Vector direction,
            @NotNull HitBox hitBox,
            @NotNull BlockFace hitFace,
            @NotNull BlockFace exitFace,
            double hitMin,
            double hitMax,
            @NotNull LivingEntity entity
    ) {
        super(origin, direction, hitBox, hitFace, exitFace, hitMin, hitMax);
        this.entity = entity;
    }

    /**
     * Returns the entity that was hit.
     *
     * @return The entity.
     */
    @NotNull
    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return "EntityTraceResult{" +
                "entity=" + entity +
                ", hitFace=" + super.getHitFace() +
                ", hitLocation=" + super.getHitLocation() +
                ", hitMin=" + super.getHitMin() +
                ", hitMax=" + super.getHitMax() +
                '}';
    }
}
