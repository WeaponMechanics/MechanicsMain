package me.deecaad.weaponmechanics.weapon.explode.raytrace;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.HitBox;
import me.deecaad.core.compatibility.block.BlockCompatibility;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class Ray {

    private final World world;
    private final Vector origin;
    private final Vector end;
    private final double directionLength;

    /**
     * Draws a ray from the given point <code>origin</code> in the direction <code>direction</code>. Used
     * the magnitude of <code>direction</code> to determine how far to trace.
     *
     * @param origin    The starting point of this ray
     * @param direction The direction and magnitude to trace
     */
    public Ray(@NotNull Location origin, @NotNull Vector direction) {
        if (origin.getWorld() == null)
            throw new IllegalArgumentException("World cannot be null");

        this.world = origin.getWorld();
        this.origin = origin.toVector();
        this.end = this.origin.clone().add(direction);

        this.directionLength = direction.length();
    }

    /**
     * Draws a ray between the 2 given points in the given <code>world</code>
     *
     * @param world The world in which the ray exists
     * @param start The starting point
     * @param stop  The ending point
     */
    public Ray(@NotNull World world, Vector start, Vector stop) {
        this.world = world;
        this.origin = start;
        this.end = stop;

        this.directionLength = stop.clone().subtract(start).length();
    }

    /**
     * A more "raw" constructor if you already have the direction's length. Draws a ray
     * between the 2 given points in the given <code>world</code>.
     * <p>
     * If you do not have the distance between the 2 points saved, you shouldn't use
     * this constructor
     *
     * @param world           The world in which the ray exists
     * @param origin          The starting point
     * @param end             The ending point
     * @param directionLength The distance between the 2 points
     * @see Ray#Ray(World, Vector, Vector)
     */
    public Ray(World world, Vector origin, Vector end, double directionLength) {
        this.world = world;
        this.origin = origin;
        this.end = end;
        this.directionLength = directionLength;
    }

    /**
     * Traces along this ray, colliding with block(s) and/or entity(s), depending on the given
     * <code>collision</code>.
     *
     * @param collision What the ray collides with
     * @param accuracy  The distance (in blocks) between checks
     * @return The collision data
     */
    public TraceResult trace(@NotNull TraceCollision collision, @Nonnegative double accuracy) {
        return trace(collision, accuracy, false);
    }

    /**
     * Traces along this ray, colliding with block(s) and/or entity(s), depending on the given
     * <code>collision</code>.
     *
     * @param collision What the ray collides with
     * @param accuracy  The distance (in blocks) between checks
     * @param isShow    Useful for debugging. Spawns redstone particles that are removed after 1 minute
     * @return The collision data
     */
    public TraceResult trace(@NotNull TraceCollision collision, @Nonnegative double accuracy, boolean isShow) {
        BlockCompatibility factory = CompatibilityAPI.getBlockCompatibility();

        // Store the entities between the starting and ending point of the ray. Map the
        // entities to their hit box. If this ray is too long, this method becomes
        // inefficient -- Ideally rays should be kept to 25 blocks max
        Map<Entity, HitBox> availableEntities = collision.isHitEntity()
                ? getEntities(collision)
                : Collections.emptyMap();

        // If we are checking for entities, and there are no entities that the vector
        // can hit, then we can take a shortcut and return an empty trace result, saving resources
        if (!collision.isHitBlock() && availableEntities.isEmpty()) {
            return new TraceResult(Collections.emptySet(), Collections.emptySet());
        }

        final LinkedHashSet<Block> blocks = new LinkedHashSet<>();
        final LinkedHashSet<Entity> entities = new LinkedHashSet<>();

        // Step is a number [0.0, 1.0] that defines a percentage of distance
        // between our starting and stopping point. If step exceeds 1, that
        // means the Ray is shorter than the accuracy allows.
        double step = accuracy / directionLength;
        if (step > 1.0) {
            return new TraceResult(entities, blocks);
        }

        // We start i at step to skip the first trace step. Doing this will
        // help prevent explosion rays from being blocks by minor floating
        // point differences. See issue #26.
        boolean collides = false;
        for (double i = step; i <= 1; i += step) {
            Vector point = VectorUtil.lerp(origin, end, i);
            Block block = world.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ());

            // We can collide with a block if the collision can actually effect blocks,
            // if this specific block can be hit, and if we actually collide with the block's hitbox
            if (collision.isHitBlock() && collision.canHit(block) && contains(factory.getHitBox(block), point)) {
                blocks.add(block);
                collides = true;
            }

            // We have to check if the ray collides with every nearby entity. The ray can collide
            // with an entity if the collision can actually effect entities, if the entity has not
            // yet been hit, if this specific entity can be hit, and if we actually collide the
            // entity's hitbox
            for (Map.Entry<Entity, HitBox> entry : availableEntities.entrySet()) {
                Entity entity = entry.getKey();
                HitBox hitbox = entry.getValue();

                if (collision.isHitEntity() && collision.canHit(entity) && contains(hitbox, point)) {
                    entities.add(entity);
                    collides = true;
                    if (collision.isFirst()) {
                        break;
                    }
                }
            }

            // If there was a collision, and there and the collision should only collide
            // once, then break out of the loop
            if (collides && collision.isFirst()) {
                break;
            }

            // Really helpful for debugging, simply displays a red-stone
            // particle for each point.
            if (isShow) {
                displayPoint(point, collides);
            }

            collides = false;
        }

        return new TraceResult(entities, blocks);
    }

    public Map<Entity, HitBox> getEntities(TraceCollision collision) {

        // The number 2.0 is taken from Mojang's code. It is probably big
        // enough to include entities whose hit-box is within the bounds, but
        // their actual location is not in the box.
        Vector min = VectorUtil.add(VectorUtil.min(origin, end), -2.0, -2.0, -2.0);
        Vector max = VectorUtil.add(VectorUtil.max(origin, end), 2.0, 2.0, 2.0);

        int minChunkX = (int) Math.floor(min.getX() / 16.0);
        int minChunkZ = (int) Math.floor(min.getZ() / 16.0);
        int maxChunkX = (int) Math.ceil(max.getX() / 16.0);
        int maxChunkZ = (int) Math.ceil(max.getZ() / 16.0);

        // Using Map for O(1) lookup
        Map<Entity, HitBox> temp = new HashMap<>();

        // By reusing the same location, we stop spigot from instantiating a
        // new location every time we grab an entity's location. This probably
        // saves computing power.
        Location reuse = new Location(null, 0, 0, 0);

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                Chunk chunk = world.getChunkAt(x, z);

                for (Entity entity : chunk.getEntities()) {
                    if (contains(min, max, entity.getLocation(reuse)) && collision.canHit(entity)) {
                        temp.put(entity, CompatibilityAPI.getEntityCompatibility().getHitBox(entity));
                    }
                }
            }
        }

        return temp;
    }

    private void displayPoint(Vector point, boolean collides) {
        Particle.DustOptions color = collides ? new Particle.DustOptions(Color.RED, 0.25f) : new Particle.DustOptions(Color.LIME, 0.25f);

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i++ >= 1000) {
                    cancel();
                }

                assert world != null;
                world.spawnParticle(Particle.REDSTONE, point.getX(), point.getY(), point.getZ(), 1, 0, 0, 0, 0, color, true);
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, 2);
    }

    private static boolean contains(Vector min, Vector max, Location loc) {
        return loc.getX() > min.getX() && loc.getX() < max.getX() &&
                loc.getY() > min.getY() && loc.getY() < max.getY() &&
                loc.getZ() > min.getZ() && loc.getZ() < max.getZ();
    }

    private static boolean contains(HitBox hitbox, Vector point) {
        if (hitbox == null) return false;

        return hitbox.collides(point);
    }
}
