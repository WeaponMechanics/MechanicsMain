package me.deecaad.weaponmechanics.weapon.scope;

import me.deecaad.core.utils.LogLevel;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class ScopeLevel {
    /**
     * Don't let anyone instantiate this class
     */
    private ScopeLevel() { }

    /**
     * From this one you can fetch the scope level values.
     * It can be either be for attributes or abilities
     *
     * @param level the scope level
     * @return the amount of zoom (when using abilities or attributes depending on level)
     */
    public static float getScope(double level) {
        if (level < 1 || level > 10) {
            debug.log(LogLevel.ERROR,
                    "Tried to get scope level of " + level + ", but only levels between 1 and 10 are allowed.",
                    new IllegalArgumentException("Tried to get scope level of " + level + ", but only levels between 1 and 10 are allowed."));
            return 0;
        }
        return (float) (1.0 / (20 / level - 10)); // checking for division by zero is not needed here, Java gives Infinity when dividing by zero. ABILITIES packet correctly understands the meaning of Infinity
    }


}