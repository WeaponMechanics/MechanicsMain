package me.deecaad.weaponmechanics.weapon.explode.raytrace;

import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponcompatibility.projectile.HitBox;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Ray {

    private static final double FINE_ACCURACY = 0.03125;
    private static Map<Vector, Vector> normalizedVectorMap = new HashMap<>(6);
    private static final double[] ACCURACIES = new double[]{
            0.03,
            0.1,
            0.3
    };

    static {
        a(new Vector(FINE_ACCURACY, 0.0, 0.0));
        a(new Vector(-FINE_ACCURACY, 0.0, 0.0));
        a(new Vector(0.0, FINE_ACCURACY, 0.0));
        a(new Vector(0.0, -FINE_ACCURACY, 0.0));
        a(new Vector(0.0, 0.0, FINE_ACCURACY));
        a(new Vector(0.0, 0.0, -FINE_ACCURACY));

        for (Vector vector : normalizedVectorMap.keySet()) {
            Vector normal = normalizedVectorMap.get(vector);

            debug.warn(vector + " --> " + normal);
        }
    }

    private static void a(Vector vector) {
        Vector normal = vector.clone().multiply(-1).normalize();
        normalizedVectorMap.put(vector, normal);
    }

    private final Vector origin;
    private final Vector direction;
    private final double directionLength;
    private final World world;

    public Ray(@Nonnull Vector origin, @Nonnull Vector direction, @Nonnull World world) {
        this.origin = origin;
        this.direction = direction;
        this.directionLength = direction.length();
        this.world = world;

        this.direction.multiply(1.0 / directionLength);
    }

    /**
     * Gets the point on the ray "at" block away
     * from the origin.
     *
     * Note: If "at" > "directionLength", there is likely a logic error
     *
     * @param at How far away from the origin to get the vector
     * @return Vector at the point
     */
    private Vector getPoint(double at) {
        if (at > directionLength) debug.warn("at > directionLength in class Ray. at: " + at);

        return origin.clone().add(direction.clone().multiply(at));
    }

    public TraceResult trace(@Nonnull TraceCollision collision, @Nonnegative double accuracy) {

        Location loc = origin.toLocation(world);
        Map<Entity, HitBox> availableEntities = null;

        // Fill map with entities within radius
        if (collision.isEntity()) {
            availableEntities = new HashMap<>();

            for (Entity entity : world.getNearbyEntities(loc, directionLength, directionLength, directionLength)) {
                HitBox box = WeaponCompatibilityAPI.getProjectileCompatibility().getHitBox(entity);
                availableEntities.put(entity, box);
            }
        }

        // Check if the map is empty
        if (collision.isEntity() && !collision.isBlock() && availableEntities.isEmpty()) {
            return new TraceResult(new LinkedHashSet<>(0), null);
        }

        LinkedHashSet<Entity> entities = new LinkedHashSet<>();
        LinkedHashSet<Block> blocks = new LinkedHashSet<>();

        debug.debug("Tracing " + directionLength + " blocks by " + accuracy);
        main:
        for (double i = 0; i < directionLength; i += accuracy) {

            Vector point = getPoint(i);

            // Only do block calculations if needed
            if (collision.isBlock()) {
                Block block = world.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ());

                // Filter out air blocks
                if (!block.isEmpty()) {

                    // Check to see if the point is inside the block's hitbox
                    HitBox hitBox = WeaponCompatibilityAPI.getProjectileCompatibility().getHitBox(block);
                    if (contains(hitBox, point)) {
                        blocks.add(block);

                        // Break if the trace only needs to find 1 block
                        if (collision.isFirst()) {
                            break;
                        }
                    }
                }
            }

            // Only do entity calculations if needed
            if (collision.isEntity()) {
                for (Map.Entry<Entity, HitBox> entry: availableEntities.entrySet()) {
                    Entity entity = entry.getKey();
                    HitBox hitbox = entry.getValue();

                    if (contains(hitbox, point)) {
                        entities.add(entity);

                        if (collision.isFirst()) {
                            break main;
                        }
                    }
                }
            }
        }

        // Return the data
        return new TraceResult(entities, blocks);
    }

    private boolean contains(HitBox hitbox, Vector point) {
        if (hitbox == null) return false;

        double minX = hitbox.min.getX();
        double maxX = hitbox.max.getX();
        double minY = hitbox.min.getY();
        double maxY = hitbox.max.getY();
        double minZ = hitbox.min.getZ();
        double maxZ = hitbox.max.getZ();

        return point.getX() >= minX
                && point.getX() <= maxX
                && point.getY() >= minY
                && point.getY() <= maxY
                && point.getZ() >= minZ
                && point.getZ() <= maxZ;
    }
}
