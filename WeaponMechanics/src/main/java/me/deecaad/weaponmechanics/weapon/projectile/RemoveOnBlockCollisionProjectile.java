package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.utils.ray.RayTrace;
import me.deecaad.core.utils.ray.RayTraceResult;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Really lightweight remove on block collision projectile.
 * This considers all blocks as 1x1x1 and is removed on any
 * collision including passable blocks.
 *
 * This doesn't check entity collisions.
 */
public class RemoveOnBlockCollisionProjectile extends AProjectile {

    private static final RayTrace rayTrace = new RayTrace().disableEntityChecks();

    public RemoveOnBlockCollisionProjectile(Location location, Vector motion) {
        this(location, motion, null);
    }

    public RemoveOnBlockCollisionProjectile(Location location, Vector motion, FakeEntity disguise) {
        super(location, motion);
        if (disguise != null) spawnDisguise(disguise);
    }

    @Override
    public boolean updatePosition() {
        Vector[] positionAndVelocity = nextPositionAndVelocity();
        Vector possibleNextLocation = positionAndVelocity[0];
        List<RayTraceResult> hits = rayTrace.cast(getWorld(), getLocation(), possibleNextLocation);
        if (hits != null) {
            RayTraceResult firstHit = hits.get(0);
            setRawLocation(firstHit.getHitLocation());
            onCollide(firstHit);
            return true;
        }
        setRawLocation(possibleNextLocation);
        setMotion(positionAndVelocity[1]);
        addDistanceTravelled(getMotionLength());
        return false;
    }
}
