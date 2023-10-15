package me.deecaad.core.file.serializers;

import me.deecaad.core.file.*;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.deecaad.core.MechanicsCore.debug;

public class ColorSerializer implements InlineSerializer<ColorSerializer> {

    private static final Pattern HEX_PATTERN = Pattern.compile("[0-9a-fA-F]{6}");
    private Color color;

    /**
     * Default constructor for serializer
     */
    public ColorSerializer() {
    }

    public ColorSerializer(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    @NotNull
    public ColorSerializer serialize(@NotNull SerializeData data) throws SerializerException {

        String input = data.config.getString(data.key);
        if (input == null || input.isEmpty())
            throw new SerializerMissingKeyException(this, "Color", StringUtil.foundAt(data.file, data.key));

        Color color = fromString(data, input.trim());
        return new ColorSerializer(color);
    }

    public static Color fromString(SerializeData data, String input) throws SerializerException {

        // Follows the format, '0xRRGGBB' and translates each character as a
        // hex character. While '0xRR' is technically a valid hex code for red,
        // this is actually treated as an error (To help avoid confusion).
        if (input.startsWith("0x")) {
            String substring = input.substring(2);
            if (substring.length() != 6) {
                throw data.exception(null, "Hex strings should have 6 digits",
                        SerializerException.forValue(input),
                        SerializerException.examples("0xFF00BB", "0x123456", "0x111111"));
            }
            int rgb = Integer.parseInt(substring, 16);
            return Color.fromRGB(rgb);
        }

        // Follows the format, '#RRGGBB' and translates each character as a
        // hex character. While '#RR' is technically a valid hex code for red,
        // this is actually treated as an error (To help avoid confusion).
        else if (input.startsWith("#")) {
            String substring = input.substring(1);
            if (substring.length() != 6) {
                throw data.exception(null, "Hex strings should have 6 digits",
                        SerializerException.forValue(input),
                        SerializerException.examples("#FF00BB", "#123456", "#111111"));
            }
            int rgb = Integer.parseInt(substring, 16);
            return Color.fromRGB(rgb);
        }

        // The above 2 options cover MOST hex scenarios, but we use this regex
        // here to be a bit more adaptable. While it isn't so good at catching
        // errors like the above examples, it should help eliminate the need
        // for people to ask questions.
        else if (HEX_PATTERN.matcher(input).find()) {
            Matcher matcher = HEX_PATTERN.matcher(input);
            matcher.find(); // always true
            String substring = matcher.group();
            int rgb = Integer.parseInt(substring, 16);
            return Color.fromRGB(rgb);
        }

        // Follows the format, 'R-G-B' and translates each character as a byte.
        else if (StringUtil.split(input).length == 3) {
            String[] split = input.split("-");
            int r = Integer.parseInt(split[0]);
            int g = Integer.parseInt(split[1]);
            int b = Integer.parseInt(split[2]);

            if (r < 0 || r > 255)
                throw new SerializerRangeException("Color", 0, r, 255, StringUtil.foundAt(data.file, data.key));
            else if (g < 0 || g > 255)
                throw new SerializerRangeException("Color", 0, g, 255, StringUtil.foundAt(data.file, data.key));
            else if (b < 0 || b > 255)
                throw new SerializerRangeException("Color", 0, b, 255, StringUtil.foundAt(data.file, data.key));

            return Color.fromRGB(r, g, b);
        }

        // Allows for primitive color usage, like 'RED', 'BLUE', or 'GREEN'.
        else {
            Optional<ColorType> optional = EnumUtil.getIfPresent(ColorType.class, input.toUpperCase(Locale.ROOT));

            if (optional.isPresent()) {
                return optional.get().color;
            }

            // This occurs when EVERY color format fails, so we should try to
            // provide a lot of information to help the user.
            else {
                throw data.exception(null, SerializerException.forValue(input),
                        SerializerException.didYouMean(input, ColorType.class),
                        "Choose one of these formats: #RRGGBB, r g b, RED, RRGGBB, 0xRRGGBB, r~g~b",
                        SerializerException.examples("#AA0022", "255 100 0", "RED", "444411", "BLUE", "0 0 255")
                        );
            }
        }
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
            String[] splittedColor = StringUtil.split(colorString);
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
