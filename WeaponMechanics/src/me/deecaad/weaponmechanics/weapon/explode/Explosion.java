package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.entity.EntityCompatibility;
import me.deecaad.compatibility.entity.FallingBlockWrapper;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.MaterialHelper;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.core.utils.VectorUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamage;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.explode.exposures.ExplosionExposure;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.BlockRegenSorter;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.LayerDistanceSorter;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.RegenerationData;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ExplosionShape;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.Projectile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    private double blockChance;
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
                     double blockChance,
                     boolean isKnockback) {

        this.weaponTitle = weaponTitle;
        this.shape = shape;
        this.exposure = exposure;
        this.blockDamage = blockDamage;
        this.regeneration = regeneration;
        this.triggers = triggers;
        this.delay = delay;
        this.blockChance = blockChance;
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

    public double getBlockChance() {
        return blockChance;
    }

    public void setBlockChance(double blockChance) {
        this.blockChance = blockChance;
    }

    public boolean isKnockback() {
        return isKnockback;
    }

    public void setKnockback(boolean knockback) {
        isKnockback = knockback;
    }

    public Optional<ClusterBomb> getCluster() {
        return cluster;
    }

    public void setCluster(ClusterBomb cluster) {
        this.cluster = Optional.of(cluster);
    }

    public Optional<AirStrike> getAirStrike() {
        return airStrike;
    }

    public void setAirStrike(AirStrike airStrike) {
        this.airStrike = Optional.of(airStrike);
    }

    /**
     * Triggers the explosion at the given location
     *
     * @param cause Whoever caused the explosion
     * @param origin The center of the explosion
     */
    public void explode(LivingEntity cause, Location origin, ICustomProjectile projectile) {
        debug.log(LogLevel.DEBUG, "Generating a " + shape + " explosion at " + origin.getBlock());

        EntityCompatibility entityCompatibility = CompatibilityAPI.getCompatibility().getEntityCompatibility();

        List<Block> blocks = blockDamage == null ? new ArrayList<>() : shape.getBlocks(origin);
        Map<LivingEntity, Double> entities = exposure.mapExposures(origin, shape);
        BlockRegenSorter sorter = new LayerDistanceSorter(origin, this);

        List<Block> transparent = new ArrayList<>();
        List<Block> solid = new ArrayList<>();
        Map<FallingBlockWrapper, Vector> fallingBlocks = new HashMap<>();

        // Separate the blocks to destroy into solid blocks (blocks that can be safely
        // removed without worry) and transparent blocks (blocks that are more likely
        // to get removed from block updates)
        // todo Check for redstone contraptions
        for (Block block : blocks) {
            Material type = block.getType();

            if (type.isSolid()) {
                solid.add(block);
            } else if (!MaterialHelper.isAir(type)) {
                transparent.add(block);
            }
        }

        // Sorting the blocks to create a satisfying pattern during regeneration.
        // By default, this sorts from bottom to the top
        try {
            solid.sort(sorter);
        } catch (IllegalArgumentException ex) {
            debug.error("A plugin modified the explosion block sorter with an illegal sorter!",
                    "Please report this error to the developers of that plugin", "Sorter: " + sorter.getClass());
            debug.log(LogLevel.ERROR, ex);
        }

        damageBlocks(transparent, true, origin, projectile, fallingBlocks);
        damageBlocks(solid, false, origin, projectile, fallingBlocks);

        @SuppressWarnings("unchecked")
        Iterable<Player> playersInView = (Collection<Player>) (Collection<?>) origin.getWorld().getNearbyEntities(origin, 100, 100, 100, entity -> entity.getType() == EntityType.PLAYER);

        // Handl
        for (Map.Entry<FallingBlockWrapper, Vector> entry : fallingBlocks.entrySet()) {
            Object nms = entry.getKey().getEntity();
            int removeTime = NumberUtils.minMax(0, entry.getKey().getTimeToHitGround(), 200);
            Vector velocity = entry.getValue();

            if (removeTime == 0) continue;

            // All the packets needed to handle showing the falling block
            // to the player. The destroy packet is sent later, when the block
            // hits the ground. Sent to every player in view.
            Object spawn = entityCompatibility.getSpawnPacket(nms);
            Object meta = entityCompatibility.getMetadataPacket(nms);
            Object motion = entityCompatibility.getVelocityPacket(nms, velocity);
            Object destroy = entityCompatibility.getDestroyPacket(nms);

            for (Player player : playersInView) {

                CompatibilityAPI.getCompatibility().sendPackets(player, spawn, meta, motion);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        CompatibilityAPI.getCompatibility().sendPackets(player, destroy);
                    }
                }.runTaskLaterAsynchronously(WeaponMechanics.getPlugin(), removeTime);
            }
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

            // This occurs because of the command /wm test
            // Useful for debugging, and can help users decide which
            // size explosion they may want
            for (Map.Entry<LivingEntity, Double> entry : entities.entrySet()) {
                LivingEntity entity = entry.getKey();
                double impact = entry.getValue();

                entity.sendMessage(StringUtils.color("&cYou suffered " + impact * 100 + "% of the impact"));
            }
        }

        if (projectile != null) {
            cluster.ifPresent(clusterBomb -> clusterBomb.trigger(projectile, cause, origin));
            airStrike.ifPresent(airStrike -> airStrike.trigger(origin, cause, projectile));
        }
    }

    protected void damageBlocks(List<Block> blocks, boolean isAtOnce, Location origin, ICustomProjectile projectile, Map<FallingBlockWrapper, Vector> fallingBlocks) {

        int timeOffset = regeneration == null ? -1 : regeneration.getTicksBeforeStart();

        EntityCompatibility entityCompatibility = CompatibilityAPI.getCompatibility().getEntityCompatibility();
        int size = blocks.size();
        for (int i = 0; i < size; i++) {
            Block block = blocks.get(i);

            int counter = isAtOnce ? size : i;
            int time = timeOffset + counter / regeneration.getMaxBlocksPerUpdate() * regeneration.getInterval();

            if (blockDamage.damage(block, time) && NumberUtils.chance(blockChance)) {

                Location loc = block.getLocation().add(0.5, 0.5, 0.5);
                BlockState state = block.getState();
                Vector velocity = loc.toVector().subtract(origin.toVector());
                velocity.setY(2.2);

                if (projectile != null) {
                    projectile.getMotion().multiply(-1);
                }

                FallingBlockWrapper wrapper = entityCompatibility.createFallingBlock(loc, state, velocity, 200);
                fallingBlocks.put(wrapper, velocity);
            }
        }
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
                (this.projectile == null ? projectile.getProjectileSettings() : this.projectile).shoot(shooter, splitLocation, vector, projectile.getWeaponStack(), weaponTitle).setTag("cluster-split-level", String.valueOf(currentDepth + 1));
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

                        (getProjectile() == null ? projectile.getProjectileSettings() : getProjectile()).shoot(shooter, location, new Vector(), projectile.getWeaponStack(), weaponTitle);
                    }
                }
            }.runTaskTimerAsynchronously(WeaponMechanics.getPlugin(), 0, delay);
        }
    }
}
