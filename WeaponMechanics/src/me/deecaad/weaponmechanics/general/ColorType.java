package me.deecaad.weaponmechanics.general;

import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.utils.StringUtils;
import org.bukkit.Color;

public enum ColorType {

    // All somewhat default Minecraft colors are directly supported
    BLACK(Color.fromRGB(0, 0, 0)),
    DARK_BLUE(Color.fromRGB(0, 0, 170)),
    DARK_GREEN(Color.fromRGB(0, 170, 0)),
    DARK_AQUA(Color.fromRGB(0, 170, 170)),
    DARK_RED(Color.fromRGB(170, 0, 0)),
    DARK_PURPLE(Color.fromRGB(170, 0, 170)),
    GOLD(Color.fromRGB(255, 170, 0)),
    GRAY(Color.fromRGB(170, 170, 170)),
    DARK_GRAY(Color.fromRGB(85, 85, 85)),
    BLUE(Color.fromRGB(85, 85, 255)),
    GREEN(Color.fromRGB(85, 255, 85)),
    AQUA(Color.fromRGB(85, 255, 255)),
    RED(Color.fromRGB(255, 85, 85)),
    LIGHT_PURPLE(Color.fromRGB(255, 85, 255)),
    YELLOW(Color.fromRGB(255, 255, 85)),
    WHITE(Color.fromRGB(255, 255, 255));

    private final Color color;

    ColorType(Color color) {
        this.color = color;
    }

    /**
     * @return the color type as bukkit color
     */
    public Color getBukkitColor() {
        return color;
    }

    /**
     * @param colorString the color as ColorType string or red-green-blue string
     * @return the color parsed from string or null if not valid or found
     */
    public static Color fromString(String colorString) {
        String[] splittedColor = StringUtils.split(colorString);
        if (splittedColor.length == 1) {
            try {
                return valueOf(colorString).getBukkitColor();
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        return fromRGBString(splittedColor);
    }

    /**
     * @param splittedColor the string color in red-green-blue format
     * @return the RGB bukkit color or null if not valid
     */
    public static Color fromRGBString(String[] splittedColor) {
        if (splittedColor.length < 3) {
            DebugUtil.log(LogLevel.ERROR, "Tried to get RGB color out of " + splittedColor + ", but it wasn't in correct format.",
                    "Correct format is red-green-blue");
            return null;
        }
        try {
            int red = Integer.parseInt(splittedColor[0]);
            int green = Integer.parseInt(splittedColor[1]);
            int blue = Integer.parseInt(splittedColor[2]);
            return Color.fromRGB(red, green, blue);
        } catch (NumberFormatException e) {
            DebugUtil.log(LogLevel.ERROR, "Tried to get RGB color out of " + splittedColor + ", but it didn't contain integers.");
            return null;
        }
    }

    /**
     * In versions before 1.13 this had to be done for particle offsets
     *
     * @return the value divided by 255
     */
    public static float getAsParticleColor(float value) {
        if (value < (float) 1.0) {
            value = (float) 1.0;
        }
        return value /= (float) 255.0;
    }
}