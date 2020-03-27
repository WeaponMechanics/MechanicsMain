package me.deecaad.weaponmechanics.weapon.explode;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * This interface outlines an explosion. Classes that
 * implement <code>Explosion</code> should have a
 * constructor that takes information about the explosion
 * and the <code>Explosion#getBlocks(Location)</code>
 * method should be influenced by that information.
 */
public interface Explosion {
    
    /**
     * This method should return a set of blocks that
     * are effected by this <code>Explosion</code>
     * triggered at the given <code>Location</code>
     *
     * Conditions (Like material blacklists) should
     * <b>NOT</b> be used to determine the blocks
     * within this method.
     *
     * @param origin The center of the explosion
     *               Should not be null
     * @return The effected blocks
     */
    @Nonnull
    Set<Block> getBlocks(@Nonnull Location origin);
    
    /**
     * This method should return a list of entities that
     * are within this <code>Explosion</code> triggered
     * at the given <code>Location</code>.
     *
     * Conditions (Like player team, the cause of the
     * explosion, etc) should <b>NOT</b> be used to
     * determine the entities within this method.
     *
     * @param origin The center of the explosion
     *               Should not be null
     * @return The effected players
     */
    @Nonnull
    Set<LivingEntity> getEntities(@Nonnull Location origin);
}
