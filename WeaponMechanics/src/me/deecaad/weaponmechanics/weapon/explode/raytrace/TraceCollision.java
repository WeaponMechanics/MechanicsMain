package me.deecaad.weaponmechanics.weapon.explode.raytrace;

public enum TraceCollision {

    /**
     * Stops the ray trace when it collides with 1 block
     */
    BLOCK,

    /**
     * Stops the ray trace when it collides with 1 entity
     */
    ENTITY,

    /**
     * Stops the ray trace on first collision with anything
     */
    BLOCK_OR_ENTITY,

    /**
     * The ray trace will calculate all block collisions
     */
    BLOCKS,

    /**
     * The ray trace will calculate all entity collisions
     */
    ENTITIES,

    /**
     * The ray trace will calculate all block and entity collisions
     */
    ALL
}
