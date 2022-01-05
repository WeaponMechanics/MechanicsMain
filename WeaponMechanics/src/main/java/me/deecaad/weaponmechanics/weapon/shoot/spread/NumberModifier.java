package me.deecaad.weaponmechanics.weapon.shoot.spread;

/**
 * Simple class to help with handling spreads
 */
public class NumberModifier {

    private final double spread;
    private final boolean percentage;

    public NumberModifier(double spread, boolean percentage) {
        this.percentage = percentage;

        // PERCENTAGE:
        // Convert from 100% to 1.0 format
        // Add 1 to spread to make it so that
        // -> 50% actually decreases 50% and 130% actually increases 30%
        // -> 100% would then mean that no changes are made

        // NORMAL SPREAD
        // *0.01 is more configuration friendly way

        this.spread = spread * 0.01;
    }

    public double applyTo(double currentSpread) {
        return percentage ? currentSpread * this.spread : currentSpread + this.spread;
    }
}