package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.utils.*;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamage;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.BlockRegenSorter;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.LayerDistanceSorter;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.RegenerationData;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectilesRunnable;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.Projectile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Explosion {

    private static DamageHandler damageHandler = new DamageHandler();

    private String weaponTitle;
    private ExplosionShape shape;
    private ExplosionExposure exposure;
    private BlockDamage blockDamage;
    private RegenerationData regeneration;
    private Set<ExplosionTrigger> triggers;
    private int delay;
    private boolean isKnockback;
    private Optional<ClusterBomb> cluster;
    private Optional<AirStrike> airStrike;

    public Explosion(@Nullable String weaponTitle,
                     @Nonnull ExplosionShape shape,
                     @Nonnull ExplosionExposure exposure,
                     @Nullable BlockDamage blockDamage,
                     @Nullable RegenerationData regeneration,
                     @Nonnull Set<ExplosionTrigger> triggers,
                     @Nonnegative int delay,
                     boolean isKnockback) {

        this.weaponTitle = weaponTitle;
        this.shape = shape;
        this.exposure = exposure;
        this.blockDamage = blockDamage;
        this.regeneration = regeneration;
        this.triggers = triggers;
        this.delay = delay;
        this.isKnockback = isKnockback;
        this.cluster = Optional.empty();
        this.airStrike = Optional.empty();
    }

    public String getWeaponTitle() {
        return weaponTitle;
    }

    public ExplosionShape getShape() {
        return shape;
    }

    public void setShape(ExplosionShape shape) {
        this.shape = shape;
    }

    public ExplosionExposure getExposure() {
        return exposure;
    }

    public void setExposure(ExplosionExposure exposure) {
        this.exposure = exposure;
    }

    public ClusterBomb getCluster() {
        return cluster.orElse(null);
    }

    public void setCluster(ClusterBomb cluster) {
        this.cluster = Optional.ofNullable(cluster);
    }

    public BlockDamage getBlockDamage() {
        return blockDamage;
    }

    public void setBlockDamage(BlockDamage blockDamage) {
        this.blockDamage = blockDamage;
    }

    public RegenerationData getRegeneration() {
        return regeneration;
    }

    public void setRegeneration(RegenerationData regeneration) {
        this.regeneration = regeneration;
    }

    public Set<ExplosionTrigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(Set<ExplosionTrigger> triggers) {
        this.triggers = triggers;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean isKnockback() {
        return isKnockback;
    }

    public void setKnockback(boolean knockback) {
        isKnockback = knockback;
    }

    /**
     * Triggers the explosion at the given location
     *
     * @param cause Whoever caused the explosion
     * @param origin The center of the explosion
     */
    public void explode(LivingEntity cause, Location origin, ICustomProjectile projectile) {
        debug.log(LogLevel.DEBUG, "Generating a " + shape + " explosion at " + origin.getBlock());

        List<Block> blocks = blockDamage == null ? new ArrayList<>() : shape.getBlocks(origin);
        Map<LivingEntity, Double> entities = exposure.mapExposures(origin, shape);

        final List<Block> transparent = new ArrayList<>();
        final List<Block> solid = new ArrayList<>();

        // Sort out the blocks to destroy for later usage
        for (Block block : blocks) {
            Material type = block.getType();

            if (type.isSolid()) {
                solid.add(block);
            } else if (!MaterialHelper.isAir(type)) {
                transparent.add(block);
            }
        }

        // Determine how soon we should start regenerating blocks
        int timeOffset;
        if (regeneration == null) {
            timeOffset = -1;
        } else {
            timeOffset = solid.size() / regeneration.getMaxBlocksPerUpdate() * regeneration.getInterval() + regeneration.getTicksBeforeStart();
        }

        // We damage transparent blocks separately from solid blocks
        // to help make regeneration more accurate... mainly redstone.
        // If there are block updates during regeneration, we want to
        // try our best to keep the blocks coming back correctly. Still,
        // redstone contraptions should be protected VIA worldguard
        int size = transparent.size();
        for (int i = 0; i < size; i++) {
            Block block = transparent.get(i);

            if (blockDamage.isBlacklisted(block) || BlockDamageData.isBroken(block)) {
                continue;
            }

            // This forces all transparent blocks to regenerate at once.
            // This fixes item sorters breaking and general hopper/redstone stuff
            blockDamage.damage(block, timeOffset);
        }

        // Sort out the blocks that are blown up. This sorter starts at
        // the bottom and works it's way up. It also regenerates blocks
        // further away from the origin first
        BlockRegenSorter sorter = new LayerDistanceSorter(origin, this);
        try {
            solid.sort(sorter);
        } catch (IllegalArgumentException ex) {
            debug.error("A plugin modified the explosion block sorter with an illegal sorter!",
                    "Please report this error to the developers of that plugin", "Sorter: " + sorter.getClass());
            debug.log(LogLevel.ERROR, ex);
        }

        // Handle the actual block damage.
        // todo Add block mask
        size = solid.size();
        for (int i = 0; i < size; i++) {
            Block block = solid.get(i);

            if (blockDamage.isBlacklisted(block) || BlockDamageData.isBroken(block)) {
                continue;
            }

            int regenTime;
            if (regeneration == null) {
                regenTime = -1;
            } else {
                regenTime = regeneration.getTicksBeforeStart() +
                        (i / regeneration.getMaxBlocksPerUpdate()) * regeneration.getInterval();
            }

            blockDamage.damage(block, regenTime);
        }

        // Handles damage and knockback to living entities. Knockback
        // is handled like vanilla knockback, and damage is very similar
        // to MC explosion damage (But we can actually use explosions with
        // no damage)
        if (weaponTitle != null) {
            damageHandler.tryUseExplosion(cause, weaponTitle, entities);

            if (isKnockback) {
                Vector originVector = origin.toVector();
                for (Map.Entry<LivingEntity, Double> entry : entities.entrySet()) {

                    LivingEntity entity = entry.getKey();
                    double exposure = entry.getValue();

                    // Normalized vector between the explosion and entity involved
                    Vector between = VectorUtils.setLength(entity.getLocation().toVector().subtract(originVector), exposure);
                    Vector motion = entity.getVelocity().add(between);

                    entity.setVelocity(motion);
                }
            }
        } else {

            // This likely occurs because of the command /wm test
            // Useful for debugging, and can help users decide which
            // size explosion they may want
            for (Map.Entry<LivingEntity, Double> entry : entities.entrySet()) {
                LivingEntity entity = entry.getKey();
                double impact = entry.getValue();

                entity.sendMessage(StringUtils.color("&cYou suffered " + impact * 100 + "% of the impact"));
            }
        }

        cluster.ifPresent(clusterBomb -> clusterBomb.trigger(projectile, cause, origin));
        airStrike.ifPresent(airStrike -> airStrike.trigger(origin, cause, projectile));
    }

    public enum ExplosionTrigger {

        /**
         * When the projectile is shot/thrown
         */
        SHOOT,

        /**
         * When the projectile hits a non-air and non-liquid block
         */
        BLOCK,

        /**
         * When the projectile hits an entity
         */
        ENTITY,

        /**
         * When the projectile hits a liquid
         */
        LIQUID
    }

    public class ClusterBomb {

        private Projectile projectile;
        private double speed;
        private int splits;
        private int bombs;

        public ClusterBomb(Projectile projectile, double speed, int splits, int bombs) {
            this.projectile = projectile;
            this.speed = speed;
            this.splits = splits;
            this.bombs = bombs;

            Explosion.this.cluster = Optional.of(this);
        }

        public Projectile getProjectile() {
            return projectile;
        }

        public void setProjectile(Projectile projectile) {
            this.projectile = projectile;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public int getSplits() {
            return splits;
        }

        public void setSplits(int splits) {
            this.splits = splits;
        }

        public int getBombs() {
            return bombs;
        }

        public void setBombs(int bombs) {
            this.bombs = bombs;
        }

        public void trigger(ICustomProjectile projectile, LivingEntity shooter, Location splitLocation) {

            int currentDepth = 0;

            if (projectile.getTag("cluster-split-level") != null) {
                currentDepth = Integer.parseInt(projectile.getTag("cluster-split-level"));
            }

            // Checking to see if we have split the proper number of times
            if (currentDepth >= splits) {
                return;
            }

            debug.debug("Splitting cluster bomb");

            for (int i = 0; i < bombs; i++) {
                Vector vector = VectorUtils.random(speed);

                // Either use the projectile settings from the "parent" projectile,
                // or use the projectile settings for this clusterbomb
                Projectile projectileSettings = this.projectile == null ? projectile.getProjectileSettings() : this.projectile;

                CustomProjectile split = new CustomProjectile(projectileSettings, shooter, splitLocation, vector, null, weaponTitle);
                CustomProjectilesRunnable.addProjectile(split);

                split.setTag("cluster-split-level", String.valueOf(currentDepth + 1));
            }

            // Remove the parent split
            projectile.remove();
        }
    }

    public class AirStrike {

        /**
         * The settings of the bomb that is dropped.
         */
        private Projectile projectileSettings;

        /**
         * Minimum/Maximum number of bombs dropped
         */
        private int min;
        private int max;

        /**
         * The height to drop the bomb from, defaults to 150
         */
        private double height;

        /**
         * The randomness/noise to add to the y position of the bomb
         */
        private double yVariation;

        /**
         * The minimum horizontal distance between bombs
         */
        private double distanceBetweenSquared;

        /**
         * The maximum horizontal distance away from the origin of the explosion
         * that a bomb can be dropped.
         */
        private double radius;

        /**
         * How many times to spawn in a volley of airstrikes
         */
        private int loops;

        /**
         * Delay between volleys (Defined by <code>loops</code>)
         */
        private int delay;


        public AirStrike(Projectile projectile, int min, int max, double height, double yVariation, double distanceBetween, double radius, int loops, int delay) {
            this.projectileSettings = projectile;
            this.min = min;
            this.max = max;
            this.height = height;
            this.yVariation = yVariation;
            this.distanceBetweenSquared = distanceBetween * distanceBetween;
            this.radius = radius;
            this.loops = loops;
            this.delay = delay;

            Explosion.this.airStrike = Optional.of(AirStrike.this);
        }

        public Projectile getProjectile() {
            return projectileSettings;
        }

        public void setProjectile(Projectile projectile) {
            this.projectileSettings = projectile;
        }

        public int getMin() {
            return min;
        }

        public void setMin(int min) {
            this.min = min;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public double getYVariation() {
            return yVariation;
        }

        public void setYVariation(double yVariation) {
            this.yVariation = yVariation;
        }

        public double getDistanceBetween() {

            // A small price to pay for salvation
            return Math.sqrt(distanceBetweenSquared);
        }

        public void setDistanceBetween(double distanceBetween) {
            this.distanceBetweenSquared = distanceBetween * distanceBetween;
        }

        public double getRadius() {
            return radius;
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public int getLoops() {
            return loops;
        }

        public void setLoops(int loops) {
            this.loops = loops;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }

        public void trigger(Location flareLocation, LivingEntity shooter, ICustomProjectile projectile) {
            new BukkitRunnable() {

                int count = 0;

                @Override
                public void run() {

                    if (count++ >= loops) {
                        cancel();
                        return;
                    }

                    int bombs = NumberUtils.random(min, max);
                    int checks = bombs * bombs;

                    // Used to make sure we don't spawn bombs too close to
                    // each other. Uses distanceBetweenSquared
                    List<Vector2d> spawnLocations = new ArrayList<>(bombs);

                    locationFinder:
                    for (int i = 0; i < checks && spawnLocations.size() < bombs; i++) {

                        double x = flareLocation.getX() + NumberUtils.random(-radius, radius);
                        double z = flareLocation.getZ() + NumberUtils.random(-radius, radius);

                        Vector2d vector = new Vector2d(x, z);

                        for (Vector2d spawnLocation : spawnLocations) {
                            if (vector.distanceSquared(spawnLocation) > distanceBetweenSquared) {
                                continue locationFinder;
                            }
                        }

                        spawnLocations.add(vector);

                        double y = flareLocation.getY() + height + NumberUtils.random(-yVariation, yVariation);
                        Location location = new Location(flareLocation.getWorld(), x, y, z);

                        Projectile projectileSettings = AirStrike.this.projectileSettings;
                        if (projectileSettings == null) {
                            projectileSettings = projectile.getProjectileSettings();
                        }

                        CustomProjectile projectile = new CustomProjectile(projectileSettings, shooter, location, new Vector(), null, weaponTitle);
                        CustomProjectilesRunnable.addProjectile(projectile);
                    }
                }
            }.runTaskTimerAsynchronously(WeaponMechanics.getPlugin(), 0, delay);
        }
    }
}
