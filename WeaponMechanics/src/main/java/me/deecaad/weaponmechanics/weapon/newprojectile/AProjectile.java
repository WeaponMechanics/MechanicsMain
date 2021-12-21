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

import java.util.Map;

public abstract class AProjectile {

    // Projectile can be maximum of 600 ticks alive (30 seconds)
    private static final int MAXIMUM_ALIVE_TICKS = 600;

    // Store this references here for easier usage
    private static final IProjectileCompatibility projectileCompatibility = WeaponCompatibilityAPI.getProjectileCompatibility();
    private static final double version = CompatibilityAPI.getVersion();

    // NMS entity if used as disguise
    public Object nmsEntity;
    private int nmsEntityId;

    private final LivingEntity shooter;
    protected final World world;

    private ProjectileSettings projectileSettings;

    private Vector lastLocation;
    protected Vector location;
    private Vector motion;
    protected double motionLength;

    private int aliveTicks;
    private double distanceTravelled;
    private boolean dead;
    private Map<String, String> tags;

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
     * Projectile base tick
     *
     * @return true if projectile should be removed from projectile runnable
     */
    public boolean tick() {
        if (this.dead) {
            // If projectile is marked for removal, but hasn't yet been removed
            return true;
        }
        ++aliveTicks;

        lastLocation = location.clone();

        // Handle collisions will update location
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

            // normalize first
            motion.divide(new Vector(motionLength, motionLength, motionLength));

            // then multiply with wanted speed
            motion.multiply(projectileSettings.getMinimumSpeed());

            // Recalculate the length
            motionLength = motion.length();
        } else if (maximumSpeed != -1.0 && motionLength > maximumSpeed) {
            if (projectileSettings.isRemoveAtMaximumSpeed()) {
                remove();
                return true;
            }
            // normalize first
            motion.divide(new Vector(motionLength, motionLength, motionLength));

            // then multiply with wanted speed
            motion.multiply(projectileSettings.getMaximumSpeed());

            // Recalculate the length
            motionLength = motion.length();
        }

        return false;
    }

    /**
     * Projectile collision handling. This method has to also update
     * projectile location and distance travelled based on collisions.
     *
     * @return true if projectile should be removed from projectile runnable
     */
    public abstract boolean handleCollisions();

    /**
     * Marks projectile for removal and will be removed on next tick
     * or in this tick if this method is called within the tick method.
     */
    public void remove() {
        this.dead = true;
    }
}