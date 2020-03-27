package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.weaponmechanics.utils.NumberUtils;
import me.deecaad.weaponmechanics.weapon.shoot.spread.ModifierHolder;

public class SpreadChange {

    private long lastChange;
    private double currentSpread;

    /**
     * Checks whether or not spread change should be reset
     *
     * @param resetMillis the time in millis required to reset
     * @return true only if spread change should reset
     */
    public boolean shouldReset(long resetMillis) {
        return NumberUtils.hasMillisPassed(lastChange, resetMillis);
    }

    /**
     * @return the current spread change
     */
    public double getCurrentSpreadChange() {
        return currentSpread;
    }

    /**
     * @param spreadChange the new spread change
     */
    public void setSpreadChange(double spreadChange) {
        this.currentSpread = spreadChange;
    }

    /**
     * Sets last change time to current time in millis
     */
    public void updateResetTime() {
        lastChange = System.currentTimeMillis();
    }
}
