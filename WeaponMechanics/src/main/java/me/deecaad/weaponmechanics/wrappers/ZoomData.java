package me.deecaad.weaponmechanics.wrappers;

public class ZoomData {

    private int zoomAmount;
    private int zoomStacks;
    private boolean zoomNightVision;

    /**
     * @return true if is zooming
     */
    public boolean isZooming() {
        return zoomAmount != 0;
    }

    /**
     * @return the current zoom amount
     */
    public int getZoomAmount() {
        return zoomAmount;
    }

    /**
     * This wont update the actual FOV changes
     *
     * @param zoomAmount the new zoom amount
     */
    public void setZoomAmount(int zoomAmount) {
        this.zoomAmount = zoomAmount;
    }

    /**
     * @return the current zoom stack amount
     */
    public int getZoomStacks() {
        return zoomStacks;
    }

    /**
     * Sets new zoom stack amount
     *
     * @param zoomStacks the new zoom stack amount
     */
    public void setZoomStacks(int zoomStacks) {
        if (zoomStacks < 0) {
            zoomStacks = 0;
        }
        this.zoomStacks = zoomStacks;
    }

    /**
     * @return true if zoom night vision is on
     */
    public boolean hasZoomNightVision() {
        return zoomNightVision;
    }

    /**
     * @param zoomNightVision whether or not zoom night vision is on
     */
    public void setZoomNightVision(boolean zoomNightVision) {
        this.zoomNightVision = zoomNightVision;
    }
}