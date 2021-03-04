package me.deecaad.weaponmechanics.utils;

import org.bukkit.Sound;

/**
 * Made this into external class in case we decide to use XSound resource so its easier to start using it when we only have to modify this class
 */
public class SoundUtil {
    
    /**
     * Don't let anyone instantiate this class
     */
    private SoundUtil() { }
    
    /**
     * Simple method to convert string to sound.
     *
     * @param soundString the string containing sound
     * @return the sound generated from string
     */
    public static Sound fromStringToSound(String soundString) {
        return Sound.valueOf(soundString.toUpperCase());
    }
}