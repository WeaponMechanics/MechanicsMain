package me.deecaad.weaponmechanics.weapon.projectile;

import java.util.SortedSet;

/**
 * Class to hold all collisions
 */
public class Collisions {

    private SortedSet<CollisionData> blockCollisions;
    private SortedSet<CollisionData> entityCollisions;

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
}