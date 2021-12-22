package me.deecaad.weaponmechanics.weapon.newprojectile.weaponprojectile;

import javax.annotation.Nullable;
import java.util.Map;

public class ListHolder<T extends Enum<T>> {

    private boolean allowAny;
    private boolean whitelist;
    private double defaultSpeedModifier;
    private Map<T, Double> list;

    /**
     * If this is null, that means key is NOT valid
     *
     * @param key the key
     * @return the speed modifier of key or null if it's not valid
     */
    @Nullable
    public Double isValid(T key) {
        if (allowAny) {
            // Since all values are valid, simply return speed modifier
            return list == null ? defaultSpeedModifier : list.getOrDefault(key, defaultSpeedModifier);
        }

        if (!whitelist) {
            // If blacklist and list contains key
            // -> Can't use
            // Else return speed modifier
            return list.containsKey(key) ? null : list.getOrDefault(key, defaultSpeedModifier);
        }
        // If whitelist and list DOES NOT contain key
        // -> Can't use
        // Else return speed modifier
        return !list.containsKey(key) ? null : list.getOrDefault(key, defaultSpeedModifier);
    }


}
