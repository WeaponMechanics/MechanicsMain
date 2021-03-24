package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.entity.EntityCompatibility;
import me.deecaad.compatibility.entity.FallingBlockWrapper;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.*;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DefaultExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DistanceExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.ExplosionExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.VoidExposure;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.BlockRegenSorter;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.LayerDistanceSorter;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.RegenerationData;
import me.deecaad.weaponmechanics.weapon.explode.shapes.*;
import me.deecaad.weaponmechanics.weapon.projectile.CollisionData;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileExplodeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.function.Supplier;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Explosion implements Serializer<Explosion> {

    private static final DamageHandler damageHandler = new DamageHandler();

    private ExplosionShape shape;
    private ExplosionExposure exposure;
    private BlockDamage blockDamage;
    private RegenerationData regeneration;
    private Set<ExplosionTrigger> triggers;
    private int delay;
    private double blockChance;
    private boolean isKnockback;
    private ClusterBomb cluster;
    private AirStrike airStrike;
    private Flashbang flashbang;
    private Mechanics mechanics;

    public Explosion() { }

    public Explosion(@Nonnull ExplosionShape shape,
                     @Nonnull ExplosionExposure exposure,
                     @Nullable BlockDamage blockDamage,
                     @Nullable RegenerationData regeneration,
                     @Nonnull Set<ExplosionTrigger> triggers,
                     @Nonnegative int delay,
                     double blockChance,
                     boolean isKnockback,
                     @Nullable ClusterBomb clusterBomb,
                     @Nullable AirStrike airStrike,
                     @Nullable Flashbang flashbang,
                     @Nullable Mechanics mechanics) {

        this.shape = shape;
        this.exposure = exposure;
        this.blockDamage = blockDamage;
        this.regeneration = regeneration;
        this.triggers = triggers;
        this.delay = delay;
        this.blockChance = blockChance;
        this.isKnockback = isKnockback;
        this.cluster = clusterBomb;
        this.airStrike = airStrike;
        this.flashbang = flashbang;
        this.mechanics = mechanics;
    }

    public ExplosionShape getShape() {
        return shape;
    }

    public ExplosionExposure getExposure() {
        return exposure;
    }

    public BlockDamage getBlockDamage() {
        return blockDamage;
    }

    public RegenerationData getRegeneration() {
        return regeneration;
    }

    public Set<ExplosionTrigger> getTriggers() {
        return triggers;
    }

    public int getDelay() {
        return delay;
    }

    public double getBlockChance() {
        return blockChance;
    }

    public boolean isKnockback() {
        return isKnockback;
    }

    public ClusterBomb getCluster() {
        return cluster;
    }

    public AirStrike getAirStrike() {
        return airStrike;
    }

    public Flashbang getFlashbang() {
        return flashbang;
    }

    public Mechanics getMechanics() {
        return mechanics;
    }

    public void explode(LivingEntity cause, CollisionData collision, ICustomProjectile projectile) {
        if (collision == null || collision.getBlock() == null) {
            explode(cause, projectile.getLocation().toLocation(projectile.getWorld()), projectile);
        } else {
            BlockFace hitBlockFace = collision.getBlockFace();
            Vector location = projectile.getLocation().subtract(hitBlockFace.getDirection().multiply(0.5));
            explode(cause, location.toLocation(projectile.getWorld()), projectile);
        }
    }

    /**
     * Triggers the explosion at the given location
     *
     * @param cause Whoever caused the explosion
     * @param origin The center of the explosion
     */
    public void explode(LivingEntity cause, Location origin, ICustomProjectile projectile) {

        if (projectile != null) {
            if (airStrike != null && !"true".equals(projectile.getTag("airstrike-bomb"))) {
                airStrike.trigger(origin, cause, projectile);
                return;
            }

            ProjectileExplodeEvent explodeEvent = new ProjectileExplodeEvent(projectile, this);
            Bukkit.getPluginManager().callEvent(explodeEvent);
            if (explodeEvent.isCancelled()) {
                return;
            }
        }

        if (debug.canLog(LogLevel.DEBUG)) {
            debug.log(LogLevel.DEBUG, "Generating a " + shape + " explosion at " + origin.getBlock());
        }

        EntityCompatibility entityCompatibility = CompatibilityAPI.getCompatibility().getEntityCompatibility();

        List<Block> blocks = blockDamage == null ? new ArrayList<>() : shape.getBlocks(origin);
        Map<LivingEntity, Double> entities = exposure.mapExposures(origin, shape);
        BlockRegenSorter sorter = new LayerDistanceSorter(origin, this);

        List<Block> transparent = new ArrayList<>();
        List<Block> solid = new ArrayList<>();
        Map<FallingBlockData, Vector> fallingBlocks = new HashMap<>();

        // Separate the blocks to destroy into solid blocks (blocks that can be safely
        // removed without worry) and transparent blocks (blocks that are more likely
        // to get removed from block updates)
        // todo Check for redstone contraptions
        for (Block block : blocks) {
            if (block.getType().isSolid()) {
                solid.add(block);
            } else if (!block.isEmpty()) {
                transparent.add(block);
            }
        }

        // Sorting the blocks to create a satisfying pattern during regeneration.
        // By default, this sorts from bottom to the top
        try {
            solid.sort(sorter);
        } catch (IllegalArgumentException ex) {
            debug.log(LogLevel.ERROR, "A plugin modified the explosion block sorter with an illegal sorter! " +
                    "Please report this error to the developers of that plugin. Sorter: " + sorter.getClass(), ex);
        }

        damageBlocks(transparent, true, origin, projectile, fallingBlocks);
        damageBlocks(solid, false, origin, projectile, fallingBlocks);

        if (!fallingBlocks.isEmpty()) {

            // Handle falling blocks
            for (Map.Entry<FallingBlockData, Vector> entry : fallingBlocks.entrySet()) {
                FallingBlockWrapper wrapper = entry.getKey().get();
                Object nms = wrapper.getEntity();
                int removeTime = NumberUtil.minMax(0, wrapper.getTimeToHitGround(), 200);
                Vector velocity = entry.getValue();

                if (removeTime == 0) continue;

                // All the packets needed to handle showing the falling block
                // to the player. The destroy packet is sent later, when the block
                // hits the ground. Sent to every player in view.
                Object spawn = entityCompatibility.getSpawnPacket(nms);
                Object meta = entityCompatibility.getMetadataPacket(nms);
                Object motion = entityCompatibility.getVelocityPacket(nms, velocity);
                Object destroy = entityCompatibility.getDestroyPacket(nms);

                for (Entity entity : DistanceUtil.getEntitiesInRange(origin)) {
                    if (entity.getType() != EntityType.PLAYER) {
                        continue;
                    }
                    Player player = (Player) entity;
                    CompatibilityAPI.getCompatibility().sendPackets(player, spawn, meta, motion);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            CompatibilityAPI.getCompatibility().sendPackets(player, destroy);
                        }
                    }.runTaskLaterAsynchronously(WeaponMechanics.getPlugin(), removeTime);
                }
            }

        }

        // Handles damage and knockback to living entities. Knockback
        // is handled like vanilla knockback, and damage is very similar
        // to MC explosion damage (But we can actually use explosions with
        // no damage)
        if (projectile.getWeaponTitle() != null) {
            damageHandler.tryUseExplosion(projectile, origin, entities);

            if (isKnockback) {
                Vector originVector = origin.toVector();
                for (Map.Entry<LivingEntity, Double> entry : entities.entrySet()) {

                    LivingEntity entity = entry.getKey();
                    double exposure = entry.getValue();

                    // Normalized vector between the explosion and entity involved
                    Vector between = VectorUtil.setLength(entity.getLocation().toVector().subtract(originVector), exposure);
                    Vector motion = entity.getVelocity().add(between);

                    entity.setVelocity(motion);
                }
            }

            if (cluster != null) cluster.trigger(projectile, cause, origin);

        } else {

            // This occurs because of the command /wm test
            // Useful for debugging, and can help users decide which
            // size explosion they may want
            for (Map.Entry<LivingEntity, Double> entry : entities.entrySet()) {
                LivingEntity entity = entry.getKey();
                double impact = entry.getValue();

                entity.sendMessage(StringUtil.color("&cYou suffered " + impact * 100 + "% of the impact"));
            }
        }

        if (flashbang != null) flashbang.trigger(exposure, projectile, origin);
        if (mechanics != null) mechanics.use(new CastData(WeaponMechanics.getEntityWrapper(cause), projectile.getWeaponTitle(), projectile.getWeaponStack()));
    }

    protected void damageBlocks(List<Block> blocks, boolean isAtOnce, Location origin, ICustomProjectile projectile, Map<FallingBlockData, Vector> fallingBlocks) {

        int timeOffset = regeneration == null ? -1 : regeneration.getTicksBeforeStart();

        int size = blocks.size();
        for (int i = 0; i < size; i++) {
            Block block = blocks.get(i);
            int time = timeOffset;

            if (regeneration != null) {
                time += (isAtOnce ? size : i) / regeneration.getMaxBlocksPerUpdate() * regeneration.getInterval();
            }

            // Getting the state of the block BEFORE the block is broken is important,
            // otherwise we are just getting an air block, which is useless
            BlockState state = block.getState();

            if (blockDamage.damage(block, time) && NumberUtil.chance(blockChance)) {

                Location loc = block.getLocation().add(0.5, 0.5, 0.5);
                Vector velocity = loc.toVector().subtract(origin.toVector());

                if (projectile != null) {
                    Vector motion = projectile.getMotion().multiply(-1).normalize();
                    velocity.add(motion);
                }

                // We want to store the data, and calculate the falling blocks later
                // so the falling blocks don't interact with blocks that are going to be
                // blown up (but aren't blown up yet by this explosion)
                FallingBlockData data = new FallingBlockData(velocity, state, loc);
                fallingBlocks.put(data, velocity);
            }
        }
    }

    @Override
    public String getKeyword() {
        return "Explosion";
    }

    @Override
    public Explosion serialize(File file, ConfigurationSection configurationSection, String path) {
        ConfigurationSection section = configurationSection.getConfigurationSection(path);

        // Gets the explosion type from config, warns the user
        // if the type is invalid
        String shapeTypeName = section.getString("Explosion_Shape", "DEFAULT").trim().toUpperCase();
        ExplosionShapeType shapeType;
        try {
            shapeType = ExplosionShapeType.valueOf(shapeTypeName);
        } catch (IllegalArgumentException ex) {
            debug.log(LogLevel.ERROR, "The explosion shape \"" + shapeTypeName + "\" is invalid.",
                    "Valid shapes: " + Arrays.toString(ExplosionShapeType.values()),
                    StringUtil.foundAt(file, path));
            return null;
        }

        String exposureTypeName = section.getString("Explosion_Exposure", "DEFAULT").trim().toUpperCase();
        ExplosionExposureType exposureType;
        try {
            exposureType = ExplosionExposureType.valueOf(exposureTypeName);
        } catch (IllegalArgumentException ex) {
            debug.log(LogLevel.ERROR, "The explosion exposure \"" + exposureTypeName + "\" is invalid.",
                    "Valid exposures: " + Arrays.toString(ExplosionExposureType.values()),
                    StringUtil.foundAt(file, path));
            return null;
        }

        // Get all possibly applicable data for the explosions,
        // and warn users for "odd" values
        double yield  = section.getDouble("Explosion_Type_Data.Yield",  3.0);
        double angle  = section.getDouble("Explosion_Type_Data.Angle",  0.5);
        double depth  = section.getDouble("Explosion_Type_Data.Depth",  -3.0);
        double height = section.getDouble("Explosion_Type_Data.Height", 3.0);
        double width  = section.getDouble("Explosion_Type_Data.Width",  3.0);
        double radius = section.getDouble("Explosion_Type_Data.Radius", 3.0);
        int rays = section.getInt("Explosion_Type_Data.Rays", 16);

        if (depth > 0) depth *= -1;

        String found = StringUtil.foundAt(file, path + ".Explosion_Type_Data.");

        debug.validate(yield > 0, "Explosion Yield should be a positive number!", found + "Yield");
        debug.validate(angle > 0, "Explosion Angle should be a positive number!", found + "Angle");
        debug.validate(height > 0, "Explosion Height should be a positive number!", found + "Depth");
        debug.validate(width > 0, "Explosion Width should be a positive number!", found + "Width");
        debug.validate(radius > 0, "Explosion Radius should be a positive number!", found + "Height");
        debug.validate(rays > 0, "Explosion Rays should be a positive number!", found + "Rays");

        debug.validate(LogLevel.WARN, yield < 50, StringUtil.foundLarge(yield, file, path + "Explosion_Type_Data.Yield"));
        debug.validate(LogLevel.WARN, angle < 50, StringUtil.foundLarge(yield, file, path + "Explosion_Type_Data.Angle"));
        debug.validate(LogLevel.WARN, height < 50, StringUtil.foundLarge(yield, file, path + "Explosion_Type_Data.Height"));
        debug.validate(LogLevel.WARN, width < 50, StringUtil.foundLarge(yield, file, path + "Explosion_Type_Data.Width"));
        debug.validate(LogLevel.WARN, radius < 50, StringUtil.foundLarge(yield, file, path + "Explosion_Type_Data.Radius"));
        debug.validate(LogLevel.WARN, rays < 50, StringUtil.foundLarge(yield, file, path + "Explosion_Type_Data.Rays"));

        ExplosionShape shape;
        switch (shapeType) {
            case CUBE:
                shape = new CuboidExplosion(width, height);
                break;
            case SPHERE:
                shape = new SphericalExplosion(radius);
                break;
            case PARABOLA:
                shape = new ParabolicExplosion(depth, angle);
                break;
            case DEFAULT:
                shape = new DefaultExplosion(yield, rays);
                break;
            default:
                throw new IllegalArgumentException("Something went wrong...");
        }
        ExplosionExposure exposure;
        switch (exposureType) {
            case DISTANCE:
                exposure = new DistanceExposure();
                break;
            case DEFAULT:
                exposure = new DefaultExposure();
                break;
            case NONE:
                exposure = new VoidExposure();
                break;
            default:
                throw new IllegalArgumentException("Something went wrong...");
        }

        // Determine which blocks will be broken and how they will be regenerated
        BlockDamage blockDamage = null;
        if (section.contains("Block_Damage")) {
            blockDamage = new BlockDamage().serialize(file, configurationSection, path + ".Block_Damage");
        }
        RegenerationData regeneration = null;
        if (section.contains("Regeneration")) {
            regeneration = new RegenerationData().serialize(file, configurationSection, path + ".Regeneration");
        }

        // Determine when the projectile should explode
        ConfigurationSection impactWhenSection = section.getConfigurationSection("Detonation.Impact_When");
        Set<Explosion.ExplosionTrigger> triggers = new HashSet<>(4);
        for (String key : impactWhenSection.getKeys(false)) {
            try {
                Explosion.ExplosionTrigger trigger = Explosion.ExplosionTrigger.valueOf(key.toUpperCase());
                boolean value = impactWhenSection.getBoolean(key);

                if (value) triggers.add(trigger);
            } catch (IllegalArgumentException ex) {
                debug.log(LogLevel.ERROR, "Unknown trigger type \"" + key + "\"... Did you spell it correctly in config?");
                debug.log(LogLevel.DEBUG, ex);
            }
        }

        // Time after the trigger the explosion occurs
        int delay = section.getInt("Detonation.Delay_After_Impact");
        debug.validate(delay >= 0, "Delay should be positive", StringUtil.foundAt(file, path + ".Detonation.Delay_After_Impact"));

        double blockChance = section.getDouble("Block_Damage.Spawn_Falling_Block_Chance");
        boolean isKnockback = !section.getBoolean("Disable_Vanilla_Knockback");
        debug.validate(blockChance >= 0.0 && blockChance <= 1.0, "Falling block spawn chance should be [0, 1]",
                StringUtil.foundAt(file, path + "Block_Damage.Spawn_Falling_Block_Chance"));

        // A weird check, but I (somehow) made this mistake. Thought it was worth checking for
        if ((blockDamage == null || !blockDamage.isBreakBlocks()) && regeneration != null) {
            debug.error("Tried to use block regeneration for an explosion but blocks will not be broken.",
                    "This is almost certainly a misconfiguration!", StringUtil.foundAt(file, path));
        }

        ClusterBomb clusterBomb = new ClusterBomb().serialize(file, configurationSection, path + ".Cluster_Bomb");
        AirStrike airStrike = new AirStrike().serialize(file, configurationSection, path + ".Airstrike");
        Flashbang flashbang = new Flashbang().serialize(file, configurationSection, path + ".Flashbang");
        Mechanics mechanics = new Mechanics().serialize(file, configurationSection, path + ".Mechanics");

        return new Explosion(shape, exposure, blockDamage, regeneration, triggers, delay, blockChance, isKnockback,
                clusterBomb, airStrike, flashbang, mechanics);
    }

    private static class FallingBlockData implements Supplier<FallingBlockWrapper> {

        private final Vector velocity;
        private final BlockState state;
        private final Location loc;

        FallingBlockData(Vector velocity, BlockState state, Location loc) {
            this.velocity = velocity;
            this.state = state;
            this.loc = loc;
        }

        @Override
        public FallingBlockWrapper get() {
            return CompatibilityAPI.getEntityCompatibility().createFallingBlock(loc, state, velocity, 200);
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
}
