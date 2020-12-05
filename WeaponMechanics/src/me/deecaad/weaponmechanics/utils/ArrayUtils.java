package me.deecaad.weaponmechanics.utils;

import java.util.Collection;

public class ArrayUtils {

    /**
     * Don't let anyone instantiate this class
     */
    private ArrayUtils() {
    }

    /**
     * Changes colors into black and white
     * (0-255) where 255 is black and 0 is white
     *
     * @param colors The array of colors
     */
    public static int[][] toBlackAndWhite(int[][] colors) {
        for (int y = 0; y < colors.length; y++) {
            for (int x = 0; x < colors[y].length; x++) {
                int color = colors[y][x] & 0xFF;

                // Think of black and white.
                // Black is the complete absence of color.
                // White is all colors.
                // Since black is going have a higher chance
                // for a random location for spread to occur,
                // I want/need the number for black to be
                // greater then white.
                colors[y][x] = Math.abs(color - 255);
            }
        }
        return colors;
    }

    /**
     * Splits a 1 dimensional array into
     * a 2 dimensional array with the given
     * width
     *
     * @param array Array to split
     * @param width Size of the 2nd dimension
     * @return Split array
     */
    public static int[][] split(int[] array, int width) {
        int[][] split = new int[array.length / width][width];
        for (int y = 0; y < split.length; y++) {
            System.arraycopy(array, y * width, split[y], 0, split[y].length);
        }
        return split;
    }

    public static String toString(Collection<?> list) {
        StringBuilder builder = new StringBuilder();
        for (Object obj : list) {
            builder.append(obj).append(", ");
        }
        builder.setLength(builder.length() - 2);
        return builder.toString();
    }
}
