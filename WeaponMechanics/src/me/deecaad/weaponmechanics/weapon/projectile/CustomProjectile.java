package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.projectile.HitBox;
import me.deecaad.compatibility.projectile.IProjectileCompatibility;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.*;

public class CustomProjectile {

    // Projectile can be maximum of 600 ticks alive (30 seconds)
    private static final int maximumAliveTicks = 600;

    // Just to identify CustomProjectile
    private static int ids = 1;
    private int id;

    // This custom projectile gives general information like through settings, motions settings etc.
    public Projectile projectile;

    private int aliveTicks;
    private LivingEntity shooter;
    public World world;
    public Vector lastLocation;
    public Vector location;
    public Vector motion;
    private Location lastKnownAirLocation;
    private Collisions collisions;
    private HitBox projectileBox;

    // Only required if there is projectile disguise
    public Object nmsEntity;
    public int projectileDisguiseId; // used to make it easier to use reflection NMS
    public float yaw; // used to make it easier to use reflection NMS
    public float pitch; // used to make it easier to use reflection NMS

    // Some pre calculated / initialized stuff
    private Comparator<CollisionData> blockComparator;
    private Comparator<CollisionData> entityComparator;

    public CustomProjectile(Projectile projectile, LivingEntity shooter, Location location, Vector motion) {
        this.id = ++ids;

        this.projectile = projectile;

        this.shooter = shooter;
        this.world = location.getWorld();
        this.location = location.toVector();
        this.lastKnownAirLocation = location.clone();
        this.lastLocation = this.location.clone();
        this.motion = motion;

        projectileBox = new HitBox(this.location, projectile.getProjectileWidth(), projectile.getProjectileLength());

        // These can't be static as comparators has to be used for last known air location of this custom projectile
        blockComparator = (o1, o2) -> (int) (o1.getBlock().getLocation().distanceSquared(lastKnownAirLocation) - o2.getBlock().getLocation().distanceSquared(lastKnownAirLocation));
        entityComparator = (o1, o2) -> (int) (o1.getLivingEntity().getLocation().distanceSquared(lastKnownAirLocation) - o2.getLivingEntity().getLocation().distanceSquared(lastKnownAirLocation));

        if (projectile.getThrough() != null) {
            // Only required if through is used
            this.collisions = new Collisions(new TreeSet<>(blockComparator), new TreeSet<>(entityComparator));
        }

        if (projectile.getProjectileDisguise() != null) {
            CompatibilityAPI.getCompatibility().getProjectileCompatibility().spawnDisguise(this);
        }
    }

    /**
     * This is only unique ID for WM custom projectile objects.
     * NOT FOR MC ENTITY IDs, use projectileDisguiseId for that (if disguises are used)
     *
     * @return the id of this custom projectile
     */
    public int getId() {
        return id;
    }

    /**
     * @param collisionData the collision data of hit block
     * @return true if projectile hit was cancelled
     */
    private boolean handleBlockHit(CollisionData collisionData) {

        return false;
    }

    /**
     * @param collisionData the collision data of hit entity
     * @param normalizedDirection the direction of projectile in normalized form
     * @return true if projectile hit was cancelled
     */
    private boolean handleEntityHit(CollisionData collisionData, Vector normalizedDirection) {

        Bukkit.broadcastMessage("" + collisionData.getHitBox().getDamagePoint(collisionData, normalizedDirection));

        return false;
    }

    /**
     * Projectile base tick.
     * Basically contains motion and collision checks
     *
     * @return true if projectile should be removed from projectile runnable
     */
    public boolean tick() {
        ProjectileMotion projectileMotion = projectile.getProjectileMotion();
        IProjectileCompatibility projectileCompatibility = CompatibilityAPI.getCompatibility().getProjectileCompatibility();

        if (location.getY() < -32 || location.getY() > 288 || aliveTicks > maximumAliveTicks) {
            if (projectileDisguiseId != 0) projectileCompatibility.destroyDisguise(this);
            return true;
        }

        float length = (float) motion.length();

        double minimumSpeed = projectileMotion.getMinimumSpeed();
        double maximumSpeed = projectileMotion.getMaximumSpeed();
        if (minimumSpeed != -1.0 && length < minimumSpeed) {
            if (projectileMotion.isRemoveAtMinimumSpeed()) {
                if (projectileDisguiseId != 0) projectileCompatibility.destroyDisguise(this);
                return true;
            }

            // normalize first
            motion.divide(new Vector(length, length, length));

            // then multiply with wanted speed
            motion.multiply(projectileMotion.getMinimumSpeed());

            // Recalculate the length
            length = (float) motion.length();
        } else if (maximumSpeed != -1.0 && length > maximumSpeed) {
            if (projectileMotion.isRemoveAtMaximumSpeed()) {
                if (projectileDisguiseId != 0) projectileCompatibility.destroyDisguise(this);
                return true;
            }
            // normalize first
            motion.divide(new Vector(length, length, length));

            // then multiply with wanted speed
            motion.multiply(projectileMotion.getMaximumSpeed());

            // Recalculate the length
            length = (float) motion.length();
        }

        if (projectileDisguiseId != 0) projectileCompatibility.updateDisguise(this, length);

        lastLocation = location.clone();

        if (handleCollisions(length)) {
            if (projectileDisguiseId != 0) projectileCompatibility.destroyDisguise(this);
            return true;
        }

        Block blockAtLocation = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (blockAtLocation.getType().isAir()) {
            lastKnownAirLocation = location.clone().toLocation(world);
        }

        double decrease = projectileMotion.getDecrease();
        if (blockAtLocation.getType() == Material.WATER) {
            decrease = projectileMotion.getDecreaseInWater();
        } else if (world.isThundering() || world.hasStorm()) {
            decrease = projectileMotion.getDecreaseWhenRainingOrSnowing();
        }

        motion.multiply(decrease);
        if (projectileMotion.getGravity() != 0.0) {
            motion.setY(motion.getY() - projectileMotion.getGravity());
        }

        ++aliveTicks;
        return false;
    }

    /**
     * Ray traces projectile and motion length distance.
     *
     * If through settings doesn't allow passing through blocks or entities this will only
     * allow one block or entity. If they allow, then there may be many blocks or entities in one ray trace.
     * This method can't use more blocks or entities than getThroughSettings() settings allow (Maximum_Pass_Throughs).
     *
     * @param length the motion length
     * @return true if projectile should die
     */
    private boolean handleCollisions(float length) {

        // Pre calculate the motion to add for location on each iteration
        // First normalize motion and then multiply
        Vector addMotion = motion.clone().divide(new Vector(length, length, length)).multiply(projectile.getProjectileLength() * 2);

        // Old motion without modified speed modifier
        Vector oldMotion = motion.clone();

        projectileBox.update(location, projectile.getProjectileWidth(), projectile.getProjectileLength());

        for (double i = 0.0; i <= length; i += projectile.getProjectileLength()) {
            Collisions iteration = getCollisions(projectileBox);

            if (iteration == null) {
                projectileBox.shift(addMotion);
                continue;
            }

            if (handleEntityHits(length, iteration.getEntityCollisions())) {
                // Projectile should die
                return true;
            }
            if (handleBlockHits(iteration.getBlockCollisions())) {
                // Projectile should die
                return true;
            }

            projectileBox.shift(addMotion);
        }

        location.add(oldMotion);
        return false;
    }

    /**
     * @param blockCollisions the list of all collisions to handle
     * @return true if projectile should die
     */
    private boolean handleBlockHits(SortedSet<CollisionData> blockCollisions) {
        if (blockCollisions.isEmpty()) {
            return false;
        }

        Through through = projectile.getThrough();
        Through.ThroughData blockThru = null;
        if (through != null) {
            blockThru = through.getBlocks();
        }

        if (blockThru == null) {
            // If this is true, that most likely means that block hit was cancelled
            // That is why add ! to destroy projectile it would have returned false
            return !handleBlockHit(blockCollisions.first());
        }

        int maxBlocksLeft = blockThru.getMaximumPassThroughs() - collisions.getBlockCollisions().size();
        for (CollisionData block : blockCollisions) {

            if (handleBlockHit(block)) {
                // Returned true and that most likely means that block hit was cancelled, skipping...
                continue;
            }

            Block bukkitBlock = block.getBlock();
            Through.ExtraThroughData extraThroughData = blockThru.getModifiers(bukkitBlock.getType(), bukkitBlock.getData());
            if (extraThroughData == null) { // Projectile should die
                return true;
            }

            motion.multiply(extraThroughData.getSpeedModifier());
            collisions.getBlockCollisions().add(block);

            if (--maxBlocksLeft <= 0) { // Projectile should die
                return true;
            }
        }
        return false;
    }

    /**
     * @param length the motion length (used to get normalized direction)
     * @param entityCollisions the list of all collisions to handle
     * @return true if projectile should die
     */
    private boolean handleEntityHits(float length, SortedSet<CollisionData> entityCollisions) {
        if (entityCollisions.isEmpty()) {
            return false;
        }

        Through through = projectile.getThrough();
        Through.ThroughData entityThru = null;
        if (through != null) {
            entityThru = through.getEntities();
        }

        if (entityThru == null) {
            // If this is true, that most likely means that entity hit was cancelled
            // That is why add ! to destroy projectile it would have returned false
            return !handleEntityHit(entityCollisions.first(), motion.clone().divide(new Vector(length, length, length)));
        }

        int maxEntitiesLeft = entityThru.getMaximumPassThroughs() - collisions.getEntityCollisions().size();
        for (CollisionData entity : entityCollisions) {

            if (handleEntityHit(entity, motion.clone().divide(new Vector(length, length, length)))) {
                // Returned true and that most likely means that entity hit was cancelled, skipping...
                continue;
            }

            Through.ExtraThroughData extraThroughData = entityThru.getModifiers(entity.getLivingEntity().getType());
            if (extraThroughData == null) { // Projectile should die
                return true;
            }

            motion.multiply(extraThroughData.getSpeedModifier());
            collisions.getEntityCollisions().add(entity);

            if (--maxEntitiesLeft <= 0) { // Projectile should die
                return true;
            }
        }
        return false;
    }

    /**
     * Returns ALL collisions inside bounding box which are valid for projectile to hit.
     * This also sorts those collisions based on their distances to projectile.
     *
     * @param projectileBox the hit box of projectile
     * @return the collisions inside projectile hit box
     */
    private Collisions getCollisions(HitBox projectileBox) {

        // First get all blocks and entities in bounding box and then sort them based on distance

        Vector min = projectileBox.getFlooredMin();
        Vector max = projectileBox.getFlooredMax();

        // All chunks that bounding box contains (will be added in block iteration)
        Set<Chunk> chunks = new HashSet<>();

        SortedSet<CollisionData> blockCollisions = new TreeSet<>(blockComparator);
        SortedSet<CollisionData> entityCollisions = new TreeSet<>(entityComparator);

        // Iterate through all blocks inside bounding box
        for (int x = (int) min.getX(); x <= max.getX(); ++x) {
            for (int y = (int) min.getY(); y <= max.getY(); ++y) {
                for (int z = (int) min.getZ(); z <= max.getZ(); ++z) {
                    Block block = world.getBlockAt(x, y, z);
                    chunks.add(block.getChunk());

                    HitBox blockBox = CompatibilityAPI.getCompatibility().getProjectileCompatibility().getHitBox(block);
                    if (blockBox == null) continue; // Null means most likely that block is passable, liquid or air

                    Vector hitLocation = projectileBox.collisionPoint(blockBox);
                    if (hitLocation == null) continue; // Null means that projectile hit box and block hit box didn't collide

                    CollisionData blockCollision = new CollisionData(blockBox, hitLocation, block);
                    if (blockCollisions.contains(blockCollision) // if this iteration already once hit block
                            || (collisions != null && collisions.getBlockCollisions().contains(blockCollision))) { // if this projectile has already hit this block once
                        continue;
                    }

                    // add block collision
                    blockCollisions.add(blockCollision);
                }
            }
        }

        // World.getNearbyEntities() is not allowed in async (entity checks aren't meant to be used async)
        // Bounding box check may be inaccurate sometimes if entity moves when this is looping (async issue only)
        for (Chunk chunk : chunks) {
            for (final Entity entity : chunk.getEntities()) {
                if (!(entity instanceof LivingEntity) || entity.getEntityId() == shooter.getEntityId()) {
                    continue;
                }
                HitBox entityBox = CompatibilityAPI.getCompatibility().getProjectileCompatibility().getHitBox(entity);
                if (entityBox == null) continue; // entity is invulnerable

                Vector hitLocation = projectileBox.collisionPoint(entityBox);
                if (hitLocation == null) continue; // Null means that projectile hit box and entity hit box didn't collide

                CollisionData entityCollision = new CollisionData(entityBox, hitLocation, (LivingEntity) entity);
                if (entityCollisions.contains(entityCollision) // if this iteration already once hit entity
                        || (collisions != null && collisions.getEntityCollisions().contains(entityCollision))) { // if this projectile has already hit this entity once
                    continue;
                }

                // add entity collision
                entityCollisions.add(entityCollision);
            }
        }

        // No any valid blocks or entities
        if (blockCollisions.isEmpty() && entityCollisions.isEmpty()) {
            return null;
        }

        return new Collisions(blockCollisions, entityCollisions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomProjectile that = (CustomProjectile) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}