package me.deecaad.weaponmechanics.weapon.scope;

import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;

import java.util.ArrayList;
import java.util.List;;

public class ScopeLevel {

    private static float[] scopeLevels = getScopeLevels();

    /**
     * Don't let anyone instantiate this class
     */
    private ScopeLevel() { }

    private static float[] getScopeLevels() {
        scopeLevels = new float[32];

        // First 12 levels:
        // -> PacketPlayOutUpdateAttributes (generic.movementSpeed)
        scopeLevels[0] = (float) -0.01;
        scopeLevels[1] = (float) -0.015;
        scopeLevels[2] = (float) -0.02;
        scopeLevels[3] = (float) -0.03;
        scopeLevels[4] = (float) -0.035;
        scopeLevels[5] = (float) -0.04;
        scopeLevels[6] = (float) -0.05;
        scopeLevels[7] = (float) -0.06;
        scopeLevels[8] = (float) -0.07;
        scopeLevels[9] = (float) -0.08;
        scopeLevels[10] = (float) -0.09;
        scopeLevels[11] = (float) -0.1;

        // Rest 20 levels:
        // -> PacketPlayOutAbilities (walk speed)
        scopeLevels[12] = (float) -0.90;
        scopeLevels[13] = (float) -0.60;
        scopeLevels[14] = (float) -0.40;
        scopeLevels[15] = (float) -0.33;
        scopeLevels[16] = (float) -0.29;
        scopeLevels[17] = (float) -0.26;
        scopeLevels[18] = (float) -0.23;
        scopeLevels[19] = (float) -0.20;
        scopeLevels[20] = (float) -0.19;
        scopeLevels[21] = (float) -0.18;
        scopeLevels[22] = (float) -0.17;
        scopeLevels[23] = (float) -0.16;
        scopeLevels[24] = (float) -0.155;
        scopeLevels[25] = (float) -0.15;
        scopeLevels[26] = (float) -0.145;
        scopeLevels[27] = (float) -0.14;
        scopeLevels[28] = (float) -0.135;
        scopeLevels[29] = (float) -0.13;
        scopeLevels[30] = (float) -0.125;
        scopeLevels[31] = (float) -0.12;

        return scopeLevels;
    }

    /**
     * From this one you can fetch the scope level values.
     * It can be either be for attributes or abilities
     *
     * @param level the scope level
     * @return the amount of zoom (when using abilities or attributes depending on level)
     */
    public static float getScope(int level) {
        if (level < 1 || level > 32) {
            DebugUtil.log(LogLevel.ERROR,
                    "Tried to get scope level of " + level + ", but only levels between 1 and 32 are allowed.");
            return 0;
        }
        // -1 because array list index starts at 0 ;)
        return scopeLevels[level - 1];
    }


}