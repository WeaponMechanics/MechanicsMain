package me.deecaad.core.compatibility;

import me.deecaad.core.file.serializers.ColorSerializer;
import me.deecaad.core.utils.ray.BlockTraceResult;
import me.deecaad.core.utils.ray.EntityTraceResult;
import me.deecaad.core.utils.ray.RayTraceResult;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;

public class HitBox {

    private Block block;
    private LivingEntity livingEntity;
    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;

    private Collection<HitBox> voxelShape;

    public HitBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        modify(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public HitBox(Vector start, Vector end) {
        this(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
    }

    public HitBox modify(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
        return this;
    }

    public void setBlockHitBox(Block block) {
        if (livingEntity != null) throw new IllegalArgumentException("Can't set living entity for block hitbox");
        this.block = block;
    }

    public void setLivingEntity(LivingEntity livingEntity) {
        if (block != null) throw new IllegalArgumentException("Can't set block for living entity hitbox");
        this.livingEntity = livingEntity;
    }

    public void addVoxelShapePart(HitBox hitBox) {
        if (voxelShape == null) voxelShape = new ArrayList<>(2);
        voxelShape.add(hitBox);
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public double getWidth() {
        return this.maxX - this.minX;
    }

    public double getDepth() {
        return this.maxZ - this.minZ;
    }

    public double getHeight() {
        return this.maxY - this.minY;
    }

    public double getCenterX() {
        return this.minX + this.getWidth() * 0.5D;
    }

    public double getCenterY() {
        return this.minY + this.getHeight() * 0.5D;
    }

    public double getCenterZ() {
        return this.minZ + this.getDepth() * 0.5D;
    }

    public Vector getMin() {
        return new Vector(minX, minY, minZ);
    }

    public Vector getMax() {
        return new Vector(maxX, maxY, maxZ);
    }

    /**
     * Check whether point collides with this hitbox
     *
     * @param point the point to check
     * @return true if collides
     */
    public boolean collides(Vector point) {
        if (point == null) return false;
        return point.getX() >= minX
                && point.getX() <= maxX
                && point.getY() >= minY
                && point.getY() <= maxY
                && point.getZ() >= minZ
                && point.getZ() <= maxZ;
    }

    /**
     * Check whether other hitbox overlaps this one
     * @param other the other hitbox
     * @return true if overlaps
     */
    public boolean overlaps(HitBox other) {
        return this.minX < other.maxX && this.maxX > other.minX && this.minY < other.maxY && this.maxY > other.minY && this.minZ < other.maxZ && this.maxZ > other.minZ;
    }

    /**
     * Grows this hit box in all directions with given amount
     *
     * @param amount the amount to grow hit box
     * @return the grown hit box
     */
    public HitBox grow(double amount) {
        if (amount == 0) return this;
        return this.modify(minX - amount, minY - amount, minZ - amount,
                maxX + amount, maxY + amount, maxZ + amount);
    }

    /**
     * @param width the xz to grow hit box
     * @param height the y to grow hit box
     * @return the grown hit box
     */
    public HitBox grow(double width, double height) {
        width /= 2;
        return this.modify(minX - width, minY, minZ - width,
                maxX + width, maxY + height, maxZ + width);
    }

    /**
     * Uses BoundingBox class method expand(Vector, double). Easier backwards compatibility this way.
     * <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/util/BoundingBox.html#expand(org.bukkit.util.Vector,double)">...</a>
     *
     * @param direction the direction to expand
     * @param expansion the amount to expand
     * @return this hit box with expansion
     */
    public HitBox expand(Vector direction, double expansion) {
        double dirX = direction.getX(), dirY = direction.getY(), dirZ = direction.getZ();
        if (dirX == 0.0 && dirY == 0.0 && dirZ == 0.0) return this;

        double negativeX = dirX < 0.0 ? -dirX * expansion : 0.0;
        double negativeY = dirY < 0.0 ? -dirY * expansion : 0.0;
        double negativeZ = dirZ < 0.0 ? -dirZ * expansion : 0.0;
        double positiveX = dirX > 0.0 ? dirX * expansion : 0.0;
        double positiveY = dirY > 0.0 ? dirY * expansion : 0.0;
        double positiveZ = dirZ > 0.0 ? dirZ * expansion : 0.0;
        return this.expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
    }

    /**
     * Uses BoundingBox class method expand(double, double, double, double, double, double). Easier backwards compatibility this way.
     * <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/util/BoundingBox.html#expand(double,double,double,double,double,double)">...</a>
     *
     * @return this hit box with expansion
     */
    public HitBox expand(double negativeX, double negativeY, double negativeZ, double positiveX, double positiveY, double positiveZ) {
        double newMinX = this.minX - negativeX;
        double newMinY = this.minY - negativeY;
        double newMinZ = this.minZ - negativeZ;
        double newMaxX = this.maxX + positiveX;
        double newMaxY = this.maxY + positiveY;
        double newMaxZ = this.maxZ + positiveZ;
        double centerZ;
        if (newMinX > newMaxX) {
            centerZ = this.getCenterX();
            if (newMaxX >= centerZ) {
                newMinX = newMaxX;
            } else if (newMinX <= centerZ) {
                newMaxX = newMinX;
            } else {
                newMinX = centerZ;
                newMaxX = centerZ;
            }
        }

        if (newMinY > newMaxY) {
            centerZ = this.getCenterY();
            if (newMaxY >= centerZ) {
                newMinY = newMaxY;
            } else if (newMinY <= centerZ) {
                newMaxY = newMinY;
            } else {
                newMinY = centerZ;
                newMaxY = centerZ;
            }
        }

        if (newMinZ > newMaxZ) {
            centerZ = this.getCenterZ();
            if (newMaxZ >= centerZ) {
                newMinZ = newMaxZ;
            } else if (newMinZ <= centerZ) {
                newMaxZ = newMinZ;
            } else {
                newMinZ = centerZ;
                newMaxZ = centerZ;
            }
        }

        return this.modify(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    /**
     * @param location the start location of ray
     * @param normalizedMotion the normalized direction
     * @return the ray trace result or null if there is no hit
     */
    public RayTraceResult rayTrace(Vector location, Vector normalizedMotion) {
        RayTraceResult mainBoxHit = ray(location, normalizedMotion);

        // Voxel shape not used or didn't hit main hitbox
        if (voxelShape == null || mainBoxHit == null) return mainBoxHit;

        // Here we know main hitbox was hit, now check all voxel shapes
        RayTraceResult hit = null;
        double closestHit = -1;
        for (HitBox boxPart : voxelShape) {

            if (mainBoxHit instanceof BlockTraceResult blockHit) {
                boxPart.setBlockHitBox(blockHit.getBlock());
            } else if (mainBoxHit instanceof EntityTraceResult entityHit) {
                boxPart.setLivingEntity(entityHit.getEntity());
            }

            RayTraceResult boxPartHit = boxPart.ray(location, normalizedMotion);
            if (boxPartHit == null) continue;

            // Only closest hit
            if (closestHit == -1 || boxPartHit.getHitMin() < closestHit) {
                closestHit = boxPartHit.getHitMin();
                hit = boxPartHit;
            }
        }

        return hit;
    }

    /**
     * Uses BoundingBox class method rayTrace(Vector, Vector, double) with slight modifications. Easier backwards compatibility this way.
     * <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/util/BoundingBox.html#rayTrace(org.bukkit.util.Vector,org.bukkit.util.Vector,double)">...</a>
     */
    private RayTraceResult ray(Vector location, Vector normalizedMotion) {

        double startX = location.getX();
        double startY = location.getY();
        double startZ = location.getZ();

        double dirX = normalizedMotion.getX();
        double dirY = normalizedMotion.getY();
        double dirZ = normalizedMotion.getZ();

        double divX = 1.0 / dirX;
        double divY = 1.0 / dirY;
        double divZ = 1.0 / dirZ;

        double tMin, tMax, tyMin, tyMax;
        BlockFace hitBlockFaceMin, hitBlockFaceMax, hitBlockFaceYMin, hitBlockFaceYMax;

        // x
        if (dirX >= 0.0) {
            tMin = (this.minX - startX) * divX;
            tMax = (this.maxX - startX) * divX;
            hitBlockFaceMin = BlockFace.WEST;
            hitBlockFaceMax = BlockFace.EAST;
        } else {
            tMin = (this.maxX - startX) * divX;
            tMax = (this.minX - startX) * divX;
            hitBlockFaceMin = BlockFace.EAST;
            hitBlockFaceMax = BlockFace.WEST;
        }

        // y
        if (dirY >= 0.0) {
            tyMin = (this.minY - startY) * divY;
            tyMax = (this.maxY - startY) * divY;
            hitBlockFaceYMin = BlockFace.DOWN;
            hitBlockFaceYMax = BlockFace.UP;
        } else {
            tyMin = (this.maxY - startY) * divY;
            tyMax = (this.minY - startY) * divY;
            hitBlockFaceYMin = BlockFace.UP;
            hitBlockFaceYMax = BlockFace.DOWN;
        }
        if ((tMin > tyMax) || (tMax < tyMin)) {
            return null;
        }
        if (tyMin > tMin) {
            tMin = tyMin;
            hitBlockFaceMin = hitBlockFaceYMin;
        }
        if (tyMax < tMax) {
            tMax = tyMax;
            hitBlockFaceMax = hitBlockFaceYMax;
        }

        // z
        double tzMin, tzMax;
        BlockFace hitBlockFaceZMin, hitBlockFaceZMax;
        if (dirZ >= 0.0) {
            tzMin = (this.minZ - startZ) * divZ;
            tzMax = (this.maxZ - startZ) * divZ;
            hitBlockFaceZMin = BlockFace.NORTH;
            hitBlockFaceZMax = BlockFace.SOUTH;
        } else {
            tzMin = (this.maxZ - startZ) * divZ;
            tzMax = (this.minZ - startZ) * divZ;
            hitBlockFaceZMin = BlockFace.SOUTH;
            hitBlockFaceZMax = BlockFace.NORTH;
        }
        if ((tMin > tzMax) || (tMax < tzMin)) {
            return null;
        }
        if (tzMin > tMin) {
            tMin = tzMin;
            hitBlockFaceMin = hitBlockFaceZMin;
        }
        if (tzMax < tMax) {
            tMax = tzMax;
            hitBlockFaceMax = hitBlockFaceZMax;
        }

        if (tMax < 0.0) return null;

        if (block != null) {
            return new BlockTraceResult(location, normalizedMotion, this, hitBlockFaceMin, hitBlockFaceMax, tMin, tMax, block);
        } else if (livingEntity == null) {
            // When not entity or block hitbox
            return new RayTraceResult(location, normalizedMotion, this, hitBlockFaceMin, hitBlockFaceMax, tMin, tMax);
        }

        return new EntityTraceResult(location, normalizedMotion, this,  hitBlockFaceMin, hitBlockFaceMax, tMin, tMax, livingEntity);
    }

    public void outlineAllBoxes(Entity player) {
        if (voxelShape != null) {
            outlineMainBox(player, Color.BLACK);
            ColorSerializer.ColorType[] colors = ColorSerializer.ColorType.values();

            int i = 1;
            for (HitBox voxel : voxelShape) {
                voxel.outlineMainBox(player, colors[i].getBukkitColor());
                if (++i >= colors.length) {
                    i = 1;
                }
            }
        } else {
            outlineMainBox(player, Color.BLACK);
        }
    }

    public void outlineMainBox(Entity player, Color color) {
        double step = 0.05;
        for (double x = minX; x <= maxX; x += step) {
            for (double y = minY; y <= maxY; y += step) {
                for (double z = minZ; z <= maxZ; z += step) {
                    int components = 0;
                    if (x == minX || x + step > maxX) components++;
                    if (y == minY || y + step > maxY) components++;
                    if (z == minZ || z + step > maxZ) components++;
                    if (components >= 2) {
                        if (CompatibilityAPI.getVersion() < 1.13) {
                            player.getWorld().spawnParticle(Particle.CRIT, x, y, z, 1, 0, 0, 0, 0.0001);
                        } else {
                            player.getWorld().spawnParticle(Particle.REDSTONE, x, y, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(color, 0.5f), true);
                        }
                    }
                }
            }
        }
    }

    public HitBox cloneDimensions() {
        return new HitBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}