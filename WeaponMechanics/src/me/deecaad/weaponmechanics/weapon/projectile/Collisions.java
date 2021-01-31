package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.utils.NumberUtils;

import java.util.Iterator;
import java.util.Set;

/**
 * Class to hold all collisions
 */
public class Collisions {

    private final Set<CollisionData> blockCollisions;
    private final Set<CollisionData> entityCollisions;

    public Collisions(Set<CollisionData> blockCollisions, Set<CollisionData> entityCollisions) {
        this.blockCollisions = blockCollisions;
        this.entityCollisions = entityCollisions;
    }

    public Set<CollisionData> getBlockCollisions() {
        return blockCollisions;
    }

    public Set<CollisionData> getEntityCollisions() {
        return entityCollisions;
    }

    public boolean contains(CollisionData collisionData) {
        if (blockCollisions.isEmpty() && entityCollisions.isEmpty()) return false;

        // Extra check to check whether the hit happened more than 0.5 second ago
        // If it was more, consider this as non hit data
        Iterator<CollisionData> iterator = collisionData.getBlock() != null ? blockCollisions.iterator() : entityCollisions.iterator();
        while (iterator.hasNext()) {
            CollisionData old = iterator.next();
            if (old.equals(collisionData)) {
                return !NumberUtils.hasMillisPassed(old.getHitTime(), 500);
            }
        }
        return false;
    }
}