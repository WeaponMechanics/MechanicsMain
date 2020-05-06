package me.deecaad.weaponmechanics.weapon.explode;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * This interface outlines an explosion. Classes that
 * implement <code>Explosion</code> should have a
 * constructor that takes information about the explosion
 * and the implemented methods should be influenced by those
 * instance variables
 */
public interface ExplosionShape {
    
    /**
     * This method should return a set of blocks that
     * are effected by this <code>Explosion</code>
     * triggered at the given <code>Location</code>
     *
     * Conditions (Like material blacklists) are not
     * used to filter out any <code>Blocks</code>, that
     * is handled separately
     *
     * @param origin The center of the explosion
     *               Should not be null
     * @return The effected blocks
     */
    @Nonnull
    List<Block> getBlocks(@Nonnull Location origin);
    
    /**
     * This method should return a list of entities that
     * are within this <code>Explosion</code> triggered
     * at the given <code>Location</code>.
     *
     * Conditions (Like player team, the cause of the
     * explosion, etc) are not used to filter entities,
     * that is handled separately
     *
     * The <code>Double</code> generic represents how much
     * "impact" the player gets. This should be a number (0, 1]
     * Higher numbers mean more damage and knockback
     *
     * @param origin The center of the explosion
     *               Should not be null
     * @return The effected players and their impact level
     */
    @Nonnull
    Map<LivingEntity, Double> getEntities(@Nonnull Location origin);
}
