package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.compatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileSettings;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileEndEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileMoveEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WeaponProjectile extends AProjectile {

    private static final boolean useMoveEvent = !WeaponMechanics.getBasicConfigurations().getBool("Disabled_Events.Projectile_Move_Event");
    private static final IProjectileCompatibility projectileCompatibility = WeaponCompatibilityAPI.getProjectileCompatibility();

    private final ItemStack weaponStack;
    private final String weaponTitle;

    private final Sticky sticky;
    private final Through through;
    private final Bouncy bouncy;

    private StickedData stickedData;
    private int throughAmount;
    private int bounces;

    // These are for through and bouncy to deny collision with
    // same block or entity right after colliding with it
    private Location lastBlock;
    private int lastEntity = -1;

    public WeaponProjectile(ProjectileSettings projectileSettings, LivingEntity shooter, Location location,
                            Vector motion, ItemStack weaponStack, String weaponTitle,
                            Sticky sticky, Through through, Bouncy bouncy) {
        super(projectileSettings, shooter, location, motion);
        this.weaponStack = weaponStack;
        this.weaponTitle = weaponTitle;
        this.sticky = sticky;
        this.through = through;
        this.bouncy = bouncy;
    }

    /**
     * Clones the settings of this weapon projectile and shoots again with
     * different location and motion.
     *
     * @param location the cloned projectile's new start location
     * @param motion the cloned projectile's new motion
     * @return the cloned projectile
     */
    public WeaponProjectile cloneSettingsAndShoot(Location location, Vector motion) {
        WeaponProjectile projectile = new WeaponProjectile(getProjectileSettings(), getShooter(), location, motion, weaponStack, weaponTitle, sticky, through, bouncy);
        WeaponMechanics.getProjectilesRunnable().addProjectile(projectile);
        return projectile;
    }

    /**
     * @return the item stack used to shoot this projectile
     */
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

    @Override
    public boolean handleCollisions(boolean disableEntityCollisions) {

        Vector possibleNextLocation = getLocation().add(getMotion());
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
            }
            return false;
        }

        // Returns sorted list of hits
        List<RayTraceResult> hits = getHits(disableEntityCollisions);
        if (hits == null) {

            // No hits, simply update location and distance travelled
            setRawLocation(possibleNextLocation);
            addDistanceTravelled(getMotionLength());

            return false;
        }

        boolean noFinalUpdate = false;
        double cacheMotionLength = getMotionLength();
        double distanceAlreadyAdded = 0;

        for (RayTraceResult hit : hits) {

            // Check if hitting same entity or block as last time
            if (equalToLast(hit)) continue;

            // Stay on track of current location and distance travelled on each loop
            setRawLocation(hit.getHitLocation());
            double add = hit.getDistanceTravelled() - distanceAlreadyAdded;
            addDistanceTravelled(distanceAlreadyAdded += add);

            if (hit.isBlock()) {
                onCollide(hit.getBlock());
            } else {
                onCollide(hit.getLivingEntity());
            }

            // Returned true and that most likely means that block hit was cancelled, skipping...
            if (hit.handleHit(this)) continue;

            // Sticky
            if (sticky != null && sticky.handleSticking(this, hit)) {
                // Break since projectile sticked to entity or block
                noFinalUpdate = true;
                break;
            }

            // Through
            if (through != null && through.handleThrough(this, hit)) {
                // Continue since projectile went through.
                // We still need to check that other collisions also allows this
                ++throughAmount;

                // Update last hit entity / block
                updateLast(hit);
                continue;
            }

            // Bouncy
            if (bouncy != null && bouncy.handleBounce(this, hit)) {
                // Break since projectile bounced to different direction
                ++bounces;

                // Update last hit entity / block
                updateLast(hit);

                noFinalUpdate = true;

                // Force disguise raw update on bounces
                updateDisguise(true);
                break;
            }

            // Projectile should die if code reaches this point
            return true;
        }

        // Check that projectile didn't stick, or just bounce
        if (!noFinalUpdate) {
            // Here we know that projectile didn't die on any collision.
            // We still have to update the location to last possible location.
            setRawLocation(possibleNextLocation);
            addDistanceTravelled(cacheMotionLength - distanceAlreadyAdded);
        }

        return false;
    }

    private void updateLast(RayTraceResult hit) {
        if (hit.isBlock()) {
            lastBlock = hit.getBlock().getLocation();
        } else {
            lastEntity = hit.getLivingEntity().getEntityId();
        }
    }

    private boolean equalToLast(RayTraceResult hit) {
        if (hit.isBlock()) {
            Location hitBlock = hit.getBlock().getLocation();
            return lastBlock != null && lastBlock.getBlockX() == hitBlock.getBlockX() && lastBlock.getBlockY() == hitBlock.getBlockY() && lastBlock.getBlockZ() == hitBlock.getBlockZ();
        }
        return lastEntity != -1 && lastEntity == hit.getLivingEntity().getEntityId();
    }

    private List<RayTraceResult> getHits(boolean disableEntityCollisions) {
        List<RayTraceResult> hits = null;

        Vector normalizedMotion = getNormalizedMotion();
        Vector location = getLocation();

        double distance = Math.ceil(getMotionLength());

        // If distance is 0 or below, it will cause issues
        if (NumberUtil.equals(distance, 0.0)) distance = 1;

        BlockIterator blocks = new BlockIterator(getWorld(), location, normalizedMotion, 0.0, (int) distance);
        int maximumBlockHits = through == null ? 1 : through.getMaximumThroughAmount() + 1;

        while (blocks.hasNext()) {
            Block block = blocks.next();

            HitBox blockBox = projectileCompatibility.getHitBox(block);
            if (blockBox == null) continue;

            RayTraceResult rayTraceResult = blockBox.rayTrace(location, normalizedMotion);
            if (rayTraceResult == null) continue; // Didn't hit

            if (hits == null) hits = new ArrayList<>(1);
            hits.add(rayTraceResult);

            // If through isn't used, it is enough to get one block hit
            if (through == null) break;

            // If it now reached maximum block hits, simply break since we know
            // projectile can't go through blocks anymore after this one.
            if (--maximumBlockHits == 0) break;

            // If it isn't valid it can't go through
            if (!through.quickValidCheck(block.getType())) break;
        }

        if (!disableEntityCollisions) {
            List<LivingEntity> entities = getPossibleEntities();
            if (entities != null && !entities.isEmpty()) {
                for (LivingEntity entity : entities) {
                    HitBox entityBox = projectileCompatibility.getHitBox(entity);
                    if (entityBox == null) continue;

                    RayTraceResult rayTraceResult = entityBox.rayTrace(location, normalizedMotion);
                    if (rayTraceResult == null) continue; // Didn't hit

                    if (hits == null) hits = new ArrayList<>(1);
                    hits.add(rayTraceResult);
                }
            }
        }

        // Sort based on the distance travelled before hit if there is more than 1 hits
        if (hits != null && hits.size() > 1) hits.sort((hit1, hit2) -> (int) (hit1.getDistanceTravelled() - hit2.getDistanceTravelled()));

        return hits;
    }

    private List<LivingEntity> getPossibleEntities() {

        // Get the box of current location to end of this iteration
        HitBox hitBox = new HitBox(getLocation(), getLocation().add(getMotion()));

        int minX = floor((hitBox.getMinX() - 2.0D) / 16.0D);
        int maxX = floor((hitBox.getMaxX() + 2.0D) / 16.0D);
        int minZ = floor((hitBox.getMinZ() - 2.0D) / 16.0D);
        int maxZ = floor((hitBox.getMaxZ() + 2.0D) / 16.0D);

        List<LivingEntity> entities = new ArrayList<>(8);

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                Chunk chunk = getWorld().getChunkAt(x, z);
                for (final Entity entity : chunk.getEntities()) {
                    if (!entity.getType().isAlive() || entity.isInvulnerable()
                            || (getAliveTicks() < 10 && entity.getEntityId() == getShooter().getEntityId())) continue;

                    entities.add((LivingEntity) entity);
                }
            }
        }

        return entities.isEmpty() ? null : entities;
    }

    private int floor(double toFloor) {
        int flooredValue = (int) toFloor;
        return toFloor < flooredValue ? flooredValue - 1 : flooredValue;
    }

    @Override
    public void onMove() {
        if (useMoveEvent) Bukkit.getPluginManager().callEvent(new ProjectileMoveEvent(this));

        Vector loc = getLocation();
        getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 1f), true);
    }

    @Override
    public void onEnd() {
        Bukkit.getPluginManager().callEvent(new ProjectileEndEvent(this));
    }
}