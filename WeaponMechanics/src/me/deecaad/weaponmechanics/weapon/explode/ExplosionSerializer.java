package me.deecaad.weaponmechanics.weapon.explode;

import com.google.common.collect.Sets;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.weapon.explode.types.CuboidExplosion;
import me.deecaad.weaponmechanics.weapon.explode.types.DefaultExplosion;
import me.deecaad.weaponmechanics.weapon.explode.types.ParabolicExplosion;
import me.deecaad.weaponmechanics.weapon.explode.types.SphericalExplosion;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Fluid;
import net.minecraft.server.v1_15_R1.IBlockData;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Set;

/**
 * This class serves to serialize different types
 * of explosions based on the configured
 * <code>ExplosionType</code>. If the type of
 * explosion is invalid, the user is notified.
 */
public class ExplosionSerializer implements Serializer<Explosion> {
    
    @Override
    public String getKeyword() {
        return "Explosion";
    }
    
    @Override
    public Explosion serialize(File file, ConfigurationSection configurationSection, String path) {
        ConfigurationSection section = configurationSection.getConfigurationSection(path);
        
        // Gets the explosion type from config, warns the user
        // if the type is invalid
        String typeName = section.getString("Explosion_Type", "DEFAULT").toUpperCase();
        ExplosionType type;
        try {
            type = ExplosionType.valueOf(typeName);
        } catch (IllegalArgumentException ex) {
            DebugUtil.log(LogLevel.ERROR, "The explosion type \"" + typeName + "\" is invalid.",
                    "Look at the wiki for valid explosion types.",
                    "Found in file " + file + " at path " + path);
            return null;
        }
        
        // Used to give the user information of where the error may have occurred.
        String found = "Found in file \"" + file + "\" at path \"" + path + "\"";
        
        // Get all possibly applicable data for the explosions,
        // and warn users for "odd" values
        double yield  = section.getDouble("Explosion_Type_Data.Yield",  3.0);
        double angle  = section.getDouble("Explosion_Type_Data.Angle",  0.5);
        double depth  = section.getDouble("Explosion_Type_Data.Depth",  -3.0);
        double height = section.getDouble("Explosion_Type_Data.Height", 3.0);
        double width  = section.getDouble("Explosion_Type_Data.Width",  3.0);
        double radius = section.getDouble("Explosion_Type_Data.Radius", 3.0);
        
        DebugUtil.assertTrue(yield > 0, "Explosion Yield should be a positive number!", found);
        DebugUtil.assertTrue(angle > 0, "Explosion Angle should be a positive number!", found);
        DebugUtil.assertTrue(depth < 0, "Explosion depth should be a negative number!", found);
        DebugUtil.assertTrue(height > 0, "Explosion Height should be a positive number!", found);
        DebugUtil.assertTrue(width > 0, "Explosion Width should be a positive number!", found);
        DebugUtil.assertTrue(radius > 0, "Explosion Radius should be a positive number!", found);
        
        // I could just compare Strings here instead of
        // making an enum for the different explosion
        // types, but this is more readable
        switch (type) {
            case CUBE:
                return new CuboidExplosion(width, height);
            case SPHERE:
                return new SphericalExplosion(radius);
            case PARABOLA:
                return new ParabolicExplosion(depth, angle);
            case DEFAULT:
                return new DefaultExplosion(yield);
        }
        
        // This should never occur. If this occurs, it's most likely that
        // a new explosion type was created, but it was never added to the enums
        throw new IllegalArgumentException("Explosion needs updating");
    }
    
    private enum ExplosionType {
    
        /**
         * Represents a parabolic explosion
         * @see me.deecaad.weaponmechanics.weapon.explode.types.ParabolicExplosion
         */
        PARABOLA,
    
        /**
         * Represents a spherical explosion
         * @see me.deecaad.weaponmechanics.weapon.explode.types.SphericalExplosion
         */
        SPHERE,
    
        /**
         * Represents a cuboid explosion
         * @see me.deecaad.weaponmechanics.weapon.explode.types.CuboidExplosion
         */
        CUBE,
    
        /**
         * Represents a default minecraft generated explosion
         */
        DEFAULT
    }


}


