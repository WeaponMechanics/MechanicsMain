package me.deecaad.core.file.serializers;

import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SimpleSerializer;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorSerializer implements SimpleSerializer<Color> {

    private static final Pattern HEX_PATTERN = Pattern.compile("[0-9a-fA-F]{6}");

    @Override
    public @NotNull String getTypeName() {
        return "#RRGGBB";
    }

    @Override
    public @NotNull Color deserialize(@NotNull String data, @NotNull String errorLocation) throws SerializerException {

        // Follows the format, '0xRRGGBB' and translates each character as a
        // hex character. While '0xRR' is technically a valid hex code for red,
        // this is actually treated as an error (To help avoid confusion).
        if (data.startsWith("0x")) {
            String substring = data.substring(2);
            if (substring.length() != 6) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .example("0xFF00BB")
                    .addMessage("Hex strings should have 6 digits")
                    .addMessage("Found value: " + data)
                    .build();
            }
            int rgb = Integer.parseInt(substring, 16);
            return Color.fromRGB(rgb);
        }

        // Follows the format, '#RRGGBB' and translates each character as a
        // hex character. While '#RR' is technically a valid hex code for red,
        // this is actually treated as an error (To help avoid confusion).
        else if (data.startsWith("#")) {
            String substring = data.substring(1);
            if (substring.length() != 6) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .example("#FF00BB")
                    .addMessage("Hex strings should have 6 digits")
                    .addMessage("Found value: " + data)
                    .build();
            }
            int rgb = Integer.parseInt(substring, 16);
            return Color.fromRGB(rgb);
        }

        // The above 2 options cover MOST hex scenarios, but we use this regex
        // here to be a bit more adaptable. While it isn't so good at catching
        // errors like the above examples, it should help eliminate the need
        // for people to ask questions.
        else if (HEX_PATTERN.matcher(data).find()) {
            Matcher matcher = HEX_PATTERN.matcher(data);
            matcher.find(); // always true
            String substring = matcher.group();
            int rgb = Integer.parseInt(substring, 16);
            return Color.fromRGB(rgb);
        }

        // Follows the format, 'R-G-B' and translates each character as a byte.
        else if (StringUtil.split(data).size() == 3) {
            String[] split = data.split("-");
            int r = Integer.parseInt(split[0]);
            int g = Integer.parseInt(split[1]);
            int b = Integer.parseInt(split[2]);

            if (r < 0 || r > 255) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .buildInvalidRange(r, 0, 255);
            } else if (g < 0 || g > 255) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .buildInvalidRange(g, 0, 255);
            } else if (b < 0 || b > 255) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .buildInvalidRange(b, 0, 255);
            }

            return Color.fromRGB(r, g, b);
        }

        // Allows for primitive color usage, like 'RED', 'BLUE', or 'GREEN'.
        else {
            Optional<ColorType> optional = EnumUtil.getIfPresent(ColorType.class, data.trim().toUpperCase(Locale.ROOT));

            if (optional.isPresent()) {
                return optional.get().getBukkitColor();
            }

            // This occurs when EVERY color format fails, so we should try to
            // provide a lot of information to help the user.
            else {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("Choose one of these formats: #RRGGBB, r g b, RED, RRGGBB, 0xRRGGBB, r~g~b")
                    .buildInvalidEnumOption(data, ColorType.class);
            }
        }
    }

    @Override
    public @NotNull List<String> examples() {
        List<String> examples = new ArrayList<>(100);
        for (ColorType colorType : ColorType.values()) {
            examples.add(colorType.name());
            Color color = colorType.getBukkitColor();

            examples.add(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
            examples.add(String.format("%d-%d-%d", color.getRed(), color.getGreen(), color.getBlue()));
            examples.add(String.format("0x%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
        }

        return examples;
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
    }
}
