package me.deecaad.core.commands;

import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import me.deecaad.core.file.SimpleSerializer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A {@link CustomArgument} for parsing a single argument that contains multiple key-value pairs (a
 * "map"). Each key must be known ahead of time, and each key is handled by a
 * {@link SimpleSerializer} for type-checking and deserialization.
 *
 * <pre>{@code
 * Example usage (registering the argument):
 *
 *   // 1) Build known types for each "key"
 *   Map<String, SimpleSerializer<?>>> weaponDataMap = new HashMap<>();
 *   weaponDataMap.put("ammo", new IntSerializer(1, 64));
 *   weaponDataMap.put("firemode", new IntSerializer(0, 2));
 *   weaponDataMap.put("skipMainhand", new BooleanSerializer());
 *   weaponDataMap.put("skin", new StringSerializer());
 *
 *   // 2) Create the custom argument
 *   Argument<Map<String, Object>> dataArg = new CustomMapArgument("data", weaponDataMap)
 *       .setDefault(Collections.emptyMap()); // make it optional
 *
 *   // 3) Use it in a CommandAPICommand
 *   new CommandAPICommand("give")
 *       .withArguments(dataArg)
 *       .executesPlayer((player, args) -> {
 *           Map<String, Object> data = (Map<String, Object>) args.get("data");
 *           // e.g. data.get("ammo") -> Integer
 *       })
 *       .register();
 * }</pre>
 *
 * <p>
 * This class automatically handles:
 * <ul>
 * <li>Splitting the user input on commas (and optional braces '{' / '}')</li>
 * <li>Validating each key, using your {@link SimpleSerializer}</li>
 * <li>Providing tab-completions for keys and values, based on each serializer's {@link
 * SimpleSerializer#examples()}</li>
 * </ul>
 * }
 *
 * @see SimpleSerializer
 */
public class CustomMapArgument extends CustomArgument<Map<String, Object>, String> {

    private final Map<String, SimpleSerializer<?>> knownTypes;

    /**
     * <p>
     * Creates a new {@link CustomMapArgument} that parses a map of key-value pairs from a single
     * argument. This uses a {@link GreedyStringArgument} as the base, so the user can type something
     * like:
     * </p>
     *
     * <pre>{@code
     *   /mycommand {ammo:5,firemode:2,skipMainhand:false}
     * }</pre>
     *
     * <p>
     * Each key must be present in {@code knownTypes}, and its value is parsed by the corresponding
     * {@link SimpleSerializer}.
     * </p>
     *
     * @param nodeName The name (identifier) for this argument
     * @param knownTypes A map from {@code key -> SimpleSerializer<?>}.
     */
    public CustomMapArgument(String nodeName, Map<String, SimpleSerializer<?>> knownTypes) {
        // The base argument is a GreedyStringArgument, so we read the entire leftover user input
        super(
            new GreedyStringArgument(nodeName),
            info -> parseMap(info.sender(), info.input(), knownTypes));

        this.knownTypes = knownTypes;

        // Provide dynamic suggestions (keys vs. values)
        this.replaceSuggestions(ArgumentSuggestions.strings(this::suggestValues));
    }

    /**
     * <p>
     * Parses the entire map from the user's input, performing key validation and value deserialization
     * via {@link SimpleSerializer}.
     *
     * @param sender The command sender (for error context, if desired)
     * @param rawInput The raw input string (e.g. "{ammo:5,firemode:2}")
     * @param knownTypes Map of known keys -> {@code SimpleSerializer<?>}
     * @return A {@code Map<String,Object>} of parsed key-value pairs
     * @throws CustomArgumentException If any syntax or parse error occurs
     */
    private static Map<String, Object> parseMap(CommandSender sender,
        String rawInput,
        Map<String, SimpleSerializer<?>> knownTypes)
        throws CustomArgumentException {

        // Trim whitespace
        String input = rawInput.trim();
        if (input.isEmpty()) {
            // If empty, return an empty map
            return new HashMap<>();
        }

        // Optionally remove braces {}
        if (input.startsWith("{")) {
            input = input.substring(1).trim();
        }
        if (input.endsWith("}")) {
            input = input.substring(0, input.length() - 1).trim();
        }

        // Split on commas for key:value pairs
        Map<String, Object> result = new HashMap<>();
        String[] pairs = input.split(",");
        for (String segment : pairs) {
            String pair = segment.trim();
            if (pair.isEmpty())
                continue;

            int colonIndex = pair.indexOf(':');
            if (colonIndex < 0) {
                // Build an error message if user forgot a colon
                MessageBuilder mb = new MessageBuilder("Map syntax error: Missing ':' in '")
                    .appendArgInput() // will be replaced with the argument text
                    .append("'");
                throw CustomArgumentException.fromMessageBuilder(mb);
            }

            String key = pair.substring(0, colonIndex).trim();
            String valueString = pair.substring(colonIndex + 1).trim();

            SimpleSerializer<?> serializer = knownTypes.get(key);
            if (serializer == null) {
                // Build an error message for unknown key
                MessageBuilder mb = new MessageBuilder("Unknown key: ").append(key);
                throw CustomArgumentException.fromMessageBuilder(mb);
            }

            Object deserialized;
            try {
                // Let your serializer parse the string
                deserialized = serializer.deserialize(valueString, "command input");
            } catch (Exception ex) {
                // In your code, you might catch SerializerException specifically
                // But here we just catch everything
                MessageBuilder mb = new MessageBuilder("Failed to parse '")
                    .append(key)
                    .append("': ")
                    .append(ex.getMessage());
                throw CustomArgumentException.fromMessageBuilder(mb);
            }

            result.put(key, deserialized);
        }

        return result;
    }

    /**
     * <p>
     * Provides tab-completion suggestions. We check if the user is currently typing a {@code key} or a
     * {@code value}. If no colon has appeared for the last segment, we suggest possible keys.
     * Otherwise, we suggest possible values from the associated serializer's {@code exampleIterator()}.
     *
     * @param info The {@link SuggestionInfo} from CommandAPI
     * @return a future of possible suggestions
     */
    private String[] suggestValues(SuggestionInfo<?> info) {
        String soFar = info.currentArg().trim();

        // If user typed nothing, suggest open brace and any possible key
        if (soFar.isEmpty()) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("{");
            suggestions.addAll(buildKeySuggestions("", Collections.emptySet()));
            return suggestions.toArray(new String[0]);
        }

        // Split on commas, look at the last "segment"
        String[] segments = soFar.split(",");
        String lastSegment = segments[segments.length - 1].trim();

        // Strip possible leading or trailing braces
        if (lastSegment.startsWith("{")) {
            lastSegment = lastSegment.substring(1).trim();
        }
        if (lastSegment.endsWith("}")) {
            lastSegment = lastSegment.substring(0, lastSegment.length() - 1).trim();
        }

        // If there's no colon in that last segment, user is typing a key
        int colonIndex = lastSegment.indexOf(':');
        if (colonIndex < 0) {
            Set<String> usedKeys = parseUsedKeys(soFar);
            List<String> suggestions = buildKeySuggestions(lastSegment, usedKeys);
            return suggestions.toArray(new String[0]);
        } else {
            // user is typing a value
            String key = lastSegment.substring(0, colonIndex).trim();
            String partialVal = lastSegment.substring(colonIndex + 1).trim();

            SimpleSerializer<?> serializer = knownTypes.get(key);
            if (serializer == null) {
                // unknown key -> no suggestions
                return new String[0];
            }

            // Filter by partial match
            List<String> filtered = serializer.examples().stream()
                .filter(ex -> ex.toLowerCase().startsWith(partialVal.toLowerCase()))
                .toList();

            return filtered.toArray(new String[0]);
        }
    }

    /**
     * <p>
     * Collects all keys that have been typed so far (to prevent duplicate suggestions).
     *
     * @param input The entire command input typed so far.
     * @return A set of used keys.
     */
    private Set<String> parseUsedKeys(String input) {
        String temp = input.trim();
        if (temp.startsWith("{")) {
            temp = temp.substring(1);
        }
        if (temp.endsWith("}")) {
            temp = temp.substring(0, temp.length() - 1);
        }

        Set<String> used = new HashSet<>();
        String[] pairs = temp.split(",");
        for (String pair : pairs) {
            String seg = pair.trim();
            if (seg.isEmpty())
                continue;

            int c = seg.indexOf(':');
            if (c > 0) {
                String k = seg.substring(0, c).trim();
                used.add(k);
            }
        }
        return used;
    }

    /**
     * <p>
     * Builds suggestions for keys that have not been used yet. We also match a partial input so that if
     * the user typed "am", we might suggest "ammo:".
     *
     * @param partialKey The partial key typed so far
     * @param usedKeys The set of keys that have been used already
     * @return A list of possible "key:" suggestions
     */
    private List<String> buildKeySuggestions(String partialKey, Set<String> usedKeys) {
        List<String> suggestions = new ArrayList<>();
        for (String k : knownTypes.keySet()) {
            if (usedKeys.contains(k)) {
                continue; // skip if already used
            }
            if (!k.startsWith(partialKey)) {
                continue; // skip if doesn't match partial
            }
            suggestions.add(k + ":");
        }
        return suggestions;
    }
}
