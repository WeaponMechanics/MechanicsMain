package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamage;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DefaultExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DistanceExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.ExplosionExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.VoidExposure;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.RegenerationData;
import me.deecaad.weaponmechanics.weapon.explode.shapes.CuboidExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.DefaultExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ExplosionShape;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ParabolicExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.SphericalExplosion;
import me.deecaad.weaponmechanics.weapon.projectile.Projectile;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

/**
 * This class serves to serialize different types
 * of explosions based on the configured
 * <code>ExplosionType</code>. If the type of
 * explosion is invalid, the user is notified.
 */
public class ExplosionSerializer implements Serializer<Explosion> {

    /**
     * Default constructor for serializer
     */
    public ExplosionSerializer() {
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

        String weaponTitle;
        try {
            weaponTitle = path.split("\\.")[0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            debug.warn("Tried to serialize explosion without weaponTitle! This is probably a mistake!");
            weaponTitle = null;
        }

        double blockChance = section.getDouble("Block_Damage.Spawn_Falling_Block_Chance");
        boolean isKnockback = !section.getBoolean("Disable_Vanilla_Knockback");
        debug.validate(blockChance >= 0.0 && blockChance <= 1.0, "Falling block spawn chance should be [0, 1]",
                StringUtil.foundAt(file, path + "Block_Damage.Spawn_Falling_Block_Chance"));

        // A weird check, but I (somehow) made this mistake. Thought it was worth checking for
        if ((blockDamage == null || !blockDamage.isBreakBlocks()) && regeneration != null) {
            debug.error("Tried to use block regeneration for an explosion but blocks will not be broken.",
                    "This is almost certainly a misconfiguration!", StringUtil.foundAt(file, path));
        }

        // Finally initialize the explosion
        Explosion explosion = new Explosion(weaponTitle, shape, exposure, blockDamage, regeneration, triggers, delay, blockChance, isKnockback);

        if (section.contains("Cluster_Bomb")) {

            Projectile projectileSettings = null;
            if (section.contains("Cluster_Bomb.Split_Projectile")) {
                projectileSettings = new Projectile().serialize(file, configurationSection, path + ".Cluster_Bomb.Split_Projectile");
            }

            double speed = section.getDouble("Cluster_Bomb.Projectile_Speed");
            int splits = section.getInt("Cluster_Bomb.Number_Of_Splits", 1);
            int bombs = section.getInt("Cluster_Bomb.Number_Of_Bombs");

            explosion.new ClusterBomb(projectileSettings, speed / 10.0, splits, bombs);
        }

        if (section.contains("Airstrike")) {

            Projectile projectileSettings = null;
            if (section.contains("Airstrike.Dropped_Projectile")) {
                projectileSettings = new Projectile().serialize(file, configurationSection, path + ".Airstrike.Dropped_Projectile");
            }

            int min = section.getInt("Airstrike.Minimum_Bombs");
            int max = section.getInt("Airstrike.Maximum_Bombs");

            double yOffset = section.getDouble("Airstrike.Height");
            double yNoise = section.getDouble("Airstrike.Vertical_Randomness");

            double separation = section.getDouble("Airstrike.Distance_Between_Bombs");
            double range = section.getDouble("Airstrike.Maximum_Distance_From_Center");

            int layers = section.getInt("Airstrike.Layers");
            int interval = section.getInt("Airstrike.Delay_Between_Layers");

            debug.validate(LogLevel.WARN, max < 100, StringUtil.foundLarge(max, file, path + ".Airstrike.Maximum_Bombs"));
            debug.validate(LogLevel.WARN, layers < 100, StringUtil.foundLarge(max, file, path + ".Airstrike.Layers"));

            explosion.new AirStrike(projectileSettings, min, max, yOffset, yNoise, separation, range, layers, interval);
        }

        if (section.contains("Flashbang")) {
            double distance = section.getDouble("Flashbang.Effect_Distance");
            Mechanics mechanics = section.contains("Flashbang.Mechanics") ? new Mechanics().serialize(file, configurationSection, section + ".Flashbang.Mechanics") : null;

            debug.validate(distance > 0.0, "Flashbang Effect_Distance should be a positive number! Found: " + distance,
                    StringUtil.foundAt(file, path + ".Flashbang.Distance"));
            debug.validate(LogLevel.WARN, distance < 100.0, StringUtil.foundLarge(distance, file, path + ".Flashbang.Effect_Distance"));

            // Since the flashbang depends on mechanics for applying blindness to effected
            // entities, not specifying the mechanics is always a mistake
            if (mechanics == null) {
                debug.error("Flashbang MUST use Mechanics in order to make people blind. You forgot to add Mechanics!",
                        StringUtil.foundAt(file, path + ".Flashbang.Mechanics"));
            }

            explosion.new Flashbang(distance, mechanics);
        }

        return explosion;
    }
    
    private enum ExplosionShapeType {
    
        /**
         * Represents a parabolic explosion
         * @see ParabolicExplosion
         */
        PARABOLA,
    
        /**
         * Represents a spherical explosion
         * @see SphericalExplosion
         */
        SPHERE,
    
        /**
         * Represents a cuboid explosion
         * @see CuboidExplosion
         */
        CUBE,
    
        /**
         * Represents a default minecraft generated explosion
         * @see DefaultExplosion
         */
        DEFAULT
    }

    private enum ExplosionExposureType {

        /**
         * Entities within the explosion's area of effect always have
         * 100% exposure, regardless of obstacles and positioning
         */
        NONE,

        /**
         * Damage is only based on the distance between the <code>LivingEntity</code>
         * involved and the origin of the explosion
         * @see DistanceExposure
         */
        DISTANCE,

        /**
         * Damage uses minecraft's explosion exposure method
         * @see DefaultExposure
          */
        DEFAULT
    }
}


