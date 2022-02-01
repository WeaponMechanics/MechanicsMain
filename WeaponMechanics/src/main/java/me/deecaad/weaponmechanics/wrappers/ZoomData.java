package me.deecaad.weaponmechanics.wrappers;

public class ZoomData {

    private int zoomAmount;
    private int zoomStacks;
    private boolean zoomNightVision;

    /**
     * @return <code>true</code> if the entity is scoped.
     */
    public boolean isZooming() {
        return zoomAmount != 0;
    }

    /**
     * @return The magnitude of the scope amount, or 0 for not scoping.
     */
    public int getZoomAmount() {
        return zoomAmount;
    }

    /**
     * Note: This method does not set the player's FOV. This method should not
     * be used unless you know what you are doing.
     *
     * @param zoomAmount How far the player is zoomed in.
     */
    public void setZoomAmount(int zoomAmount) {
        this.zoomAmount = zoomAmount;
    }

    /**
     * @return How many times stacky-scope was used, or 0.
     */
    public int getZoomStacks() {
        return zoomStacks;
    }

    /**
     * Note: This method does not set the player's FOV. This method should not
     * be used unless you know what you are doing.
     *
     * @param zoomStacks the new zoom stack amount.
     */
    public void setZoomStacks(int zoomStacks) {
        this.zoomStacks = Math.max(0, zoomStacks);
    }

    /**
     * @return <code>true</code> if entity is scoped into a night vision scope.
     */
    public boolean hasZoomNightVision() {
        return zoomNightVision;
    }

    /**
     * Note: This method does not set the player's potion effects. This method
     * should not be used unless you know what you are doing.
     *
     * @param zoomNightVision whether zoom night vision is on
     */
    public void setZoomNightVision(boolean zoomNightVision) {
        this.zoomNightVision = zoomNightVision;
    }
}