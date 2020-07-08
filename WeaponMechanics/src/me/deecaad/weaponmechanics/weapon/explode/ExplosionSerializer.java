package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.effects.Effect;
import me.deecaad.core.effects.EffectList;
import me.deecaad.core.effects.serializers.EffectListSerializer;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.RegenerationData;
import me.deecaad.weaponmechanics.weapon.explode.shapes.CuboidExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.DefaultExplosion;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DefaultExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DistanceExposure;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ParabolicExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.SphericalExplosion;
import me.deecaad.weaponmechanics.weapon.explode.exposures.VoidExposure;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        String shapeTypeName = section.getString("Explosion_Shape", "DEFAULT").toUpperCase();
        ExplosionShapeType shapeType;
        try {
            shapeType = ExplosionShapeType.valueOf(shapeTypeName);
        } catch (IllegalArgumentException ex) {
            debug.log(LogLevel.ERROR, "The explosion shape \"" + shapeTypeName + "\" is invalid.",
                    "Valid Shapes: " + Arrays.toString(ExplosionShapeType.values()),
                    StringUtils.foundAt(file, path));
            return null;
        }

        String exposureTypeName = section.getString("Explosion_Exposure", "DEFAULT").toUpperCase();
        ExplosionExposureType exposureType;
        try {
            exposureType = ExplosionExposureType.valueOf(exposureTypeName);
        } catch (IllegalArgumentException ex) {
            debug.log(LogLevel.ERROR, "The explosion exposure \"" + exposureTypeName + "\" is invalid.",
                    "Valid exposures: " + Arrays.toString(ExplosionExposureType.values()),
                    StringUtils.foundAt(file, path));
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

        // todo change depth to be positive... makes more sense that way
        if (depth > 0) depth *= -1;

        String found = StringUtils.foundAt(file, path);

        debug.validate(yield > 0, "Explosion Yield should be a positive number!", found);
        debug.validate(angle > 0, "Explosion Angle should be a positive number!", found);
        debug.validate(depth < 0, "Explosion depth should be a negative number!", found);
        debug.validate(height > 0, "Explosion Height should be a positive number!", found);
        debug.validate(width > 0, "Explosion Width should be a positive number!", found);
        debug.validate(radius > 0, "Explosion Radius should be a positive number!", found);

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
                shape = new DefaultExplosion(yield);
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
        boolean isBreakBlocks = section.getBoolean("Blocks.Enabled", true);
        RegenerationData regeneration = null;
        if (section.contains("Blocks.Regeneration")) {
            regeneration = new RegenerationData().serialize(file, configurationSection, path + ".Blocks.Regeneration");
        }
        boolean isBlacklist = section.getBoolean("Blocks.Blacklist", false);
        Set<String> materials = section.getList("Blocks.Block_List", new ArrayList<>(0))
                .stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

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

        String weaponTitle;
        try {
            weaponTitle = path.split("\\.")[0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            debug.warn("Tried to serialize explosion without weaponTitle! This is probably a mistake!");
            weaponTitle = null;
        }

        boolean isKnockback = !section.getBoolean("Disable_Vanilla_Knockback");

        List<Effect> effects = null;
        if (section.contains("Effects")) {
            effects = new EffectListSerializer().serialize(file, configurationSection, path + ".Effects").getEffects();
        }
        return new Explosion(weaponTitle, shape, exposure, isBreakBlocks, regeneration, isBlacklist, materials, triggers, delay, isKnockback, effects);
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


