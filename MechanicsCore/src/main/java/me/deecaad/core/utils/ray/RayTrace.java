package me.deecaad.core.utils.ray;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.HitBox;
import me.deecaad.core.utils.NumberUtil;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class RayTrace {

    private boolean disableEntityChecks;
    private boolean disableBlockChecks;
    private Predicate<LivingEntity> entityFilter;
    private Predicate<Block> blockFilter;
    private Entity entity;
    private boolean outlineHitPosition;
    private boolean outlineHitBox;
    private boolean allowLiquid;
    private double raySize = 0.1;

    public RayTrace() { }

    public RayTrace disableEntityChecks() {
        this.disableEntityChecks = true;
        return this;
    }

    public RayTrace disableBlockChecks() {
        this.disableBlockChecks = true;
        return this;
    }

    public RayTrace withEntityFilter(Predicate<LivingEntity> entityFilter) {
        this.entityFilter = entityFilter;
        return this;
    }

    public RayTrace withBlockFilter(Predicate<Block> blockFilter) {
        this.blockFilter = blockFilter;
        return this;
    }

    public RayTrace withOutlineHitPosition(Entity entity) {
        this.outlineHitPosition = true;
        this.entity = entity;
        return this;
    }

    public RayTrace withOutlineHitBox(Entity entity) {
        this.outlineHitBox = true;
        this.entity = entity;
        return this;
    }

    public RayTrace enableLiquidChecks() {
        this.allowLiquid = true;
        return this;
    }

    public RayTrace withRaySize(double size) {
        this.raySize = size;
        return this;
    }

    public List<RayTraceResult> cast(World world, Vector start, Vector direction, double range) {
        return cast(world, start, start.clone().add(direction.clone().multiply(range)), direction);
    }

    public List<RayTraceResult> cast(World world, Vector start, Vector end) {
        return cast(world, start, end, end.clone().subtract(start).normalize());
    }

    public List<RayTraceResult> cast(World world, Vector start, Vector end, Vector direction) {
        return cast(world, start, end, direction, 0);
    }

    public List<RayTraceResult> cast(World world, Vector start, Vector end, Vector direction, double maximumBlockThrough) {
        List<RayTraceResult> hits = new ArrayList<>(5);
        getBlockHits(hits, world, start, end, direction, maximumBlockThrough);
        getEntityHits(hits, world, start, end, direction);

        if (!hits.isEmpty()) {

            // If more than 1 hit, sort based on distance travelled (lowest to highest)
            if (hits.size() > 1) hits.sort(Comparator.comparingDouble(RayTraceResult::getHitMin));

            if (this.outlineHitPosition) hits.get(0).outlineOnlyHitPosition(entity);
            if (this.outlineHitBox) {
                RayTraceResult firstHit = hits.get(0);
                if (firstHit instanceof BlockTraceResult blockHit) {
                    CompatibilityAPI.getBlockCompatibility().getHitBox(blockHit.getBlock()).outlineAllBoxes(entity);
                } else if (firstHit instanceof EntityTraceResult entityHit) {
                    HitBox entityBox = CompatibilityAPI.getEntityCompatibility().getHitBox(entityHit.getEntity());
                    entityBox.grow(raySize);
                    entityBox.outlineAllBoxes(entity);
                }
            }
            return hits;
        }

        return null;
    }

    private void getBlockHits(List<RayTraceResult> hits, World world, Vector start, Vector end, Vector direction, double maximumBlockThrough) {
        if (this.disableBlockChecks) return;

        // Method based on NMS block traversing

        double startX = NumberUtil.lerp(start.getX(), end.getX(), -1.0E-7);
        double startY = NumberUtil.lerp(start.getY(), end.getY(), -1.0E-7);
        double startZ = NumberUtil.lerp(start.getZ(), end.getZ(), -1.0E-7);

        int currentX = NumberUtil.intFloor(startX);
        int currentY = NumberUtil.intFloor(startY);
        int currentZ = NumberUtil.intFloor(startZ);

        Block startBlock = world.getBlockAt(currentX, currentY, currentZ);
        RayTraceResult rayStartBlock = rayBlock(startBlock, start, direction);
        if (rayStartBlock != null) {
            hits.add(rayStartBlock);

            // Don't count liquid as actual hits along the path
            if (!allowLiquid || !startBlock.isLiquid()) {
                if (maximumBlockThrough != -1.0 && (maximumBlockThrough -= rayStartBlock.getThroughDistance()) < 0) return;
            }
        }

        double endX = NumberUtil.lerp(end.getX(), start.getX(), -1.0E-7);
        double endY = NumberUtil.lerp(end.getY(), start.getY(), -1.0E-7);
        double endZ = NumberUtil.lerp(end.getZ(), start.getZ(), -1.0E-7);

        double directionX = endX - startX;
        double directionY = endY - startY;
        double directionZ = endZ - startZ;
        int blockX = NumberUtil.sign(directionX);
        int blockY = NumberUtil.sign(directionY);
        int blockZ = NumberUtil.sign(directionZ);
        double addX = blockX == 0 ? Double.MAX_VALUE : (double) blockX / directionX;
        double addY = blockY == 0 ? Double.MAX_VALUE : (double) blockY / directionY;
        double addZ = blockZ == 0 ? Double.MAX_VALUE : (double) blockZ / directionZ;
        double maxX = addX * (blockX > 0 ? 1.0 - NumberUtil.frac(startX) : NumberUtil.frac(startX));
        double maxY = addY * (blockY > 0 ? 1.0 - NumberUtil.frac(startY) : NumberUtil.frac(startY));
        double maxZ = addZ * (blockZ > 0 ? 1.0 - NumberUtil.frac(startZ) : NumberUtil.frac(startZ));

        while (maximumBlockThrough > -1) {
            if (maxX > 1.0 && maxY > 1.0 && maxZ > 1.0) {
                break;
            }

            if (maxX < maxY) {
                if (maxX < maxZ) {
                    currentX += blockX;
                    maxX += addX;
                } else {
                    currentZ += blockZ;
                    maxZ += addZ;
                }
            } else if (maxY < maxZ) {
                currentY += blockY;
                maxY += addY;
            } else {
                currentZ += blockZ;
                maxZ += addZ;
            }

            Block newBlock = world.getBlockAt(currentX, currentY, currentZ);
            RayTraceResult rayNewBlock = rayBlock(newBlock, start, direction);
            if (rayNewBlock != null) {
                hits.add(rayNewBlock);

                // Don't count liquid as actual hits along the path
                if (!allowLiquid || !newBlock.isLiquid()) {
                    if (--maximumBlockThrough < 0) break;
                }

            }
        }
    }

    private RayTraceResult rayBlock(Block block, Vector start, Vector direction) {
        if (blockFilter != null && blockFilter.test(block)) return null;

        HitBox blockBox = CompatibilityAPI.getBlockCompatibility().getHitBox(block, allowLiquid);
        if (blockBox == null) return null;

        return blockBox.rayTrace(start, direction);
    }

    private void getEntityHits(List<RayTraceResult> hits, World world, Vector start, Vector end, Vector direction) {
        if (this.disableEntityChecks) return;
        HitBox hitBox = new HitBox(start, end);

        int minX = NumberUtil.intFloor((hitBox.getMinX() - 2.0) / 16.0);
        int maxX = NumberUtil.intFloor((hitBox.getMaxX() + 2.0) / 16.0);
        int minZ = NumberUtil.intFloor((hitBox.getMinZ() - 2.0) / 16.0);
        int maxZ = NumberUtil.intFloor((hitBox.getMaxZ() + 2.0) / 16.0);

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                Chunk chunk = world.getChunkAt(x, z);
                for (final Entity entity : chunk.getEntities()) {
                    RayTraceResult rayNewEntity = rayEntity(hitBox, entity, start, direction);
                    if (rayNewEntity != null) {
                        hits.add(rayNewEntity);
                    }
                }
            }
        }
    }

    private RayTraceResult rayEntity(HitBox hitBox, Entity entity, Vector start, Vector direction) {
        if (!entity.getType().isAlive()) return null;
        if (entityFilter != null && entityFilter.test((LivingEntity) entity)) return null;

        HitBox entityBox = CompatibilityAPI.getEntityCompatibility().getHitBox(entity);
        if (entityBox == null) return null;

        entityBox.grow(raySize);
        if (!hitBox.overlaps(entityBox)) return null;

        return entityBox.rayTrace(start, direction);
    }
}