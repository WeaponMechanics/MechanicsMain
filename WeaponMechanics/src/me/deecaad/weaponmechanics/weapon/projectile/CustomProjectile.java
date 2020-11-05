package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.compatibility.worldguard.IWorldGuardCompatibility;
import me.deecaad.compatibility.worldguard.WorldGuardAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.MaterialHelper;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponcompatibility.projectile.HitBox;
import me.deecaad.weaponcompatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CustomProjectile implements ICustomProjectile {

    // Projectile can be maximum of 600 ticks alive (30 seconds)
    private static final int maximumAliveTicks = 600;

    private static final IProjectileCompatibility projectileCompatibility = WeaponCompatibilityAPI.getProjectileCompatibility();
    private static DamageHandler damageHandler = new DamageHandler();

    // Just to identify CustomProjectile
    private static int ids = 1;
    private final int id;

    // This projectile object gives general information like through settings, motions settings etc.
    public Projectile projectile;

    private int aliveTicks;
    private final LivingEntity shooter;
    private final World world;
    private Vector lastLocation;
    private Vector location;
    private Vector motion;
    private double motionLength;
    private double distanceTravelled;
    private Location lastKnownAirLocation;
    private Collisions collisions;
    private final HitBox projectileBox;
    private boolean dead;
    private final Map<String, String> tags;

    private ItemStack weaponStack;
    private String weaponTitle;

    /**
     * NMS entity if used as disguise, null otherwise.
     */
    @Nullable
    public Object projectileDisguiseNMSEntity;

    private int projectileDisguiseId; // used to make it easier to use reflection NMS
    private float projectileDisguiseYaw; // used to make it easier to use reflection NMS
    private float projectileDisguisePitch; // used to make it easier to use reflection NMS

    // Some pre calculated / initialized stuff
    private final Comparator<CollisionData> blockComparator;
    private final Comparator<CollisionData> entityComparator;

    public CustomProjectile(Projectile projectile, LivingEntity shooter, Location location, Vector motion, ItemStack weaponStack, String weaponTitle) {
        this(projectile, shooter, location, motion);
        this.weaponStack = weaponStack;
        this.weaponTitle = weaponTitle;
    }

    public CustomProjectile(Projectile projectile, LivingEntity shooter, Location location, Vector motion) {
        this.id = ++ids;

        this.projectile = projectile;

        this.shooter = shooter;
        this.world = location.getWorld();
        this.location = location.toVector();
        this.lastKnownAirLocation = location.clone();
        this.lastLocation = this.location.clone();
        this.motion = motion;
        this.tags = new HashMap<>();

        projectileBox = new HitBox(this.location, projectile.getProjectileWidth(), projectile.getProjectileHeight());

        // These can't be static as comparators has to be used for last known air location of this custom projectile
        blockComparator = (o1, o2) -> (int) (o1.getBlock().getLocation().distanceSquared(lastKnownAirLocation) - o2.getBlock().getLocation().distanceSquared(lastKnownAirLocation));
        entityComparator = (o1, o2) -> (int) (o1.getLivingEntity().getLocation().distanceSquared(lastKnownAirLocation) - o2.getLivingEntity().getLocation().distanceSquared(lastKnownAirLocation));

        if (projectile.getThrough() != null) {
            // Only required if through is used
            this.collisions = new Collisions(new TreeSet<>(blockComparator), new TreeSet<>(entityComparator));
        }

        if (projectile.getProjectileDisguise() != null) {
            motionLength = motion.length();
            projectileCompatibility.spawnDisguise(this, this.location, this.motion);
        }
    }

    @Override
    public LivingEntity getShooter() {
        return this.shooter;
    }

    @Override
    public ItemStack getWeaponStack() {
        return weaponStack;
    }

    @Override
    public String getWeaponTitle() {
        return weaponTitle;
    }

    @Override
    public int getUniqueId() {
        return this.id;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public Vector getLastLocation() {
        return this.lastLocation.clone();
    }

    @Override
    public Vector getLocation() {
        return this.location.clone();
    }

    @Override
    public void setLocation(Vector location) {
        if (location == null) throw new IllegalArgumentException("Location can't be null");
        this.location = location;
    }

    @Override
    public Vector getMotion() {
        return this.motion.clone();
    }

    @Override
    public double getMotionLength() {
        return motionLength;
    }

    @Override
    public void setMotion(Vector motion) {
        if (motion == null) throw new IllegalArgumentException("Motion can't be null");
        this.motion = motion;
        this.motionLength = motion.length();
    }

    @Override
    public double getDistanceTravelled() {
        return this.distanceTravelled;
    }

    @Override
    public String getTag(String key) {
        return this.tags.get(key);
    }

    @Override
    public void setTag(String key, String value) {
        if (key == null) throw new IllegalArgumentException("Key can't be null");
        if (value == null) throw new IllegalArgumentException("Value can't be null");

        this.tags.put(key, value);
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    @Override
    public void remove() {
        this.dead = true;
        if (projectileDisguiseId != 0) projectileCompatibility.destroyDisguise(this);
    }

    @Override
    public boolean hasProjectileDisguise() {
        return projectileDisguiseId != 0;
    }

    @Override
    public void updateDisguiseLocationAndMotion() {
        if (projectileDisguiseId != 0) projectileCompatibility.updateDisguise(this, this.location, this.motion, this.lastLocation);
    }

    @Override
    public int getProjectileDisguiseId() {
        return projectileDisguiseId;
    }

    @Override
    public float getProjectileDisguiseYaw() {
        return projectileDisguiseYaw;
    }

    @Override
    public float getProjectileDisguisePitch() {
        return projectileDisguisePitch;
    }

    /**
     * @param collisionData the collision data of hit block
     * @return true if projectile hit was cancelled
     */
    private boolean handleBlockHit(CollisionData collisionData) {

        Configuration config = WeaponMechanics.getConfigurations();
        String weaponTitle = getTag("weapon-title");

        if (weaponTitle == null) {
            return false;
        }

        Explosion explosion = config.getObject(weaponTitle + ".Explosion", Explosion.class);

        // Handle worldguard flags
        IWorldGuardCompatibility worldGuard = WorldGuardAPI.getWorldGuardCompatibility();
        boolean isCancelled;
        Location loc = location.toLocation(world);
        if (shooter instanceof Player) {
            isCancelled = !worldGuard.testFlag(loc, (Player) shooter, "weapon-explode");
        } else {
            isCancelled = !worldGuard.testFlag(loc, null, "weapon-explode");
        }

        if (isCancelled) {
            Object obj = worldGuard.getValue(loc, "weapon-explode-message");
            if (obj != null && !obj.toString().isEmpty()) {
                shooter.sendMessage(StringUtils.color(obj.toString()));
            }
        } else if (explosion != null) {
            Set<Explosion.ExplosionTrigger> triggers = explosion.getTriggers();
            boolean explosionTriggered = getTag("explosion-detonation") != null;
            boolean fluid = MaterialHelper.isFluid(collisionData.getBlock().getType()) && triggers.contains(Explosion.ExplosionTrigger.LIQUID);
            boolean solid = collisionData.getBlock().getType().isSolid() && triggers.contains(Explosion.ExplosionTrigger.BLOCK);

            if (!explosionTriggered && (fluid || solid)) {

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Vector v = getLocation();
                        Location origin = new Location(world, v.getX(), v.getY(), v.getZ());
                        explosion.explode(shooter, origin, CustomProjectile.this);

                        setTag("explosion-detonation", "true");
                    }
                }.runTaskLater(WeaponMechanics.getPlugin(), explosion.getDelay());
            }
        }

        return false;
    }

    /**
     * @param collisionData the collision data of hit entity
     * @param normalizedDirection the direction of projectile in normalized form
     * @return true if projectile hit was cancelled
     */
    private boolean handleEntityHit(CollisionData collisionData, Vector normalizedDirection) {

        // Handle worldguard flags
        IWorldGuardCompatibility worldGuard = WorldGuardAPI.getWorldGuardCompatibility();
        Location loc = location.toLocation(world);
        boolean isCancelled;
        if (shooter instanceof Player) {
            isCancelled = !worldGuard.testFlag(loc, (Player) shooter, "weapon-damage");
        } else {
            isCancelled = !worldGuard.testFlag(loc, null, "weapon-damage");
        }

        if (isCancelled) {
            Object obj = worldGuard.getValue(loc, "weapon-damage-message");
            if (obj != null && !obj.toString().isEmpty()) {
                shooter.sendMessage(StringUtils.color(obj.toString()));
            }

            return true;
        }

        Configuration config = WeaponMechanics.getConfigurations();
        DamagePoint point = collisionData.getHitBox().getDamagePoint(collisionData, normalizedDirection);
        String weaponTitle = getTag("weapon-title");

        if (weaponTitle == null) {
            return true;
        }

        LivingEntity victim = collisionData.getLivingEntity();
        boolean isDamaged = damageHandler.tryUse(victim, shooter, weaponTitle, this, point, false);

        // Handle worldguard flags
        if (shooter instanceof Player) {
            isCancelled = !worldGuard.testFlag(loc, (Player) shooter, "weapon-explode");
        } else {
            isCancelled = !worldGuard.testFlag(loc, null, "weapon-explode");
        }

        if (isCancelled) {
            Object obj = worldGuard.getValue(loc, "weapon-explode-message");
            if (obj != null && !obj.toString().isEmpty()) {
                shooter.sendMessage(StringUtils.color(obj.toString()));
            }
        }

        Explosion explosion = config.getObject(weaponTitle + ".Explosion", Explosion.class);
        boolean canExplode = getTag("explosion-detonation") == null;
        if (!isCancelled && explosion != null && canExplode && explosion.getTriggers().contains(Explosion.ExplosionTrigger.ENTITY)) {

            new BukkitRunnable() {
                @Override
                public void run() {
                    Vector v = getLocation();
                    Location origin = new Location(world, v.getX(), v.getY(), v.getZ());
                    explosion.explode(shooter, origin, CustomProjectile.this);

                    setTag("explosion-detonation", "true");
                }
            }.runTaskLater(WeaponMechanics.getPlugin(), explosion.getDelay());
        }

        return false;
    }

    /**
     * Projectile base tick.
     * Basically contains motion and collision checks
     *
     * @return true if projectile should be removed from projectile runnable
     */
    public boolean tick() {
        if (this.dead) {
            // No need for remove() call as this can only be true if its already been called at least once
            // This check is here just if some other plugin removes this projectile
            return true;
        }

        ProjectileMotion projectileMotion = projectile.getProjectileMotion();

        if (location.getY() < -32 || location.getY() > 288 || aliveTicks > maximumAliveTicks) {
            remove();
            return true;
        }

        motionLength = motion.length();

        double minimumSpeed = projectileMotion.getMinimumSpeed();
        double maximumSpeed = projectileMotion.getMaximumSpeed();
        if (minimumSpeed != -1.0 && motionLength < minimumSpeed) {
            if (projectileMotion.isRemoveAtMinimumSpeed()) {
                remove();
                return true;
            }

            // normalize first
            motion.divide(new Vector(motionLength, motionLength, motionLength));

            // then multiply with wanted speed
            motion.multiply(projectileMotion.getMinimumSpeed());

            // Recalculate the length
            motionLength = motion.length();
        } else if (maximumSpeed != -1.0 && motionLength > maximumSpeed) {
            if (projectileMotion.isRemoveAtMaximumSpeed()) {
                remove();
                return true;
            }
            // normalize first
            motion.divide(new Vector(motionLength, motionLength, motionLength));

            // then multiply with wanted speed
            motion.multiply(projectileMotion.getMaximumSpeed());

            // Recalculate the length
            motionLength = motion.length();
        }

        if (projectileDisguiseId != 0) projectileCompatibility.updateDisguise(this, this.location, this.motion, this.lastLocation);

        lastLocation = location.clone();

        if (handleCollisions()) {
            remove();
            return true;
        }

        Block blockAtLocation = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (blockAtLocation.isEmpty()) {
            lastKnownAirLocation = location.clone().toLocation(world);
        }

        double decrease = projectileMotion.getDecrease();
        if (blockAtLocation.isLiquid()) {
            decrease = projectileMotion.getDecreaseInWater();
        } else if (world.isThundering() || world.hasStorm()) {
            decrease = projectileMotion.getDecreaseWhenRainingOrSnowing();
        }

        motion.multiply(decrease);
        if (projectileMotion.getGravity() != 0.0) {
            motion.setY(motion.getY() - projectileMotion.getGravity());
        }

        // todo event

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
     * @return true if projectile should die
     */
    private boolean handleCollisions() {

        // todo: make this method better

        // Pre calculate the motion to add for location on each iteration
        // First normalize motion and then multiply
        Vector addMotion = motion.clone().divide(new Vector(motionLength, motionLength, motionLength)).multiply(projectile.getProjectileHeight() * 2);

        // Old motion without modified speed modifier
        Vector oldMotion = motion.clone();
        projectileBox.update(location, projectile.getProjectileWidth(), projectile.getProjectileHeight());
        location.add(oldMotion);
        this.distanceTravelled += motionLength;

        for (double travelled = 0.0; travelled <= motionLength; travelled += projectile.getProjectileHeight()) {
            Collisions iteration = getCollisions(projectileBox);

            if (iteration == null) {
                projectileBox.shift(addMotion);
                continue;
            }

            if (handleEntityHits(iteration.getEntityCollisions())
                    || handleBlockHits(iteration.getBlockCollisions())) {
                // Projectile should die
                return true;
            }

            projectileBox.shift(addMotion);
        }

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
     * @param entityCollisions the list of all collisions to handle
     * @return true if projectile should die
     */
    private boolean handleEntityHits(SortedSet<CollisionData> entityCollisions) {
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
            return !handleEntityHit(entityCollisions.first(), motion.clone().divide(new Vector(motionLength, motionLength, motionLength)));
        }

        int maxEntitiesLeft = entityThru.getMaximumPassThroughs() - collisions.getEntityCollisions().size();
        for (CollisionData entity : entityCollisions) {

            if (handleEntityHit(entity, motion.clone().divide(new Vector(motionLength, motionLength, motionLength)))) {
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

                    HitBox blockBox = projectileCompatibility.getHitBox(block);
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
                HitBox entityBox = projectileCompatibility.getHitBox(entity);
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

    /**
     * Set the projectile disguise id.
     * This id is used when sending entity moving etc. packets.
     *
     * @param nmsEntityId the projectile disguise's id
     */
    public void setProjectileDisguiseId(int nmsEntityId) {
        if (projectileDisguiseId != 0) throw new IllegalArgumentException("You can't set new projectile disguise id after its set!");
        projectileDisguiseId = nmsEntityId;
    }

    /**
     * Calculates new yaw and pitch based on the projectile motion.
     */
    public void calculateYawAndPitch() {
        if (projectileDisguiseId != 0) return;

        double x = motion.getX();
        double z = motion.getZ();

        double PIx2 = 6.283185307179;
        projectileDisguiseYaw = (float) Math.toDegrees((Math.atan2(-x, z) + PIx2) % PIx2);
        projectileDisguiseYaw %= 360.0F;
        if (projectileDisguiseYaw >= 180.0F) {
            projectileDisguiseYaw -= 360.0F;
        } else if (projectileDisguiseYaw < -180.0F) {
            projectileDisguiseYaw += 360.0F;
        }

        projectileDisguisePitch = (float) Math.toDegrees(Math.atan(-motion.getY() / Math.sqrt(NumberConversions.square(x) + NumberConversions.square(z))));
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