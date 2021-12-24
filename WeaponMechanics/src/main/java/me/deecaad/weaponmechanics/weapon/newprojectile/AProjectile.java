package me.deecaad.weaponmechanics.weapon.newprojectile;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.compatibility.projectile.IProjectileCompatibility;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public abstract class AProjectile {

    // Projectile can be maximum of 600 ticks alive (30 seconds)
    private static final int MAXIMUM_ALIVE_TICKS = 600;

    // Store this references here for easier usage
    protected static final IProjectileCompatibility projectileCompatibility = WeaponCompatibilityAPI.getProjectileCompatibility();
    private static final double version = CompatibilityAPI.getVersion();

    private final LivingEntity shooter;
    private final World world;

    private final ProjectileSettings projectileSettings;

    private Vector lastLocation;
    private Vector location;
    private Vector motion;
    private double motionLength;

    private int aliveTicks;
    private double distanceTravelled;
    private boolean dead;
    private Map<String, String> stringTags;
    private Map<String, Integer> integerTags;

    protected AProjectile(ProjectileSettings projectileSettings, LivingEntity shooter, Location location, Vector motion) {
        this.projectileSettings = projectileSettings;
        this.shooter = shooter;
        this.world = location.getWorld();
        this.location = location.toVector();
        this.lastLocation = this.location.clone();
        this.motion = motion;
        this.motionLength = motion.length();
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
        return integerTags == null || integerTags.isEmpty() ? 0 : this.integerTags.get(key);
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
        this.dead = true;
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
            remove();
            return true;
        }

        lastLocation = location.clone();

        // Handle collisions will update location and distance travelled
        if (handleCollisions()) {
            remove();
            return true;
        }

        double locationY = location.getY();
        if (aliveTicks >= MAXIMUM_ALIVE_TICKS || locationY < (version < 1.16 ? -32 : world.getMinHeight()) || locationY > world.getMaxHeight()) {
            remove();
            return true;
        }

        if (VectorUtil.isEmpty(motion)) {
            // No need to continue as motion is empty

            // Ensure that motion length is also 0
            if (motionLength != 0) motionLength = 0;

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
                remove();
                return true;
            }
            setMotion(getNormalizedMotion().multiply(projectileSettings.getMinimumSpeed()));
        } else if (maximumSpeed != -1.0 && motionLength > maximumSpeed) {
            if (projectileSettings.isRemoveAtMaximumSpeed()) {
                remove();
                return true;
            }
            setMotion(getNormalizedMotion().multiply(projectileSettings.getMaximumSpeed()));
        }

        ++aliveTicks;

        return false;
    }

    /**
     * Projectile collision handling. This method has to also update
     * projectile location and distance travelled based on collisions.
     *
     * @return true if projectile should be removed from projectile runnable
     */
    public abstract boolean handleCollisions();
}