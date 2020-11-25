package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.utils.NumberUtils;

import java.util.Iterator;
import java.util.SortedSet;

/**
 * Class to hold all collisions
 */
public class Collisions {

    private final SortedSet<CollisionData> blockCollisions;
    private final SortedSet<CollisionData> entityCollisions;

    public Collisions(SortedSet<CollisionData> blockCollisions, SortedSet<CollisionData> entityCollisions) {
        this.blockCollisions = blockCollisions;
        this.entityCollisions = entityCollisions;
    }

    public SortedSet<CollisionData> getBlockCollisions() {
        return blockCollisions;
    }

    public SortedSet<CollisionData> getEntityCollisions() {
        return entityCollisions;
    }

    /**
     * @param collisionData the new collision data
     * @return whether new collision data is not able to hit again
     */
    public boolean isNotAbleToHit(CollisionData collisionData) {
        if (blockCollisions.isEmpty() && entityCollisions.isEmpty()) return false;
        Iterator<CollisionData> iterator = collisionData.getBlock() != null ? blockCollisions.iterator() : entityCollisions.iterator();
        while (iterator.hasNext()) {
            CollisionData old = iterator.next();
            if (isNotAbleToHit(old, collisionData)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotAbleToHit(CollisionData old, CollisionData collisionData) {
        // Check if this "old" collision data matches new collision data
        // -> Meaning same block or living entity is being hit
        // --> If they're same same -> should not be able to hit

        // After that check that the hit time of this old collision data is less than 1000
        // If its less than 1000 -> should NOT be able to hit
        return old.equals(collisionData) && !NumberUtils.hasMillisPassed(old.getHitTime(), 1000);
    }

}