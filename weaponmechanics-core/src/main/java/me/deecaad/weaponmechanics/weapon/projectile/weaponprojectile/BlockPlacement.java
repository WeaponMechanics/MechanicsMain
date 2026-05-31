package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.ray.BlockTraceResult;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BlockPlacement implements Serializer<BlockPlacement> {

    public enum PlacementMode {
        PLACE_ADJACENT,
        REPLACE,
        REPLACE_AIR,
        REMOVE
    }

    private Material block;
    private PlacementMode mode;
    private int removeAfterTicks;
    private boolean lineOfSight;
    private double radius;
    private double chance;

    /**
     * Default constructor for serializer
     */
    public BlockPlacement() {
    }

    public BlockPlacement(Material block, PlacementMode mode, int removeAfterTicks, boolean lineOfSight, double radius, double chance) {
        this.block = block;
        this.mode = mode;
        this.removeAfterTicks = removeAfterTicks;
        this.lineOfSight = lineOfSight;
        this.radius = radius;
        this.chance = chance;
    }

    public void handleBlockPlacement(BlockTraceResult result, WeaponProjectile projectile) {
        Block target;
        if (mode == PlacementMode.PLACE_ADJACENT) {
            target = result.getBlock().getRelative(result.getHitFace());
            // Only place in air, liquids, or passable blocks (grass, flowers, etc.)
            Material targetType = target.getType();
            if (!targetType.isAir() && !target.isLiquid() && !target.isPassable()) {
                return;
            }
        } else {
            target = result.getBlock();
        }

        BlockState oldState = target.getState();
        Material placeMaterial = (mode == PlacementMode.REMOVE) ? Material.AIR : block;
        target.setType(placeMaterial);

        if (removeAfterTicks > 0) {
            WeaponMechanics.getInstance().getFoliaScheduler().global().runDelayed(() -> {
                oldState.update(true, true);
            }, (long) removeAfterTicks);
        }
    }

    /**
     * Handles block placement for an explosion. Iterates all blocks in the explosion radius
     * and places/replaces/removes blocks according to the configured mode.
     *
     * <ul>
     *   <li>{@link PlacementMode#REPLACE} — replaces every block in the radius with the configured material.</li>
     *   <li>{@link PlacementMode#REPLACE_AIR} — replaces only air/passable blocks (e.g. place fire in open space).</li>
     *   <li>{@link PlacementMode#REMOVE} — sets every block in the radius to AIR.</li>
     *   <li>{@link PlacementMode#PLACE_ADJACENT} — not applicable to explosions; call is ignored.</li>
     * </ul>
     *
     * @param origin the explosion origin, used for line-of-sight checks when {@code lineOfSight} is enabled
     * @param blocks the list of blocks inside the explosion radius
     */
    public void handleExplosionPlacement(@Nullable Location origin, List<Block> blocks) {
        if (mode == PlacementMode.PLACE_ADJACENT)
            return;

        Material placeMaterial = (mode == PlacementMode.REMOVE) ? Material.AIR : block;
        List<BlockState> oldStates = removeAfterTicks > 0 ? new ArrayList<>() : null;

        double radiusSq = radius > 0 ? radius * radius : -1;
        Vector originVec = origin != null ? origin.toVector() : null;

        for (Block b : blocks) {
            if (mode == PlacementMode.REPLACE_AIR && !b.getType().isAir() && !b.isPassable())
                continue;

            // Radius check: skip blocks outside the specified placement radius
            if (radiusSq > 0 && originVec != null) {
                Vector blockCenter = b.getLocation().add(0.5, 0.5, 0.5).toVector();
                if (blockCenter.distanceSquared(originVec) > radiusSq)
                    continue;
            }

            // Line-of-sight check: skip blocks that have a solid wall between them and the origin.
            // Uses Bukkit's DDA raytracer + 3 sample points within the candidate block to defeat
            // hairline gaps where a single ray could slip through diagonally-placed blocks.
            if (lineOfSight && origin != null) {
                if (isLineOfSightBlocked(origin, b))
                    continue;
            }

            // Roll the per-block placement chance (default 1.0 = always place)
            if (chance < 1.0 && ThreadLocalRandom.current().nextDouble() >= chance)
                continue;

            if (oldStates != null)
                oldStates.add(b.getState());

            b.setType(placeMaterial);
        }

        if (oldStates != null && !oldStates.isEmpty()) {
            WeaponMechanics.getInstance().getFoliaScheduler().global().runDelayed(() -> {
                for (BlockState state : oldStates)
                    state.update(true, true);
            }, (long) removeAfterTicks);
        }
    }

    /**
     * Returns true if the origin does not have a clear line of sight to the candidate block.
     * Casts 3 rays from {@code origin} to 3 different sample points spread inside the candidate
     * block — defeats the rare case where a single ray slips through the corner where 4
     * diagonally-placed blocks meet (a mathematically thin gap that DDA can ignore).
     */
    private boolean isLineOfSightBlocked(Location origin, Block candidate) {
        Vector originVec = origin.toVector();
        // Three sample points spread in 3D inside the block
        double[][] samples = {
            {0.5, 0.5, 0.5},
            {0.25, 0.75, 0.25},
            {0.75, 0.25, 0.75}
        };

        for (double[] s : samples) {
            Vector target = candidate.getLocation().add(s[0], s[1], s[2]).toVector();
            Vector dir = target.clone().subtract(originVec);
            double dist = dir.length();
            if (dist <= 0)
                continue;

            RayTraceResult hit = origin.getWorld().rayTraceBlocks(
                origin, dir.normalize(), dist, FluidCollisionMode.NEVER, true);
            if (hit == null)
                continue; // this ray reached the candidate freely

            // Something solid is in the way — only acceptable if it's the candidate itself
            // (REPLACE mode where the target block is solid)
            Block hitBlock = hit.getHitBlock();
            if (hitBlock == null || hitBlock.getX() != candidate.getX()
                || hitBlock.getY() != candidate.getY() || hitBlock.getZ() != candidate.getZ()) {
                return true; // wall in the way for at least one of the rays → block is occluded
            }
        }
        return false;
    }

    @Override
    public String getKeyword() {
        return "Block_Placement";
    }

    @Override
    public @NotNull BlockPlacement serialize(@NotNull SerializeData data) throws SerializerException {
        PlacementMode mode = data.of("Mode").getEnum(PlacementMode.class).orElse(PlacementMode.PLACE_ADJACENT);

        Material block = null;
        if (mode != PlacementMode.REMOVE) {
            block = data.of("Block").assertExists().getEnum(Material.class).get();
            if (!block.isBlock()) {
                throw data.exception("Block", "'" + block.name() + "' is not a placeable block material",
                    "Use a solid block like STONE, SPONGE, DIRT, etc.");
            }
        }

        int removeAfterTicks = data.of("Remove_After_Ticks").getInt().orElse(-1);
        boolean lineOfSight = data.of("Line_Of_Sight").getBool().orElse(false);
        double radius = data.of("Radius").getDouble().orElse(-1.0);
        double chance = data.of("Chance").getDouble().orElse(1.0);
        if (chance < 0.0 || chance > 1.0) {
            throw data.exception("Chance", "'Chance' must be between 0.0 and 1.0 (got " + chance + ")",
                "0.0 = never place, 1.0 = always place. Use a value like 0.5 for a 50% chance per block.");
        }

        return new BlockPlacement(block, mode, removeAfterTicks, lineOfSight, radius, chance);
    }
}
