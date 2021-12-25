package me.deecaad.weaponmechanics.weapon.projectile;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

/**
 * Really lightweight remove on block collision projectile.
 * This considers all blocks as 1x1x1 and is removed on any
 * collision including passable blocks and liquid.
 *
 * This doesn't check entity collisions.
 */
public class RemoveOnBlockCollisionProjectile extends AProjectile {

    protected RemoveOnBlockCollisionProjectile(ProjectileSettings projectileSettings, Location location, Vector motion) {
        super(projectileSettings, location, motion);
    }

    @Override
    public boolean handleCollisions() {
        // Rounding might cause 0.5 "extra" movement, but it doesn't really matter
        BlockIterator blocks = new BlockIterator(getWorld(), getLocation(), getNormalizedMotion(), 0.0, (int) Math.round(getMotionLength()));

        while (blocks.hasNext()) {
            Block block = blocks.next();

            if (block.isEmpty()) continue;

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
