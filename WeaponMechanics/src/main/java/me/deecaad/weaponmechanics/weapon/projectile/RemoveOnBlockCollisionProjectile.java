package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.utils.NumberUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

/**
 * Really lightweight remove on block collision projectile.
 * This considers all blocks as 1x1x1 and is removed on any
 * collision including passable blocks.
 *
 * This doesn't check entity collisions.
 */
public class RemoveOnBlockCollisionProjectile extends AProjectile {

    protected RemoveOnBlockCollisionProjectile(ProjectileSettings projectileSettings, Location location, Vector motion) {
        super(projectileSettings, location, motion);
    }

    @Override
    public boolean handleCollisions(boolean disableEntityCollisions) {

        double distance = Math.ceil(getMotionLength());

        // If distance is 0 or below, it will cause issues
        if (NumberUtil.equals(distance, 0.0)) distance = 1;

        BlockIterator blocks = new BlockIterator(getWorld(), getLocation(), getNormalizedMotion(), 0.0, (int) distance);

        while (blocks.hasNext()) {
            Block block = blocks.next();

            if (block.isEmpty() || block.isLiquid()) continue;

            // Only update location and not distance travelled
            setRawLocation(block.getLocation().toVector());
            onCollide(block);

            // Block was not air, remove
            return true;
        }

        // There wasn't any collisions
        return false;
    }
}
