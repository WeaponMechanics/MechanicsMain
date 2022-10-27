package me.deecaad.core.compatibility;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class RayTraceResult {

    private Block block;
    private LivingEntity livingEntity;

    private final HitBox hitBox;
    private final Vector hitLocation;
    private final double distanceTravelled;
    private final BlockFace hitFace;

    public RayTraceResult(HitBox hitBox, Vector hitLocation, double distanceTravelled, BlockFace hitFace) {
        this.hitBox = hitBox;
        this.hitLocation = hitLocation;
        this.distanceTravelled = distanceTravelled;
        this.hitFace = hitFace;
    }

    public RayTraceResult(HitBox hitBox, Vector hitLocation, double distanceTravelled, BlockFace hitFace, Block block) {
        this.hitBox = hitBox;
        this.hitLocation = hitLocation;
        this.distanceTravelled = distanceTravelled;
        this.hitFace = hitFace;
        this.block = block;
    }

    public RayTraceResult(HitBox hitBox, Vector hitLocation, double distanceTravelled, BlockFace hitFace, LivingEntity livingEntity) {
        this.hitBox = hitBox;
        this.hitLocation = hitLocation;
        this.distanceTravelled = distanceTravelled;
        this.hitFace = hitFace;
        this.livingEntity = livingEntity;
    }

    /**
     * @return the used hit box
     */
    public HitBox getHitBox() {
        return hitBox;
    }

    /**
     * @return the exact hit location
     */
    public Vector getHitLocation() {
        return hitLocation;
    }

    /**
     * @return the distance travelled during THIS ray until hit
     */
    public double getDistanceTravelled() {
        return distanceTravelled;
    }

    /**
     * @return the direction from which this hit came
     */
    public BlockFace getHitFace() {
        return hitFace;
    }

    /**
     * @return the hit block, or null
     */
    @Nullable
    public Block getBlock() {
        return block;
    }

    /**
     * @return the hit entity, or null
     */
    @Nullable
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    /**
     * @return whether this was entity hit
     */
    public boolean isEntity() {
        return livingEntity != null;
    }

    /**
     * @return whether this was block hit
     */
    public boolean isBlock() {
        return block != null;
    }


    public void outlineOnlyHitPosition(Entity player) {
        double x = hitLocation.getX();
        double y = hitLocation.getY();
        double z = hitLocation.getZ();
        if (CompatibilityAPI.getVersion() < 1.13) {
            player.getWorld().spawnParticle(Particle.CRIT, x, y, z, 1, 0, 0, 0, 0.0001);
        } else {
            player.getWorld().spawnParticle(Particle.REDSTONE, x, y, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.BLACK, 1.5f), true);
        }
    }

    @Override
    public String toString() {
        return "RayTraceResult{" +
                "block=" + block +
                ", livingEntity=" + livingEntity +
                ", hitLocation=" + hitLocation +
                ", distanceTravelled=" + distanceTravelled +
                '}';
    }
}