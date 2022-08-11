package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.RayTrace;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WeaponProjectile extends AProjectile {

    // Storing this reference to be able to use cloneSettingsAndShoot(Location, Motion) method
    private final ProjectileSettings projectileSettings;

    private final ItemStack weaponStack;
    private final String weaponTitle;

    private final Sticky sticky;
    private final Through through;
    private final Bouncy bouncy;

    private StickedData stickedData;
    private int throughAmount;
    private int bounces;
    private boolean rolling;

    // These are for through and bouncy to deny collision with
    // same block or entity right after colliding with it
    private int lastBlockUpdateTick;
    private Location lastBlock;
    private int lastEntityUpdateTick;
    private int lastEntity = -1;

    private final RayTrace rayTrace;

    public WeaponProjectile(ProjectileSettings projectileSettings, LivingEntity shooter, Location location,
                            Vector motion, ItemStack weaponStack, String weaponTitle,
                            Sticky sticky, Through through, Bouncy bouncy) {
        super(shooter, location, motion);

        this.projectileSettings = projectileSettings;
        this.weaponStack = weaponStack;
        this.weaponTitle = weaponTitle;
        this.sticky = sticky;
        this.through = through;
        this.bouncy = bouncy;

        if (projectileSettings.isDisableEntityCollisions()) {
            this.rayTrace = new RayTrace()
                    .withBlockFilter(this::equalToLastHit)
                    .disableEntityChecks()
                    .enableLiquidChecks()
                    .withRaySize(projectileSettings.getSize());
        } else {
            this.rayTrace = new RayTrace()
                    .withBlockFilter(this::equalToLastHit)
                    .withEntityFilter(entity ->
                            equalToLastHit(entity)
                            || (getShooter() != null && getAliveTicks() < 10 && entity.getEntityId() == getShooter().getEntityId()))
                    .enableLiquidChecks()
                    .withRaySize(projectileSettings.getSize());
        }
    }

    /**
     * Clones the settings of this weapon projectile without shooting it
     *
     * @param location the cloned projectile's new start location
     * @param motion the cloned projectile's new motion
     * @return the cloned projectile
     */
    public WeaponProjectile clone(Location location, Vector motion) {
        return new WeaponProjectile(projectileSettings, getShooter(), location, motion, weaponStack, weaponTitle, sticky, through, bouncy);
    }

    @Override
    public double getGravity() {
        return rolling || stickedData != null ? 0 : projectileSettings.getGravity();
    }

    @Override
    public double getMinimumSpeed() {
        return projectileSettings.getMinimumSpeed();
    }

    @Override
    public boolean isRemoveAtMinimumSpeed() {
        return projectileSettings.isRemoveAtMinimumSpeed();
    }

    @Override
    public double getMaximumSpeed() {
        return projectileSettings.getMaximumSpeed();
    }

    @Override
    public boolean isRemoveAtMaximumSpeed() {
        return projectileSettings.isRemoveAtMaximumSpeed();
    }

    @Override
    public double getDrag() {
        if (getCurrentBlock().isLiquid())
            return projectileSettings.getDecreaseInWater();
        else if (getWorld().isThundering() || getWorld().hasStorm())
            return projectileSettings.getDecreaseWhenRainingOrSnowing();
        else
            return projectileSettings.getDecrease();
    }

    @Override
    public int getMaximumAliveTicks() {
        return projectileSettings.getMaximumAliveTicks();
    }

    public boolean hasTravelledMaximumDistance() {
        double maximum = projectileSettings.getMaximumTravelDistance();
        return maximum != -1 && getDistanceTravelled() >= maximum;
    }

    /**
     * Can be null if for example API is used to shoot this projectile.
     *
     * @return the item stack used to shoot this projectile
     */
    @Nullable
    public ItemStack getWeaponStack() {
        return weaponStack;
    }

    /**
     * @return the weapon title used to shoot this projectile
     */
    public String getWeaponTitle() {
        return weaponTitle;
    }

    /**
     * @return the sticked data if this projectile is sticked to some entity or block
     */
    public StickedData getStickedData() {
        return stickedData;
    }

    /**
     * Set new sticked data. If new sticked data is null, sticked data is removed
     *
     * @param stickedData the new sticked data
     */
    public void setStickedData(StickedData stickedData) {
        if (stickedData == null) {
            this.stickedData = null;
            // This basically removes sticky
            setMotion(new Vector(
                    ThreadLocalRandom.current().nextFloat() * 0.2,
                    ThreadLocalRandom.current().nextFloat() * 0.2,
                    ThreadLocalRandom.current().nextFloat() * 0.2
            ));
            return;
        }

        // Just extra check if entity happens to die or block disappear
        if (stickedData.isBlockStick()) {
            if (stickedData.getBlock() == null) {
                return;
            }
        } else if (stickedData.getLivingEntity() == null) {
            return;
        }

        // Now we can safely set the sticked data and other required values

        this.stickedData = stickedData;
        setLocation(stickedData.getNewLocation());
        setMotion(new Vector(0, 0, 0));
    }

    /**
     * @return the amount of hit boxes this projectile has gone through using through feature
     */
    public int getThroughAmount() {
        return throughAmount;
    }

    /**
     * @return the amount of hit boxes this projectile has bounced off using bouncy feature
     */
    public int getBounces() {
        return bounces;
    }

    /**
     * Setting rolling to true disables projectile gravity
     *
     * @param rolling the new rolling state
     */
    public void setRolling(boolean rolling) {
        if (bouncy == null) return;

        this.rolling = rolling;
    }

    /**
     * @return whether projectile is currently rolling
     */
    public boolean isRolling() {
        return rolling;
    }

    @Override
    public boolean handleCollisions() {

        Vector motion = getMotion();
        Vector possibleNextLocation = getLocation().add(motion);
        if (!getWorld().isChunkLoaded(possibleNextLocation.getBlockX() >> 4, possibleNextLocation.getBlockZ() >> 4)) {
            // Remove projectile if next location would be in unloaded chunk
            return true;
        }

        if (stickedData != null) {
            Vector newLocation = stickedData.getNewLocation();
            if (newLocation == null) {
                // If this happens, either entity is dead or block isn't there anymore
                setStickedData(null);
            } else if (!stickedData.isBlockStick()) {
                // Update location and update distance travelled if living entity
                setRawLocation(newLocation);
                addDistanceTravelled(getLastLocation().distance(newLocation));
                return hasTravelledMaximumDistance();
            }
            return false;
        }

        // Don't check for new collisions if motion is empty
        if (VectorUtil.isEmpty(motion)) return false;

        // Returns sorted list of hits

        List<RayTraceResult> hits = rayTrace.cast(getWorld(), getLocation(), possibleNextLocation, getNormalizedMotion(),
                through == null ? 0 : through.getMaximumThroughAmount());
        if (hits == null) {

            // Check if can't keep rolling
            if (isRolling() && bouncy.checkForRollingCancel(this)) return true;

            // No hits, simply update location and distance travelled
            setRawLocation(possibleNextLocation);
            addDistanceTravelled(getMotionLength());

            return hasTravelledMaximumDistance();
        }

        double cacheMotionLength = getMotionLength();
        double distanceAlreadyAdded = 0;

        for (RayTraceResult hit : hits) {

            // Stay on track of current location and distance travelled on each loop
            setRawLocation(hit.getHitLocation());
            double add = hit.getDistanceTravelled() - distanceAlreadyAdded;
            addDistanceTravelled(distanceAlreadyAdded += add);

            if (hasTravelledMaximumDistance()) {
                // Kill projectile since it can't go this far
                return true;
            }

            onCollide(hit);

            // We only want to let onCollide to be called onLiquid hits
            if (hit.isBlock() && hit.getBlock().isLiquid()) {
                continue;
            }

            // Returned true and that most likely means that block hit was cancelled, skipping...
            if (hit.handleHit(this)) continue;

            // Sticky
            if (sticky != null && sticky.handleSticking(this, hit)) {
                // Break since projectile sticked to entity or block
                return false;
            }

            // Through
            if (through != null && through.handleThrough(this, hit)) {
                // Continue since projectile went through.
                // We still need to check that other collisions also allows this
                ++throughAmount;

                // Update last hit entity / block
                updateLastHit(hit);
                continue;
            }

            // Bouncy and rolling
            if (bouncy != null) {

                // We want to check that projectile isn't already rolling
                // If it is already rolling we want to allow bouncing against hits
                if (!isRolling() && cacheMotionLength < bouncy.getRequiredMotionToStartRollingOrDie()) {

                    // Returns true if projectile should die, false otherwise
                    return !hit.isBlock() || !bouncy.handleRolling(this, hit.getBlock());
                } else if (bouncy.handleBounce(this, hit)) {
                    // Break since projectile bounced to different direction
                    ++bounces;

                    // Update last hit entity / block
                    updateLastHit(hit);
                    return false;
                }
            }

            // Projectile should die if code reaches this point
            return true;
        }

        // Here we know that projectile didn't die on any collision.
        // We still have to update the location to last possible location.
        setRawLocation(possibleNextLocation);
        addDistanceTravelled(cacheMotionLength - distanceAlreadyAdded);

        return hasTravelledMaximumDistance();
    }

    private void updateLastHit(RayTraceResult hit) {
        if (hit.isBlock()) {
            lastBlock = hit.getBlock().getLocation();
            lastBlockUpdateTick = getAliveTicks() + 1;
        } else {
            lastEntity = hit.getLivingEntity().getEntityId();
            lastEntityUpdateTick = getAliveTicks() + 1;
        }

        // Logic of +1 for last update tick:

        // Current alive tick is 5 in this case
        // lastXUpdateTick = 5 + 1

        // getAliveTicks <= lastXUpdateTick

        // getAliveTicks = 5 // CHECKS DURING CURRENT TICK
        // 5 <= 6 = TRUE
        // -> equalToLastHit can be true

        // getAliveTicks = 6 // CHECKS TICK AFTER HIT
        // 6 <= 6 = TRUE
        // -> equalToLastHit can be true

        // getAliveTicks = 7 // CHECKS 2 TICKS AFTER HIT
        // 7 <= 6 = FALSE
        // -> equalToLastHit is false even if the hit entity / block is same
    }

    private boolean equalToLastHit(Block hit) {
        Location hitBlock = hit.getLocation();
        return lastBlock != null && lastBlock.getBlockX() == hitBlock.getBlockX() && lastBlock.getBlockY() == hitBlock.getBlockY() && lastBlock.getBlockZ() == hitBlock.getBlockZ() // Check block
                && getAliveTicks() <= lastBlockUpdateTick; // Check hit tick
    }

    private boolean equalToLastHit(LivingEntity entity) {
        return lastEntity != -1 && lastEntity == entity.getEntityId() // Check entity
                && getAliveTicks() <= lastEntityUpdateTick; // Check hit tick
    }

    @Override
    public void onEnd() {
        super.onEnd();
        Bukkit.getPluginManager().callEvent(new ProjectileEndEvent(this));
    }
}