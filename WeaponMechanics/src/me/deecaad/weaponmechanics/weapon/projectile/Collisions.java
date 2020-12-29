package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.utils.NumberUtils;
import org.bukkit.Bukkit;

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

    public boolean contains(CollisionData collisionData) {
        if (blockCollisions.isEmpty() && entityCollisions.isEmpty()) return false;

        // Extra check to check whether the hit happened more than 1 second ago
        // If it was more, consider this as non hit data
        Iterator<CollisionData> iterator = collisionData.getBlock() != null ? blockCollisions.iterator() : entityCollisions.iterator();
        while (iterator.hasNext()) {
            CollisionData old = iterator.next();
            if (old.equals(collisionData)) {
                return !NumberUtils.hasMillisPassed(old.getHitTime(), 1000);
            }
        }
        return false;
    }
}