package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.utils.VectorUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;

public abstract class AProjectile {

    // Projectile can be maximum of 600 ticks alive (30 seconds)
    private static final int MAXIMUM_ALIVE_TICKS = 600;

    private static int CHECK_FOR_NEW_PLAYER_RATE = 0;

    // Store this here for easier usage
    private static final double version = CompatibilityAPI.getVersion();

    private final LivingEntity shooter;
    private final World world;

    private final ProjectileSettings projectileSettings;
    private FakeEntity disguise;
    private int lastDisguiseUpdateTick;

    private Vector lastLocation;
    private Vector location;
    private Vector motion;
    private double motionLength;

    private int aliveTicks;
    private double distanceTravelled;
    private boolean dead;
    private Map<String, String> stringTags;
    private Map<String, Integer> integerTags;

    protected AProjectile(ProjectileSettings projectileSettings, Location location, Vector motion) {
        this(projectileSettings, null, location, motion);
    }

    protected AProjectile(ProjectileSettings projectileSettings, LivingEntity shooter, Location location, Vector motion) {
        this.projectileSettings = projectileSettings;
        this.shooter = shooter;
        this.world = location.getWorld();
        this.location = location.toVector();
        this.lastLocation = this.location.clone();
        this.motion = motion;
        this.motionLength = motion.length();
        spawnDisguise(location, projectileSettings);
        onStart();
    }

    /**
     * @return the base projectile settings of this projectile
     */
    public ProjectileSettings getProjectileSettings() {
        return projectileSettings;
    }

    /**
     * @return the disguise of projectile, or null
     */
    public FakeEntity getDisguise() {
        return disguise;
    }

    /**
     * @return the shooter of projectile
     */
    public LivingEntity getShooter() {
        return shooter;
    }

    /**
     * @return the world where this projectile is in
     */
    public World getWorld() {
        return world;
    }

    /**
     * @return the clone of last location
     */
    public Vector getLastLocation() {
        return lastLocation.clone();
    }

    /**
     * @return the clone of current location
     */
    public Vector getLocation() {
        return location.clone();
    }

    /**
     * @param location the new location for projectile
     */
    public void setLocation(Vector location) {
        if (location == null) throw new IllegalArgumentException("Location can't be null");
        this.lastLocation = this.location.clone();
        this.location = location;
    }

    /**
     * Main difference compared to {@link #setLocation(Vector)} is that this
     * doesn't update projectile last location.
     *
     * @param location the new location for projectile
     */
    public void setRawLocation(Vector location) {
        if (location == null) throw new IllegalArgumentException("Location can't be null");
        this.location = location;
    }

    /**
     * @return the clone of current motion
     */
    public Vector getMotion() {
        return motion.clone();
    }

    /**
     * @return the current motion's length
     */
    public double getMotionLength() {
        return motionLength;
    }

    /**
     * @return the normalized current motion
     */
    public Vector getNormalizedMotion() {
        return getMotion().divide(new Vector(motionLength, motionLength, motionLength));
    }

    /**
     * @param motion the new motion for projectile
     */
    public void setMotion(Vector motion) {
        if (motion == null) throw new IllegalArgumentException("Motion can't be null");
        this.motion = motion;
        this.motionLength = motion.length();
    }

    /**
     * @return the amount of ticks this projectile has been alive
     */
    public int getAliveTicks() {
        return aliveTicks;
    }

    /**
     * @return the distance projectile has travelled where 1.0 equals 1 block
     */
    public double getDistanceTravelled() {
        return distanceTravelled;
    }

    /**
     * @param amount adds this amount to distance travelled
     */
    public void addDistanceTravelled(double amount) {
        distanceTravelled += amount;
    }

    /**
     * Used to fetch any temporary data from projectiles
     *
     * @param key the key to fetch
     * @return the value of key or null if not found
     */
    public String getTag(String key) {
        return stringTags == null || stringTags.isEmpty() ? null : this.stringTags.get(key);
    }

    /**
     * This can store temporary data for projectiles
     *
     * @param key the key to use
     * @param value the value for key
     */
    public void setTag(String key, String value) {
        if (key == null) throw new IllegalArgumentException("Key can't be null");
        if (value == null) throw new IllegalArgumentException("Value can't be null");
        if (this.stringTags == null) {
            this.stringTags = new HashMap<>();
        }
        this.stringTags.put(key, value);
    }

    /**
     * Used to fetch any temporary data from projectiles
     *
     * @param key the key to fetch
     * @return the value of key or 0 if not found
     */
    public int getIntTag(String key) {
        return integerTags == null || integerTags.isEmpty() ? 0 : this.integerTags.getOrDefault(key, 0);
    }

    /**
     * This can store temporary data for projectiles
     *
     * @param key the key to use
     * @param value the value for key
     */
    public void setIntTag(String key, int value) {
        if (key == null) throw new IllegalArgumentException("Key can't be null");
        if (this.integerTags == null) {
            this.integerTags = new HashMap<>();
        }
        this.integerTags.put(key, value);
    }

    /**
     * @return true if projectile is marked for removal or is already dead
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Marks projectile for removal and will be removed on next tick
     * or in this tick if this method is called within the tick method.
     */
    public void remove() {
        // Don't allow anything to remove this twice
        if (this.dead) return;

        this.dead = true;

        // Call one last time on move
        onMove();
        updateDisguise(true);

        onEnd();
        if (disguise != null) disguise.remove();
    }

    /**
     * Projectile base tick
     *
     * @return true if projectile should be removed from projectile runnable
     */
    public boolean tick() {
        if (this.dead) {
            // If projectile is marked for removal, but hasn't yet been removed
            return true;
        }

        if (aliveTicks >= MAXIMUM_ALIVE_TICKS) {
            return true;
        }

        // Update last location here since handle collisions will change the location
        lastLocation = location.clone();

        // Handle collisions will update location and distance travelled
        if (handleCollisions(projectileSettings.isDisableEntityCollisions())) {
            return true;
        }

        double locationY = location.getY();
        if (aliveTicks >= MAXIMUM_ALIVE_TICKS || locationY < (version < 1.16 ? -32 : world.getMinHeight()) || locationY > world.getMaxHeight()) {
            return true;
        }

        if (VectorUtil.isEmpty(motion)) {
            // No need to continue as motion is empty

            // Ensure that motion length is also 0
            if (motionLength != 0) motionLength = 0;

            onMove();
            updateDisguise(true);
            ++aliveTicks;
            return false;
        }

        Block blockAtCurrentLocation = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        double decrease = projectileSettings.getDecrease();
        if (blockAtCurrentLocation.isLiquid()) {
            decrease = projectileSettings.getDecreaseInWater();
        } else if (world.isThundering() || world.hasStorm()) {
            decrease = projectileSettings.getDecreaseWhenRainingOrSnowing();
        }

        motion.multiply(decrease);

        double gravity = projectileSettings.getGravity();
        if (gravity != 0) {
            motion.setY(motion.getY() - gravity);
        }
        motionLength = motion.length();

        double minimumSpeed = projectileSettings.getMinimumSpeed();
        double maximumSpeed = projectileSettings.getMaximumSpeed();
        if (minimumSpeed != -1.0 && motionLength < minimumSpeed) {
            if (projectileSettings.isRemoveAtMinimumSpeed()) {
                return true;
            }
            setMotion(getNormalizedMotion().multiply(projectileSettings.getMinimumSpeed()));
        } else if (maximumSpeed != -1.0 && motionLength > maximumSpeed) {
            if (projectileSettings.isRemoveAtMaximumSpeed()) {
                return true;
            }
            setMotion(getNormalizedMotion().multiply(projectileSettings.getMaximumSpeed()));
        }

        onMove();

        // Force teleport packet if disguise went wrong way on the start (e.g. collided with shooter)
        updateDisguise(aliveTicks == 2);
        ++aliveTicks;
        return false;
    }

    private void spawnDisguise(Location location, ProjectileSettings projectileSettings) {
        EntityType type = projectileSettings.getProjectileDisguise();
        if (type == null) return;

        // Cache this rate
        if (CHECK_FOR_NEW_PLAYER_RATE == 0) CHECK_FOR_NEW_PLAYER_RATE = getBasicConfigurations().getInt("Check_For_New_Player_Rate", 50);

        disguise = CompatibilityAPI.getEntityCompatibility().generateFakeEntity(location, type, projectileSettings.getDisguiseData());

        if (projectileSettings.getGravity() == 0.0) disguise.setGravity(false);

        disguise.show();

        // Update once instantly
        Vector normalizedMotion = getNormalizedMotion();
        // Force teleport packet on first run
        disguise.setPosition(location.getX(), location.getY(), location.getZ(), calculateYaw(normalizedMotion), calculatePitch(normalizedMotion), true);
        disguise.setMotion(motion);
    }

    /**
     * This method can't be used multiple times on same tick
     *
     * @param forceTeleport true to force teleport packet
     */
    protected void updateDisguise(boolean forceTeleport) {
        if (disguise == null || lastDisguiseUpdateTick == aliveTicks) return;

        // Show for new players in range
        if (aliveTicks % CHECK_FOR_NEW_PLAYER_RATE == 0) disguise.show();

        Vector normalizedMotion = getNormalizedMotion();
        disguise.setPosition(location.getX(), location.getY(), location.getZ(), calculateYaw(normalizedMotion), calculatePitch(normalizedMotion), forceTeleport);
        disguise.setMotion(motion);

        lastDisguiseUpdateTick = aliveTicks;
    }

    private float calculateYaw(Vector normalizedMotion) {
        double PI_2 = VectorUtil.PI_2;
        return (float) Math.toDegrees((Math.atan2(-normalizedMotion.getX(), normalizedMotion.getZ()) + PI_2) % PI_2);
    }

    private float calculatePitch(Vector normalizedMotion) {
        return (float) Math.toDegrees(Math.atan(-normalizedMotion.getY() / Math.sqrt(NumberConversions.square(normalizedMotion.getX()) + NumberConversions.square(normalizedMotion.getZ()))));
    }

    /**
     * Projectile collision handling. This method has to also update
     * projectile location and distance travelled based on collisions.
     *
     * @param disableEntityCollisions whether to skip entity collision checks
     * @return true if projectile should be removed from projectile runnable
     */
    public abstract boolean handleCollisions(boolean disableEntityCollisions);

    /**
     * Override this method to do something on start
     */
    public void onStart() {}

    /**
     * Override this method to do something on end
     */
    public void onEnd() {}

    /**
     * Override this method to do something when projectile moves
     */
    public void onMove() {}

    /**
     * Override this method to do something when projectile collides with block.
     *
     * @param block the collided block
     */
    public void onCollide(Block block) {}

    /**
     * Override this method to do something when projectile collides with entity
     *
     * @param entity the collided entity
     */
    public void onCollide(LivingEntity entity) {}
}