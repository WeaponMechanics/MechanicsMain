package me.deecaad.core.mechanics.inline;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializerException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import static me.deecaad.core.mechanics.inline.InlineSerializer.BRACKETS;

public class Arguments {

    public static final ArgumentSerializer<Integer> INTEGER = Integer::parseInt;

    private final Map<String, ArgumentSerializer<?>> args;

    public Arguments() {
        args = new HashMap<>();
    }

    public Arguments addArgument(String name, ArgumentSerializer<?> type) {
        args.put(name.toLowerCase(Locale.ROOT), type);
        return this;
    }

    /**
     * This serialization method expects "map-formatted" input
     * (<code>key1=value1, key2=value2, key3=value3</code>). This method should
     * probably not be called from any other location then
     * {@link InlineSerializer#parse(String, Map)}
     *
     * @param str The non-null, non-empty string to serialize.
     * @throws InlineException If formatting is inproper, or type doesn't match.
     */
    public void parse(String str) throws InlineException {
        StringBuilder temp = new StringBuilder();

        int startValue = -1;
        ArgumentSerializer<?> arg = null;
        LinkedList<Character> stack = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (InlineSerializer.isWhitespace(c))
                continue;

            // Once we find our assignment operator, we can now take a substring
            // (until the next semicolon) and attempt to parse that as an arg.
            if (arg == null && (c == '=' || c == ':')) {
                arg = args.get(temp.toString().toLowerCase(Locale.ROOT));

                if (arg == null)
                    throw new InlineException(i, "Could not match config to any Argument",
                            SerializerException.forValue(temp.toString()),
                            SerializerException.didYouMean(temp.toString(), args.keySet()),
                            SerializerException.possibleValues(args.keySet(), temp.toString(), MechanicsCore.getPlugin().getConfig().getInt("Show_Serialize_Options", 32)));
            }

            // At this point, we've found the argument name and assignment
            // operator. For 95% of arguments, we can just trace forward
            // to the next semicolon, BUT we have to consider nested args.
            if (arg != null) {
                if (BRACKETS.containsKey(c))
                    stack.push(c);
                if (!stack.isEmpty() && c == BRACKETS.get(stack.peek()))
                    stack.pop();
            }

            // Handle escaped semicolons
            if (c == ';') {
                if (str.charAt(i + 1) == ';') {
                    i += 1;
                    continue;
                }


            } else if (c == ',') {
                if (str.charAt(i + 1) == 'c') {
                    i += 1;
                    continue;
                }
            }
        }
    }

    /**
     * The argument parser is used to serialize an object from a string. Since
     * YAML files don't have tab-completions, the format should be kept simple.
     *
     * @param <T> Type of the serialized object.
     */
    public interface ArgumentSerializer<T> {
        T serialize(String str) throws Exception;
    }
}
