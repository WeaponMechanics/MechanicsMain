package me.deecaad.weaponmechanics.weapon.explode.shapes;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;

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

    List<LivingEntity> getEntities(@Nonnull Location origin);

    @Nonnegative
    double getMaxDistance();
}
