package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.weaponcompatibility.projectile.HitBox;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Class to hold collision data information
 */
public class CollisionData {

    private final Vector hitLocation;
    private final HitBox hitBox;
    private Block block;
    private BlockFace blockFace;
    private LivingEntity livingEntity;
    private final long hitTime;

    public CollisionData(HitBox hitBox, Vector hitLocation, LivingEntity livingEntity) {
        this.hitBox = hitBox;
        this.hitLocation = hitLocation;
        this.livingEntity = livingEntity;
        this.hitTime = System.currentTimeMillis();
    }

    public CollisionData(HitBox hitBox, Vector hitLocation, Block block) {
        this.hitBox = hitBox;
        this.hitLocation = hitLocation;
        this.block = block;
        this.hitTime = System.currentTimeMillis();
    }

    /**
     * Returns the <code>HitBox</code> of the <code>Entity</code> or <code>Block</code>
     * that was hit
     *
     * @return the hit hitbox
     */
    public HitBox getHitBox() {
        return hitBox;
    }

    /**
     * Returns the (x, y, z) coordinates that were hit. Note that this is
     * not 100% accurate. The larger the hitbox of the projectile, the less
     * accurate this method is
     *
     * @return The point that was hit by ray tracing
     */
    public Vector getHitLocation() {
        return hitLocation;
    }

    /**
     * Returns the block that was hit, or null
     *
     * @return The hit block
     */
    @Nullable
    public Block getBlock() {
        return block;
    }

    /**
     * Returns the living entity that was hit, or null
     *
     * @return The hit entity
     */
    @Nullable
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    /**
     * Gets the time (In milliseconds) that the projectile hit the hitbox
     *
     * @see System#currentTimeMillis()
     *
     * @return The hit time
     */
    public long getHitTime() {
        return hitTime;
    }

    /**
     * Gets the <code>BlockFace</code> that was hit. Note: Since this method
     * relies on <code>getHitLocation</code> to find the hit block face, this
     * method is not 100% accurate.
     *
     * @return The hit block face
     */
    public BlockFace getBlockFace() {
        if (blockFace == null) {
            Vector relative = hitLocation.clone().subtract(hitBox.min);
            double x = relative.getX();
            double y = relative.getY();
            double z = relative.getZ();

            double temp = Double.MAX_VALUE;
            if (x < temp) {
                temp = x;
                blockFace = BlockFace.WEST;
            }
            if (hitBox.getWidth() - x < temp) {
                temp = hitBox.getWidth() - x;
                blockFace = BlockFace.EAST;
            }
            if (y < temp) {
                temp = y;
                blockFace = BlockFace.DOWN;
            }
            if (hitBox.getHeight() - y < temp) {
                temp = hitBox.getHeight() - y;
                blockFace = BlockFace.UP;
            }
            if (z < temp) {
                temp = z;
                blockFace = BlockFace.NORTH;
            }
            if (hitBox.getDepth() - z < temp) {
                blockFace = BlockFace.SOUTH;
            }
        }

        return blockFace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollisionData that = (CollisionData) o;
        return Objects.equals(block, that.block) &&
                Objects.equals(livingEntity, that.livingEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, livingEntity);
    }
}