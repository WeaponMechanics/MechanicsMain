package me.deecaad.weaponmechanics.weapon.explode.raytrace;

public enum TraceCollision {

    /**
     * Stops the ray trace when it collides with 1 block
     */
    BLOCK(true, false, true),

    /**
     * Stops the ray trace when it collides with 1 entity
     */
    ENTITY(false, true, true),

    /**
     * Stops the ray trace on first collision with anything
     */
    BLOCK_OR_ENTITY(true, true, true),

    /**
     * The ray trace will calculate all block collisions
     */
    BLOCKS(true, false, false),

    /**
     * The ray trace will calculate all entity collisions
     */
    ENTITIES(false, true, false),

    /**
     * The ray trace will calculate all block and entity collisions
     */
    BLOCKS_OR_ENTITIES(true, true, false);

    private final boolean isBlock;
    private final boolean isEntity;
    private final boolean isFirst;

    TraceCollision(boolean isBlock, boolean isEntity, boolean isFirst) {
        this.isBlock = isBlock;
        this.isEntity = isEntity;
        this.isFirst = isFirst;
    }

    public boolean isBlock() {
        return isBlock;
    }

    public boolean isEntity() {
        return isEntity;
    }

    public boolean isFirst() {
        return isFirst;
    }
}
