package me.deecaad.weaponmechanics.weapon.newprojectile.weaponprojectile;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.newprojectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.newprojectile.HitBox;
import me.deecaad.weaponmechanics.weapon.newprojectile.ProjectileSettings;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WeaponProjectile extends AProjectile {

    private ItemStack weaponStack;
    private String weaponTitle;

    private Sticky sticky;
    private Through through;
    private Bouncy bouncy;

    private StickedData stickedData;
    private int throughAmount;
    private int bounces;

    public WeaponProjectile(ProjectileSettings projectileSettings, LivingEntity shooter, Location location,
                               Vector motion, ItemStack weaponStack, String weaponTitle) {
        super(projectileSettings, shooter, location, motion);
        this.weaponStack = weaponStack;
        this.weaponTitle = weaponTitle;

        Configuration config = WeaponMechanics.getConfigurations();
        sticky = config.getObject(weaponTitle + ".Projectile.Sticky", Sticky.class);
        through = config.getObject(weaponTitle + ".Projectile.Through", Through.class);
        bouncy = config.getObject(weaponTitle + ".Projectile.Bouncy", Bouncy.class);
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
    public boolean setStickedData(StickedData stickedData) {
        if (stickedData == null) {
            this.stickedData = null;
            // This basically removes sticky
            setMotion(new Vector(
                    ThreadLocalRandom.current().nextFloat() * 0.2,
                    ThreadLocalRandom.current().nextFloat() * 0.2,
                    ThreadLocalRandom.current().nextFloat() * 0.2
            ));
            return true;
        }

        // Just extra check if entity happens to die or block disappear
        if (stickedData.isBlockStick()) {
            if (stickedData.getBlock() == null) {
                return false;
            }
        } else if (stickedData.getLivingEntity() == null) {
            return false;
        }

        // Now we can safely set the sticked data and other required values

        this.stickedData = stickedData;
        setLocation(stickedData.getNewLocation());
        setMotion(new Vector(0, 0, 0));
        return true;
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
    public boolean handleCollisions() {

        if (stickedData != null) {
            Vector newLocation = stickedData.getNewLocation();
            if (newLocation == null) {
                // If this happens, either entity is dead or block isn't there anymore
                setStickedData(null);
            } else if (!stickedData.isBlockStick()) {
                // Update location and update distance travelled if living entity
                addDistanceTravelled(getLastLocation().distance(newLocation));
                setLocation(newLocation);
            }
            return false;
        }

        // Returns sorted list of hits
        List<RayTraceResult> hits = getHits();
        if (hits == null) return false;

        double distanceAlreadyAdded = 0;

        for (RayTraceResult hit : hits) {

            setLocation(hit.getHitLocation());
            double add = hit.getDistanceTravelled() - distanceAlreadyAdded;
            addDistanceTravelled(distanceAlreadyAdded += add);

            // Returned true and that most likely means that block hit was cancelled, skipping...
            if (hit.handleHit(this)) continue;

            // Sticky
            if (sticky != null && sticky.handleSticking(this, hit)) {
                // Break since projectile sticked to entity or block
                break;
            }

            // Through
            if (through != null && through.handleThrough(this, hit)) {
                // Continue since projectile went through.
                // We still need to check that other collisions also allows this
                ++throughAmount;
                continue;
            }

            // Bouncy
            if (bouncy != null && bouncy.handleBounce(this, hit)) {
                // Break since projectile bounced to different direction
                ++bounces;
                break;
            }

            // Projectile should die if code reaches this point
            return true;
        }

        return false;
    }

    private List<RayTraceResult> getHits() {
        List<RayTraceResult> hits = null;

        Vector normalizedMotion = getNormalizedMotion();
        Vector location = getLocation();

        List<Block> blocks = getPossibleBlocks();
        if (blocks != null && !blocks.isEmpty()) {
            for (Block block : blocks) {
                HitBox blockBox = new HitBox(null, null); //projectileCompatibility.getHitBox(block);
                if (blockBox == null) continue;

                blockBox.setBlockHitBox(block);
                RayTraceResult rayTraceResult = blockBox.rayTrace(location, normalizedMotion);
                if (rayTraceResult == null) continue; // Didn't hit

                if (hits == null) hits = new ArrayList<>();
                hits.add(rayTraceResult);
            }
        }

        List<LivingEntity> entities = getPossibleEntities();
        if (entities != null && !entities.isEmpty()) {
            for (LivingEntity entity : entities) {
                HitBox entityBox = new HitBox(null, null); //projectileCompatibility.getHitBox(entity);
                if (entityBox == null) continue;

                entityBox.setLivingEntity(entity);
                RayTraceResult rayTraceResult = entityBox.rayTrace(location, normalizedMotion);
                if (rayTraceResult == null) continue; // Didn't hit

                if (hits == null) hits = new ArrayList<>();
                hits.add(rayTraceResult);
            }
        }

        // Sort based on distance to location if more than 1 hits
        if (hits != null && hits.size() > 1) hits.sort((hit1, hit2) -> (int) (hit1.getHitLocation().distanceSquared(location) - hit2.getHitLocation().distanceSquared(location)));

        return hits;
    }

    private List<Block> getPossibleBlocks() {

        List<Block> blocks = new ArrayList<>();
        // todo

        // If through is used -> only get Maximum_Through_Amount - throughAmount + 1 blocks

        return blocks.isEmpty() ? null : blocks;
    }

    private List<LivingEntity> getPossibleEntities() {

        // Get the box of current location to end of this iteration
        HitBox hitBox = new HitBox(getLocation(), getLocation().add(getMotion()));

        int minX = floor((hitBox.getMinX() - 2.0D) / 16.0D);
        int maxX = floor((hitBox.getMaxX() + 2.0D) / 16.0D);
        int minZ = floor((hitBox.getMinZ() - 2.0D) / 16.0D);
        int maxZ = floor((hitBox.getMaxZ() + 2.0D) / 16.0D);

        List<LivingEntity> entities = new ArrayList<>();

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
}