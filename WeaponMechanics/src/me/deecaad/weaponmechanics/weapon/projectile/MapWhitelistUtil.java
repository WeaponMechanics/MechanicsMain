package me.deecaad.weaponmechanics.weapon.projectile;

import java.util.Map;

public class MapWhitelistUtil {

    public static <T, G> T getData(Map<G, T> map, G key, T defaultValue, boolean whitelist) {
        if (!whitelist) {
            // If blacklist and list contains key
            // -> Mark projectile for removal
            // Else return default speed and damage modifiers
            return map.containsKey(key) ? null : defaultValue;
        }
        // If whitelist and list DOES NOT contain key
        // -> Mark projectile for removal
        // Else return key's own speed and damage modifier OR default speed and damage modifiers
        if (!map.containsKey(key)) return null;
        T keyData = map.get(key);
        return keyData != null ? keyData : defaultValue;
    }
}