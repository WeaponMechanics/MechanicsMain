package me.deecaad.core.utils.ray;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.HitBox;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RayTraceResult {

    private final @NotNull Vector origin; // no getter... immutable
    private final @NotNull Vector direction; // no getter... immutable
    private final @NotNull HitBox hitBox;
    private final @NotNull BlockFace hitFace;
    private final @NotNull BlockFace exitFace;
    private final double hitMin;
    private final double hitMax;

    // cached if calculated
    private @Nullable Vector hitLocation;
    private @Nullable Vector exitLocation;

    public RayTraceResult(
            @NotNull Vector origin,
            @NotNull Vector direction,
            @NotNull HitBox hitBox,
            @NotNull BlockFace hitFace,
            @NotNull BlockFace exitFace,
            double hitMin,
            double hitMax
    ) {
        this.origin = origin;
        this.direction = direction;
        this.hitBox = hitBox;
        this.hitFace = hitFace;
        this.exitFace = exitFace;
        this.hitMin = hitMin;
        this.hitMax = hitMax;
    }

    /**
     * Returns the that hitbox that was hit.
     *
     * @return The hitbox.
     */
    @NotNull
    public HitBox getHitBox() {
        return hitBox;
    }

    /**
     * Returns the hit face. Will be one of:
     * <ul>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#WEST}</li>
     *     <li>{@link BlockFace#UP}</li>
     *     <li>{@link BlockFace#DOWN}</li>
     * </ul>
     *
     * <p>Even when the raytrace starts within the hitbox, this will return the
     * hit face as if it were "moved back" before ray tracing. This means that
     * you might want to check {@link #getHitMin()} and check if it is negative.
     *
     * @return The hit hitface.
     */
    @NotNull
    public BlockFace getHitFace() {
        return hitFace;
    }

    /**
     * Returns the exit face. Will be one of:
     * <ul>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#WEST}</li>
     *     <li>{@link BlockFace#UP}</li>
     *     <li>{@link BlockFace#DOWN}</li>
     * </ul>
     *
     * @return The hit hitface.
     */
    @NotNull
    public BlockFace getExitFace() {
        return exitFace;
    }

    /**
     * Returns the exact coordinates that were hit, in world space.
     *
     * @return The hit location.
     */
    @NotNull
    public Vector getHitLocation() {
        if (hitLocation == null) {
            hitLocation = origin.clone();
            hitLocation.setX(hitLocation.getX() + direction.getX() * hitMin);
            hitLocation.setY(hitLocation.getY() + direction.getY() * hitMin);
            hitLocation.setZ(hitLocation.getZ() + direction.getZ() * hitMin);
        }

        return hitLocation;
    }

    /**
     * Returns the exact coordinates that the ray exits the hitbox, in world space.
     *
     * @return The exit location.
     */
    @NotNull
    public Vector getExitLocation() {
        if (exitLocation == null) {
            exitLocation = origin.clone();
            exitLocation.setX(exitLocation.getX() + direction.getX() * hitMax);
            exitLocation.setY(exitLocation.getY() + direction.getY() * hitMax);
            exitLocation.setZ(exitLocation.getZ() + direction.getZ() * hitMax);
        }

        return exitLocation;
    }

    /**
     * Returns the distance between the origin of the ray and the hit location.
     * Will return a negative number if the ray spawned within the hitbox.
     *
     * @return The <i>entry wound</i> of the ray hit.
     */
    public double getHitMin() {
        return hitMin;
    }

    /**
     * Same as {@link #getHitMin()}, except this method will always return a
     * positive number.
     *
     * @return The <i>entry wound</i> of the ray hit (with 0 as a minimum bound).
     */
    public double getHitMinClamped() {
        return Math.max(hitMin, 0.0); // assert positive number
    }

    /**
     * Returns the distance between the origin and the furthest point of the hitbox.
     *
     * @return The <i>exit wound</i> of the ray hit.
     */
    public double getHitMax() {
        return hitMax;
    }

    /**
     * Returns the distance travelled through the hitbox. For a block, this
     * number will not exceed <code>sqrt(2)</code>. This method will always
     * return a positive number.
     *
     * @return The distance travelled through the hitbox.
     */
    public double getThroughDistance() {
        return Math.max(hitMax - hitMin, 0.0); // assert positive number
    }

    public void outlineOnlyHitPosition(Entity player) {
        Vector hitLocation = getHitLocation();
        double x = hitLocation.getX();
        double y = hitLocation.getY();
        double z = hitLocation.getZ();
        if (CompatibilityAPI.getVersion() < 1.13) {
            player.getWorld().spawnParticle(Particle.CRIT, x, y, z, 1, 0, 0, 0, 0.0001);
        } else {
            player.getWorld().spawnParticle(Particle.REDSTONE, x, y, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.BLACK, 1.5f), true);
        }
    }

    // DEPRECATED METHODS FROM BEFORE WE SWITCHED TO BlockTraceResult and EntityTraceResult
    @Deprecated
    public boolean isBlock() {
        return this instanceof BlockTraceResult;
    }

    @Deprecated
    public Block getBlock() {
        return ((BlockTraceResult) this).getBlock();
    }

    @Deprecated
    public boolean isEntity() {
        return this instanceof EntityTraceResult;
    }

    @Deprecated
    public Entity getLivingEntity() {
        return ((EntityTraceResult) this).getEntity();
    }

    @Override
    public String toString() {
        return "RayTraceResult{" +
                "hitFace=" + hitFace +
                ", hitLocation=" + hitLocation +
                ", hitMin=" + hitMin +
                ", hitMax=" + hitMax +
                '}';
    }
}
