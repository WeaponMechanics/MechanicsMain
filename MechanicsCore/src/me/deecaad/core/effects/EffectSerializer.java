package me.deecaad.core.effects;

import me.deecaad.core.effects.shapes.Shape;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.deecaad.core.MechanicsCore.debug;

public class EffectSerializer implements Serializer<List<Effect>> {

    public static final Map<String, StringSerializable<Effect>> EFFECT_SERIALIZERS = new HashMap<>();
    public static final Map<String, StringSerializable<Shape>> SHAPE_SERIALIZERS = new HashMap<>();

    private static Pattern mapChecker = Pattern.compile("\\(.+\\)");
    private static Pattern integerChecker = Pattern.compile("-?\\d+");
    private static Pattern doubleChecker = Pattern.compile("-?\\d+\\.\\d+");
    private static Pattern booleanChecker = Pattern.compile("true|false");

    /**
     * Empty constructor for serializer
     */
    public EffectSerializer() {
    }

    @Override
    public String getKeyword() {
        return "Effects";
    }

    @Override
    public List<Effect> serialize(File file, ConfigurationSection configurationSection, String path) {
        List<String> strings = configurationSection.getConfigurationSection(path).getStringList(getKeyword());
        List<Effect> effects = new ArrayList<>();
        String error = "Found in file \"" + file + "\" at path \"" + path + "\"";

        for (String str: strings) {

            // Get which serializer to use
            String serializerName = str.substring(0, str.indexOf("("));
            StringSerializable<Effect> serializer = EFFECT_SERIALIZERS.get(serializerName);

            // Gets everything inside the first layer of parens
            // and gets rid of the parens
            Matcher argumentsMatcher = mapChecker.matcher(str);
            String match = argumentsMatcher.group(); // This is probably safe
            match = match.substring(1, match.length() - 1);

            // Splits all arguments by splitting at either a comma
            // semicolon when NOT inside paren
            // Example: https://regexr.com/52m19
            String[] split = match.split("[,;](?![^(]*\\))");

            // Fill up a map with all of the arguments
            Map<String, Object> args = new HashMap<>();
            Map<String, Object> defaults = serializer.getDefaults();
            for (String argument: split) {

                // Get the key (variable name) and value from the argument
                // The regex adds support for using either the equals sign
                // or the colon.
                String[] keyAndArg = argument.split("[=:]");
                String key = keyAndArg[0];
                if (!defaults.containsKey(key)) {
                    debug.log(LogLevel.ERROR, "Unknown variable \"" + key + "\"", error);
                    continue;
                }
                String value = keyAndArg[1];
                args.put(key, guessDataType(value));
            }

            // Add default values if there is no value present
            args.putAll(defaults);
            effects.add(serializer.serialize(args));
        }

        return effects;
    }

    private static Object guessDataType(String value) {
        if (integerChecker.matcher(value).matches()) {
            return Integer.parseInt(value);
        } else if (doubleChecker.matcher(value).matches()) {
            return Double.parseDouble(value);
        } else if (booleanChecker.matcher(value).matches()) {
            return Boolean.parseBoolean(value);
        } else if (mapChecker.matcher(value).find()) {

            // Get which serializer to use
            String serializerName = value.substring(0, value.indexOf("("));
            StringSerializable<Shape> serializer = SHAPE_SERIALIZERS.get(serializerName);

            // Separate the arguments
            String match = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")") - 1);
            String[] split = match.split("[,;]"); // We are only allowed single nests

            // Fill up a map with all of the arguments
            Map<String, Object> args = new HashMap<>();
            Map<String, Object> defaults = serializer.getDefaults();
            for (String argument: split) {

                // Get the key (variable name) and value from the argument
                // The regex adds support for using either the equals sign
                // or the colon.
                String[] keyAndArg = argument.split("[=:]");
                String key = keyAndArg[0];
                if (!defaults.containsKey(key)) {
                    debug.log(LogLevel.ERROR, "Unknown variable \"" + key + "\"");
                    continue;
                }
                String arg = keyAndArg[1];
                args.put(key, guessDataType(arg));
            }

            // Add default values if there is no value present
            args.putAll(defaults);
            return serializer.serialize(args);

        } else {

            // Probably just meant to be a String, so keep
            // it as a String
            return value;
        }
    }
}
