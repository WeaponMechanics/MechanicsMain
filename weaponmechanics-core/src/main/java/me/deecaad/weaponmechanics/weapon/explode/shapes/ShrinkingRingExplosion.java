package me.deecaad.weaponmechanics.weapon.explode.shapes;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An explosion shape that walks a shrinking sphere outward along each of a ring of rays. The ring
 * lies in the plane perpendicular to a locally-computed "up" axis (the terrain normal at the
 * origin), so the resulting crater follows the surface flat on flat ground, tilted on slopes,
 * inverted on ceilings.
 * <p>
 * For each ray, a sphere starts at the origin and steps outward by {@code stepDistance} per
 * iteration while its radius decreases by {@code shrinkPerStep}. Once the radius hits zero, that
 * ray stops. The result is a coherent disk-shaped crater with a clear origin and rigid edges.
 */
public class ShrinkingRingExplosion implements ExplosionShape {

    private static final int TERRAIN_SAMPLE_RADIUS = 3;

    private double initialRadius;
    private double shrinkPerStep;
    private double stepDistance;
    private int rayCount;
    private boolean autoOrient;

    /**
     * Default constructor for serializer.
     */
    public ShrinkingRingExplosion() {
    }

    public ShrinkingRingExplosion(double initialRadius, double shrinkPerStep, double stepDistance, int rayCount, boolean autoOrient) {
        this.initialRadius = initialRadius;
        this.shrinkPerStep = shrinkPerStep;
        this.stepDistance = stepDistance;
        this.rayCount = rayCount;
        this.autoOrient = autoOrient;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey(WeaponMechanics.getInstance(), "shrinking_ring");
    }

    @Override
    public @NotNull List<Block> getBlocks(@NotNull Location origin) {
        World world = origin.getWorld();
        if (world == null)
            throw new IllegalArgumentException("origin world is null");

        Vector up = autoOrient ? computeTerrainNormal(origin) : new Vector(0, 1, 0);

        // Build an orthonormal basis (right, forward) in the plane perpendicular to up.
        Vector right = up.getCrossProduct(new Vector(0, 1, 0));
        if (right.lengthSquared() < 1.0e-6) {
            right = up.getCrossProduct(new Vector(1, 0, 0));
        }
        right.normalize();
        Vector forward = up.getCrossProduct(right).normalize();

        double rx = right.getX(), ry = right.getY(), rz = right.getZ();
        double fx = forward.getX(), fy = forward.getY(), fz = forward.getZ();
        double ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();

        LongSet seen = new LongOpenHashSet(4096);
        List<Block> blocks = new ArrayList<>();

        for (int i = 0; i < rayCount; i++) {
            double angle = 2.0 * Math.PI * i / rayCount;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double dx = rx * cos + fx * sin;
            double dy = ry * cos + fy * sin;
            double dz = rz * cos + fz * sin;

            double radius = initialRadius;
            double travelled = 0.0;
            while (radius > 0.0) {
                double cxd = ox + dx * travelled;
                double cyd = oy + dy * travelled;
                double czd = oz + dz * travelled;
                collectSphere(world, cxd, cyd, czd, radius, seen, blocks);
                travelled += stepDistance;
                radius -= shrinkPerStep;
            }
        }

        return blocks;
    }

    private void collectSphere(@NotNull World world, double cx, double cy, double cz, double radius,
                               @NotNull LongSet seen, @NotNull List<Block> out) {
        double radiusSquared = radius * radius;
        int r = (int) Math.ceil(radius);
        int icx = (int) Math.floor(cx);
        int icy = (int) Math.floor(cy);
        int icz = (int) Math.floor(cz);

        for (int x = icx - r; x <= icx + r; x++) {
            double bx = (x + 0.5) - cx;
            double bxSq = bx * bx;
            if (bxSq > radiusSquared)
                continue;

            for (int y = icy - r; y <= icy + r; y++) {
                double by = (y + 0.5) - cy;
                double bxySq = bxSq + by * by;
                if (bxySq > radiusSquared)
                    continue;

                for (int z = icz - r; z <= icz + r; z++) {
                    double bz = (z + 0.5) - cz;
                    if (bxySq + bz * bz > radiusSquared)
                        continue;

                    if (seen.add(packBlockKey(x, y, z))) {
                        out.add(world.getBlockAt(x, y, z));
                    }
                }
            }
        }
    }

    // Packs (x, y, z) into one long for dedup. 26 bits each for x/z (range
    // [-33M, +33M]), 12 bits for y (range [-2048, +2048]).
    private static long packBlockKey(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF)) << 38
            | ((long) (z & 0x3FFFFFF)) << 12
            | (y & 0xFFFL);
    }

    /**
     * Computes a unit vector pointing away from the local solid mass around {@code origin}. For
     * each empty (air) block in a small cube around the origin, the unit vector toward that block
     * is accumulated; the normalized sum gives the local terrain normal. Falls back to world up
     * when the origin is fully buried or floating in open air.
     */
    private @NotNull Vector computeTerrainNormal(@NotNull Location origin) {
        World world = origin.getWorld();
        Vector normal = new Vector();
        int cx = origin.getBlockX();
        int cy = origin.getBlockY();
        int cz = origin.getBlockZ();

        for (int dx = -TERRAIN_SAMPLE_RADIUS; dx <= TERRAIN_SAMPLE_RADIUS; dx++) {
            for (int dy = -TERRAIN_SAMPLE_RADIUS; dy <= TERRAIN_SAMPLE_RADIUS; dy++) {
                for (int dz = -TERRAIN_SAMPLE_RADIUS; dz <= TERRAIN_SAMPLE_RADIUS; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0)
                        continue;

                    Block block = world.getBlockAt(cx + dx, cy + dy, cz + dz);
                    if (!block.isEmpty())
                        continue;

                    double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    normal.add(new Vector(dx / len, dy / len, dz / len));
                }
            }
        }

        if (normal.lengthSquared() < 1.0e-6)
            return new Vector(0, 1, 0);

        return normal.normalize();
    }

    @Override
    public @NotNull List<LivingEntity> getEntities(@NotNull Location origin) {
        double max = getMaxDistance();
        double maxSquared = max * max;
        return origin.getWorld().getLivingEntities()
            .stream()
            .filter(entity -> entity.getLocation().distanceSquared(origin) < maxSquared)
            .collect(Collectors.toList());
    }

    @Override
    public double getMaxDistance() {
        // floor(initialRadius / shrinkPerStep) is the number of full steps before radius hits 0.
        // The last sphere's edge extends initialRadius beyond its center (conservative).
        int steps = (int) Math.floor(initialRadius / shrinkPerStep);
        return stepDistance * steps + initialRadius;
    }

    @Override
    public boolean isContained(@NotNull Location origin, @NotNull Location point) {
        double max = getMaxDistance();
        return origin.distanceSquared(point) < max * max;
    }

    @Override
    public double getArea() {
        // Rough estimate: a flat disk of radius getMaxDistance() with thickness initialRadius.
        double max = getMaxDistance();
        return Math.PI * max * max * initialRadius;
    }

    @Override
    public @NotNull ExplosionShape serialize(@NotNull SerializeData data) throws SerializerException {
        double initialRadius = data.of("Initial_Radius").assertExists().assertRange(0.0, null).getDouble().getAsDouble();
        double shrinkPerStep = data.of("Shrink_Per_Step").assertExists().assertRange(1.0e-6, null).getDouble().getAsDouble();
        double stepDistance = data.of("Step_Distance").assertRange(1.0e-6, null).getDouble().orElse(initialRadius);
        int rayCount = data.of("Ray_Count").assertRange(1, null).getInt().orElse(24);
        boolean autoOrient = data.of("Auto_Orient").getBool().orElse(true);

        return new ShrinkingRingExplosion(initialRadius, shrinkPerStep, stepDistance, rayCount, autoOrient);
    }

    @Override
    public String toString() {
        return "ShrinkingRingExplosion{" +
            "initialRadius=" + initialRadius +
            ", shrinkPerStep=" + shrinkPerStep +
            ", stepDistance=" + stepDistance +
            ", rayCount=" + rayCount +
            ", autoOrient=" + autoOrient +
            '}';
    }
}
