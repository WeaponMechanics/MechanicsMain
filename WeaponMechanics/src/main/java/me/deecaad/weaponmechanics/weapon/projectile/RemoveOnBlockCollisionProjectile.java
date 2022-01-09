package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.Ray;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.TraceCollision;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.TraceResult;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * Really lightweight remove on block collision projectile.
 * This considers all blocks as 1x1x1 and is removed on any
 * collision including passable blocks.
 *
 * This doesn't check entity collisions.
 */
public class RemoveOnBlockCollisionProjectile extends AProjectile {

    public RemoveOnBlockCollisionProjectile(Location location, Vector motion) {
        this(location, motion, null);
    }

    public RemoveOnBlockCollisionProjectile(Location location, Vector motion, FakeEntity disguise) {
        super(location, motion);
        if (disguise != null) spawnDisguise(disguise);
    }

    @Override
    public boolean handleCollisions(boolean disableEntityCollisions) {
        Vector possibleNextLocation = getLocation().add(getMotion());
        TraceResult result = new Ray(getWorld(), getLocation(), possibleNextLocation).trace(TraceCollision.BLOCK, 0.3);
        if (!result.isEmpty()) {
            Block hit = result.getOneBlock();
            setRawLocation(hit.getLocation().toVector());
            onCollide(result.getOneBlock());
            return true;
        }
        setRawLocation(possibleNextLocation);
        return false;
    }
}
