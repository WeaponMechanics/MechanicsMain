package me.deecaad.weaponmechanics.weapon.shoot.spread;

/**
 * Simple class to help with handling spreads
 */
public class ModifierHolder {

    private double spread;
    private boolean percentage;

    public ModifierHolder(double spread, boolean percentage) {
        this.percentage = percentage;
        if (percentage) {
            // Convert from 100% to 1.0 format
            // Add 1 to spread to make it so that
            // -> 50% actually decreases 50% and 130% actually increases 30%
            // -> 100% would then mean that no changes are made
            this.spread = spread * 0.01 + 1.0;
        } else {
            spread *= 0.1;
        }
    }

    public double applyTo(double currentSpread) {
        return percentage ? currentSpread * this.spread : currentSpread + this.spread;
    }
}