package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.EntityCompatibility;
import me.deecaad.core.compatibility.entity.FallingBlockWrapper;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.*;
import me.deecaad.core.utils.primitive.DoubleEntry;
import me.deecaad.core.utils.primitive.DoubleMap;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DefaultExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DistanceExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.ExplosionExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.ExposureFactory;
import me.deecaad.weaponmechanics.weapon.explode.exposures.VoidExposure;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.LayerDistanceSorter;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.RegenerationData;
import me.deecaad.weaponmechanics.weapon.explode.shapes.*;
import me.deecaad.weaponmechanics.weapon.projectile.CollisionData;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileExplodeEvent;
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

    public void explode(LivingEntity cause, Location origin, ICustomProjectile projectile) {

        // If the projectile uses airstrikes, then the airstrike should be
        // triggered instead od the explosion.
        if (projectile != null && airStrike != null && !"true".equals(projectile.getTag("airstrike-bomb"))) {
            airStrike.trigger(origin, cause, projectile);
            return;
        }

        // This event is not cancellable. If developers want to cancel
        // explosions, they should
        ProjectileExplodeEvent event = new ProjectileExplodeEvent(shape.getBlocks(origin),
                new LayerDistanceSorter(origin, this), exposure.mapExposures(origin, shape));

        List<Block> blocks      = event.getBlocks();
        int initialCapacity     = Math.max(blocks.size(), 10);
        List<Block> transparent = new ArrayList<>(initialCapacity);
        List<Block> solid       = new ArrayList<>(initialCapacity);

        // Sort the blocks into different categories (To make regeneration more
        // reliable). In the future, this may also be used to filter out
        // redstone contraptions.
        for (Block block : blocks) {
            if (block.getType().isSolid())
                solid.add(block);
            else if (!block.isEmpty())
                transparent.add(block);
        }

        // Sorting the blocks can make regeneration look better
        try {

            // Not an error, BUT probably not intentional
            if (event.getSorter() == null) {
                debug.debug("Null sorter used while regenerating explosion... Was this intentional?");
            } else {
                solid.sort(event.getSorter());
            }
        } catch (IllegalArgumentException e) {
            debug.log(LogLevel.ERROR, "A plugin modified the explosion block sorter with an illegal sorter! " +
                    "Please report this error to the developers of that plugin. Sorter: " + event.getSorter().getClass(), e);
        }

        Map<FallingBlockData, Vector> fallingBlocks = new HashMap<>((int) (blockChance * 1.1 * blocks.size()));
        int timeOffset = regeneration == null ? -1 : (solid.size() * regeneration.getInterval() / regeneration.getMaxBlocksPerUpdate());

        damageBlocks(transparent, true, origin, projectile, fallingBlocks, timeOffset);
        damageBlocks(solid, false, origin, projectile, fallingBlocks, 0);
        spawnFallingBlocks(fallingBlocks, origin);

        DoubleMap<LivingEntity> entities = event.getEntities();
        if (projectile != null && projectile.getWeaponTitle() != null) {
            WeaponMechanics.getWeaponHandler().getDamageHandler().tryUseExplosion(projectile, origin, entities);

            if (isKnockback) {
                Vector originVector = origin.toVector();
                for (DoubleEntry<LivingEntity> entry : entities.entrySet()) {

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
            for (DoubleEntry<LivingEntity> entry : entities.entrySet()) {
                LivingEntity entity = entry.getKey();
                double impact = entry.getValue();

                entity.sendMessage(StringUtil.color("&cYou suffered " + impact * 100 + "% of the impact"));
            }
        }

        if (flashbang != null) flashbang.trigger(exposure, projectile, origin);
        if (mechanics != null) mechanics.use(new CastData(WeaponMechanics.getEntityWrapper(cause), projectile.getWeaponTitle(), projectile.getWeaponStack()));
    }

    protected void damageBlocks(List<Block> blocks, boolean isAtOnce, Location origin, ICustomProjectile projectile, Map<FallingBlockData, Vector> fallingBlocks, int timeOffset) {
        boolean isRegenerate = regeneration != null;


        if (isRegenerate)
            timeOffset += regeneration.getTicksBeforeStart();

        List<BlockDamageData.DamageData> brokenBlocks = isRegenerate ? new ArrayList<>(regeneration.getMaxBlocksPerUpdate()) : null;

        int size = blocks.size();
        for (int i = 0; i < size; i++) {
            Block block = blocks.get(i);

            // Getting the state of the block BEFORE the block is broken is important,
            // otherwise we are just getting an air block, which is useless
            BlockState state = block.getState();
            BlockDamageData.DamageData data = blockDamage.damage(block);

            // Group blocks together to reduce task scheduling (#28). After
            // reaching the bound, we can schedule a task to generate later.
            if (isRegenerate) {
                brokenBlocks.add(data);

                if (brokenBlocks.size() == regeneration.getMaxBlocksPerUpdate() || i == size - 1) {
                    int time = timeOffset + ((isAtOnce ? size : i) / regeneration.getMaxBlocksPerUpdate() * regeneration.getInterval());

                    List<BlockDamageData.DamageData> finalBrokenBlocks = new ArrayList<>(brokenBlocks);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (BlockDamageData.DamageData block : finalBrokenBlocks) {

                                // The blocks may have been regenerated already
                                if (block.isBroken()) {
                                    block.regenerate();
                                    block.remove();
                                }
                            }
                        }
                    }.runTaskLater(WeaponMechanics.getPlugin(), time);

                    // Reset back to 0 elements, so we can continue adding
                    // blocks to regenerate to the list.
                    brokenBlocks.clear();
                }
            }

            // Handling falling blocks super expensive on the CPU... TODO
            if (data.isBroken() && NumberUtil.chance(blockChance)) {

                Location loc = block.getLocation().add(0.5, 0.5, 0.5);
                Vector velocity = loc.toVector().subtract(origin.toVector()).normalize(); // normalize to slow down

                if (projectile != null) {
                    Vector motion = projectile.getMotion().multiply(-1).normalize();
                    velocity.add(motion);
                }

                // We want to store the data, and calculate the falling blocks later
                // so the falling blocks don't interact with blocks that are going to be
                // blown up (but aren't blown up yet by this explosion)
                FallingBlockData falling = new FallingBlockData(velocity, state, loc);
                fallingBlocks.put(falling, velocity);
            }
        }
    }

    protected void spawnFallingBlocks(Map<FallingBlockData, Vector> fallingBlocks, Location origin) {
        EntityCompatibility entityCompatibility = CompatibilityAPI.getEntityCompatibility();

        // We cannot do this async, unfortunately
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

    @Override
    public String getKeyword() {
        return "Explosion";
    }

    @Override
    public Explosion serialize(File file, ConfigurationSection configurationSection, String path) {
        ConfigurationSection section = configurationSection.getConfigurationSection(path);
        assert section != null;

        // Get all possibly applicable data for the explosions,
        // and warn users for "odd" values
        ConfigurationSection typeData = section.getConfigurationSection("Explosion_Type_Data");
        if (typeData == null) {
            debug.log(LogLevel.ERROR, "Missing Explosion_Type_Data, a required argument.", StringUtil.foundAt(file, path));
            return null;
        }
        double yield  = typeData.getDouble("Yield",  3.0);
        double angle  = typeData.getDouble("Angle",  0.5);
        double depth  = typeData.getDouble("Depth",  -3.0);
        double height = typeData.getDouble("Height", 3.0);
        double width  = typeData.getDouble("Width",  3.0);
        double radius = typeData.getDouble("Radius", 3.0);
        int rays = typeData.getInt("Rays", 16);

        String found = StringUtil.foundAt(file, path + ".Explosion_Type_Data.");

        debug.validate(yield > 0, "Explosion Yield should be a positive number!", found + "Yield");
        debug.validate(angle > 0, "Explosion Angle should be a positive number!", found + "Angle");
        debug.validate(height > 0, "Explosion Height should be a positive number!", found + "Depth");
        debug.validate(width > 0, "Explosion Width should be a positive number!", found + "Width");
        debug.validate(radius > 0, "Explosion Radius should be a positive number!", found + "Height");
        debug.validate(rays > 0, "Explosion Rays should be a positive number!", found + "Rays");

        // We only want to turn users about big numbers, since users MIGHT
        // want to use these numbers.
        debug.validate(LogLevel.WARN, yield < 50,  StringUtil.foundLarge(yield, file, path + "Explosion_Type_Data.Yield"));
        debug.validate(LogLevel.WARN, angle < 50,  StringUtil.foundLarge(angle, file, path + "Explosion_Type_Data.Angle"));
        debug.validate(LogLevel.WARN, height < 50, StringUtil.foundLarge(height, file, path + "Explosion_Type_Data.Height"));
        debug.validate(LogLevel.WARN, width < 50,  StringUtil.foundLarge(width, file, path + "Explosion_Type_Data.Width"));
        debug.validate(LogLevel.WARN, radius < 50, StringUtil.foundLarge(radius, file, path + "Explosion_Type_Data.Radius"));
        debug.validate(LogLevel.WARN, rays < 50,   StringUtil.foundLarge(rays, file, path + "Explosion_Type_Data.Rays"));

        // We want to ensure each value in the section is their expected type.
        // For example, sometimes a configuration section will hold a value as
        // an int when we would expect it to be a double.
        if (typeData.contains("Yield", true))  typeData.set("Yield", yield);
        if (typeData.contains("Angle", true))  typeData.set("Angle", angle);
        if (typeData.contains("Depth", true))  typeData.set("Depth", -Math.abs(depth));
        if (typeData.contains("Height", true)) typeData.set("Height", height);
        if (typeData.contains("Width", true))  typeData.set("Width", width);
        if (typeData.contains("Radius", true)) typeData.set("Radius", radius);

        // Rays is a default value, so we don't need to check if typeData.contains("Rays").
        typeData.set("Rays", rays);

        Map<String, Object> temp = typeData.getValues(true);
        ExplosionExposure exposure;
        ExplosionShape shape;

        try {
            exposure = ExposureFactory.getInstance().get(section.getString("Explosion_Exposure", "DEFAULT"), temp);
            shape = ShapeFactory.getInstance().get(section.getString("Explosion_Shape", "DEFAULT"), temp);
        } catch (Factory.FactoryException e) {
            debug.error(e.getMessage(), "You failed to specify the " + e.getMissingArgument(),
                    "You instead specified: " + e.getValues(), StringUtil.foundAt(file, path + ".Explosion_type_Data"));
            return null;
        }

        if (exposure == null) {
            debug.error("Invalid explosion exposure \"" + section.getString("Explosion_Exposure", "DEFAULT") + "\"",
                    "Did you mean: " + StringUtil.didYouMean(section.getString("Explosion_Exposure", "DEFAULT"), ExposureFactory.getInstance().getOptions()),
                    "Valid Options: " + ExposureFactory.getInstance().getOptions(),
                    "Found at: " + StringUtil.foundAt(file, path + ".Explosion_Exposure"));
            return null;
        } else if (shape == null) {
            debug.error("Invalid explosion shape \"" + section.getString("Explosion_Shape") + "\"",
                    "Did You Mean: " + StringUtil.didYouMean(section.getString("Explosion_Shape", "DEFAULT"), ShapeFactory.getInstance().getOptions()),
                    "Valid Options: " + ShapeFactory.getInstance().getOptions(),
                    "Found at: " + StringUtil.foundAt(file, path + ".Explosion_Shape"));
            return null;
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
        if (impactWhenSection == null) {
            debug.error("Missing \"Detonation.Impact_When\" section.",
                    "We need it to determine when the projectile explodes.");
            return null;
        }

        Set<Explosion.ExplosionTrigger> triggers = new HashSet<>(4, 1.0f);
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
