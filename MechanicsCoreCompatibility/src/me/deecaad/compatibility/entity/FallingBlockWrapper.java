package me.deecaad.compatibility.entity;

/**
 * Wraps an NMS falling block entity to it's time
 * to hit the ground
 */
public class FallingBlockWrapper {

    private final Object entity;
    private final int timeToHitGround;

    /**
     * Only let compatibility methods instantiate this class
     *
     * @param entity The nms entity
     * @param timeToHitGround The time, in ticks, to hit the ground (or -1)
     */
    FallingBlockWrapper(Object entity, int timeToHitGround) {
        this.entity = entity;
        this.timeToHitGround = timeToHitGround;
    }

    public Object getEntity() {
        return entity;
    }

    public int getTimeToHitGround() {
        return timeToHitGround;
    }
}
