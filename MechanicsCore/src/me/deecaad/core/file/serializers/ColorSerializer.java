package me.deecaad.core.file.serializers;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.Enums;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import static me.deecaad.core.MechanicsCore.debug;

public class ColorSerializer implements Serializer<Color> {
    @Override
    public String getKeyword() {
        return "Color";
    }

    @Override
    public Color serialize(File file, ConfigurationSection configurationSection, String path) {

        String data = configurationSection.getString(path);

        Color color;

        try {
            if (data.startsWith("0x")) {
                String subString = data.substring(2);
                if (subString.length() != 6) {
                    debug.warn("Hex strings are usually 6 digits for colors... Found: " + subString,
                            StringUtils.foundAt(file, path));
                }
                int rgb = Integer.parseInt(subString, 16);
                color = Color.fromRGB(rgb);

            } else if (data.startsWith("#")) {
                String subString = data.substring(1);
                if (subString.length() != 6) {
                    debug.warn("Hex strings are usually 6 digits for colors... Found: " + subString,
                            StringUtils.foundAt(file, path));
                }
                int rgb = Integer.parseInt(subString, 16);
                color = Color.fromRGB(rgb);

            } else if (StringUtils.countChars('-', data) == 2) {
                String[] split = data.split("-");
                int r = Integer.parseInt(split[0]);
                int g = Integer.parseInt(split[1]);
                int b = Integer.parseInt(split[2]);

                if (r < 0 || r > 255) {
                    debug.error("Invalid number for red color. Number should be in range 0-255. Found: " + r,
                            StringUtils.foundAt(file, path));
                    return null;
                } else if (g < 0 || g > 255) {
                    debug.error("Invalid number for green color. Number should be in range 0-255. Found: " + g,
                            StringUtils.foundAt(file, path));
                    return null;
                } else if (b < 0 || b > 255) {
                    debug.error("Invalid number for blue color. Number should be in range 0-255. Found: " + b,
                            StringUtils.foundAt(file, path));
                    return null;
                }

                color = Color.fromRGB(r, g, b);

            } else {
                Optional<ColorType> optional = Enums.getIfPresent(ColorType.class, data.trim().toUpperCase());

                if (optional.isPresent()) {
                    color = optional.get().color;
                } else {
                    debug.error("Can't figure out which color to use for input: " + data,
                            "Did you mean " + StringUtils.didYouMean(data, Enums.getOptions(ColorType.class)) + "?",
                            StringUtils.foundAt(file, path));
                    return null;
                }
            }
        } catch (NumberFormatException ex) {
            debug.error("Found an invalid number or hex string in configurations: " + ex.getMessage(),
                    StringUtils.foundAt(file, path));
            return null;
        }

        return color;
    }

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
                    return ColorType.valueOf(colorString).getBukkitColor();
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
                debug.log(LogLevel.ERROR, "Tried to get RGB color out of " + Arrays.toString(splittedColor) + ", but it wasn't in correct format.",
                        "Correct format is red-green-blue");
                return null;
            }
            try {
                int red = Integer.parseInt(splittedColor[0]);
                int green = Integer.parseInt(splittedColor[1]);
                int blue = Integer.parseInt(splittedColor[2]);
                return Color.fromRGB(red, green, blue);
            } catch (NumberFormatException e) {
                debug.log(LogLevel.ERROR, "Tried to get RGB color out of " + Arrays.toString(splittedColor) + ", but it didn't contain integers.");
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
            return value / (float) 255.0;
        }
    }
}
