package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;

public class WeaponScopeEvent extends WeaponCancellableEvent {

    private ScopeType scopeType;
    private int zoomAmount;
    private int zoomStack;
    
    public WeaponScopeEvent(String weaponTitle, LivingEntity livingEntity, ScopeType scopeType, int zoomAmount, int zoomStack) {
        super(weaponTitle, livingEntity);
        this.scopeType = scopeType;
        this.zoomAmount = zoomAmount;
        this.zoomStack = zoomStack;
    }

    /**
     * @return the zoom cause
     */
    public ScopeType getScopeType() {
        return scopeType;
    }

    /**
     * If this is 0 it most likely means that entity is zooming out.
     * Make sure to check scoep type
     *
     * @return the NEW zoom amount (1-32)
     */
    public int getZoomAmount() {
        return zoomAmount;
    }

    /**
     * Sets new zoom amount for this event. Make sure its between 1 and 32.
     *
     * @param zoomAmount the new zoom amount
     */
    public void setZoomAmount(int zoomAmount) {
        if (zoomAmount < 1 || zoomAmount > 32) {
            throw new IllegalArgumentException("Zoom amount must be between 1 and 32");
        }
        this.zoomAmount = zoomAmount;
    }

    /**
     * You can get the old zoom stack by reducing this by 1.
     * If this is 0, it either means that stacking is not used or it was first zoom in.
     * Make sure to check scope type.
     *
     * @return the NEW zoom stack amount
     */
    public int getZoomStack() {
        return zoomStack;
    }

    public enum ScopeType {

        /**
         * Means zooming in
         */
        IN,

        /**
         * Means zoom is stacking
         */
        STACK,

        /**
         * Means zooming out
         */
        OUT
    }
}