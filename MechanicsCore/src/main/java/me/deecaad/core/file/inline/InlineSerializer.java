package me.deecaad.core.file.inline;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerMissingKeyException;
import me.deecaad.core.file.inline.types.NestedType;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class InlineSerializer<T> implements Serializer<T> {

    /**
     * Default constructor for serializer.
     */
    public InlineSerializer() {
    }

    /**
     * Map constructor for inline-serializer.
     *
     * @param args The non-null arguments to build the object from.
     */
    public InlineSerializer(Map<Argument, Object> args) {
        // this is intentionally empty, and serves only to remind developers to
        // implement this constructor.
    }

    @Override
    public boolean shouldSerialize(SerializeData data) {
        // We don't want FileReader activating on these by default
        return false;
    }

    public abstract ArgumentMap args();

    @Override
    public abstract String getKeyword();

    /**
     * Reformats the <u>Upper_Snake_Case</u> YAML config key to
     * <u>lowerCamelCase</u> inline key. This helps make inline serialization
     * less sensitive to errors.
     *
     * @return The non-null lowerCamelCase keyword.
     */
    public final String getInlineKeyword() {
        String[] split = getKeyword().split("_");
        split[0] = split[0].toLowerCase(Locale.ROOT);
        return String.join("", split);
    }

    @NotNull
    @Override
    public T serialize(SerializeData data) throws SerializerException {

        // Expanded format
        if (data.config.isConfigurationSection(data.key)) {
            return expandedFormat(data);
        }

        // Inline format
        else if (data.config.isString(data.key)) {

            String line = data.config.getString(data.key);

            try {
                return inlineFormat(line);
            } catch (InlineException ex) {
                int index = ex.getIndex();
                boolean isIndexAccurate = true;
                if (index == -1) {
                    index = line.indexOf(ex.getLookAfter());
                    index = line.indexOf(ex.getIssue(), index == -1 ? 0 : index);
                    isIndexAccurate = false;
                }

                throw data.exception(null, ex.getException().getMessages())
                        .addMessage(line)
                        .addMessage(StringUtil.repeat(" ", index) + "^" + (isIndexAccurate ? "" : "          (Pointer location may be inaccurate)"));
            }
        }

        else {
            throw data.exception(null, "Could not find either a string or a configuration");
        }
    }

    public T expandedFormat(SerializeData data) {
        return null;
    }

    public T inlineFormat(String line) throws InlineException {
        String implied = getInlineKeyword();

        // Count trailing whitespace, so we can trim() the edges of the string
        // while keeping track of the index of errors in the string.
        int trailingWhitespace;
        for (trailingWhitespace = 0; trailingWhitespace < line.length(); trailingWhitespace++) {
            if (line.charAt(trailingWhitespace) != ' ')
                break;
        }

        // Since we already know which type the string will be parsed as, we
        // don't need to keyword. For example, when serializing a sound (so,
        // 'sound(type=...)'), we only need the `(...)` part.
        line = line.trim();
        if (line.toLowerCase(Locale.ROOT).startsWith(implied.toLowerCase(Locale.ROOT))) {
            line = line.substring(implied.length());
            trailingWhitespace += implied.length();
        }

        // Quick count of open and close parenthesis, to make sure the user has the
        // correct number and nothing is "malformed"
        int squareBrackets = 0;
        int parenthesis = 0;
        for (int i = 0; i < line.length(); i++) {
            if (StringUtil.isEscaped(line, i))
                continue;

            switch (line.charAt(i)) {
                case '(' -> parenthesis++;
                case ')' -> parenthesis--;
                case '[' -> squareBrackets++;
                case ']' -> squareBrackets--;
            }
        }

        // Every opening '(' needs a closing ')'
        if (parenthesis != 0) {
            int index = trailingWhitespace + (parenthesis > 0
                    ? index(line, '(', 0, line.lastIndexOf(')'), 1)
                    : index(line, ')', line.length() - 1, line.indexOf('('), -1));
            String[] messages = {"Missing " + (parenthesis > 0 ? " closing ')'" : " opening '('")};
            throw new InlineException(index, new SerializerException(this, messages, ""));
        }

        // Every opening '[' needs a closing ']'
        if (squareBrackets != 0) {
            int index = trailingWhitespace + (squareBrackets > 0
                    ? index(line, '[', 0, line.lastIndexOf(']'), 1)
                    : index(line, ']', line.length() - 1, line.indexOf('['), -1));
            String[] messages = {"Missing " + (squareBrackets > 0 ? " closing ']'" : " opening '['")};
            throw new InlineException(index, new SerializerException(this, messages, ""));
        }

        // We don't need the outside parenthesis either
        if (line.startsWith("(") && line.endsWith(")")) {
            line = line.substring(1, line.length() - 1);
            trailingWhitespace += 1;
        }

        // The Map we have is has a bunch of nested maps (assuming we are using
        // nested serializers). We need to search the nested maps and parse them
        // into serialized objects. We also need to fill in default values.
        Map<Argument, Object> args;
        try {
            args = serialize(line);
        } catch (InlineException ex) {
            if (ex.getIndex() != -1)
                ex.setIndex(ex.getIndex() + trailingWhitespace);
            else
                ex.setOffset(ex.getOffset() + trailingWhitespace);
            throw ex;
        }

        expand(this, args);
        InlineSerializer<?> serialized = ReflectionUtil.newInstance(ReflectionUtil.getConstructor(getClass(), Map.class),  args);
        return (T) serialized;
    }

    protected static void expand(InlineSerializer<?> serializer, Map<Argument, Object> args) throws InlineException {

        // First pass, we need to loop through the serializer's default
        // variables and add those into the arguments.
        for (Argument argument : serializer.args().getArgs().values()) {
            if (argument.isRequired() && !args.containsKey(argument))
                throw new InlineException("(", 1, new SerializerMissingKeyException(serializer, argument.getName(), ""));
            if (!argument.isRequired() && !args.containsKey(argument))
                args.put(argument, argument.getDefaultValue());
        }

        // Second pass, look for nested inline-serializers and parse them.
        for (Map.Entry<Argument, Object> entry : args.entrySet()) {
            Argument key = entry.getKey();

            if (!(key.getType() instanceof NestedType type))
                continue;

            // Recursively handle the nested expansion. The nested
            // inline-serializers have default values/required values as well!
            Map<Argument, Object> nested = (Map<Argument, Object>) entry.getValue();
            expand(type.getSerializer(), nested);

            InlineSerializer<?> serialized = ReflectionUtil.newInstance(ReflectionUtil.getConstructor(type.getSerializer().getClass(), Map.class),  nested);
            args.put(key, serialized);
        }
    }

    protected static int index(String str, char c, int start, int stop, int step) {
        for (int i = start; i != stop; i += step) {
            if (StringUtil.isEscaped(str, i))
                continue;

            if (str.charAt(i) == c)
                return i;
        }

        return stop;
    }

    protected Map<Argument, Object> serialize(String line) throws InlineException {

        Map<Argument, Object> serializedData = new HashMap<>();
        Map<Argument, Object> currentDepth = serializedData;
        LinkedList<Map<Argument, Object>> stack = new LinkedList<>();
        LinkedList<String> keyStack = new LinkedList<>();

        String key = null;
        StringBuilder value = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '(' || c == '[') {
                Map<Argument, Object> newDepth = new HashMap<>();
                currentDepth.put(args().getArgument(keyStack, key), newDepth);
                stack.push(newDepth);
                keyStack.push(key);

                currentDepth = newDepth;
                key = null;
                value.setLength(0);
            }

            else if (c == ')' || c == ']') {

                if (value.length() != 0) {
                    serializeArgument(keyStack, key, currentDepth, value);
                } else if (key != null) {
                    throw new InlineException(i, new SerializerException("", new String[] {"Formatting error... Found a key '" + key + "' without a value before a close character ('" + c + "')"}, ""));
                }

                currentDepth = stack.pop();
                key = null;
            }

            else if (c == '=' && key == null) {
                key = value.toString().strip();
                value.setLength(0);  // clear
            }

            else if (c == ',') {

                if (key == null)
                    throw new InlineException(i, new SerializerException("", new String[] {"Formatting error... Found comma character (',') without a 'key=value'"}, ""));
                if (value.length() == 0)
                    throw new InlineException(i, new SerializerException("", new String[] {"Formatting error... Found comma character (',') with a key '" + key + "' without a value"}, ""));

                serializeArgument(keyStack, key, currentDepth, value);
                key = null;
            }

            else {
                value.append(c);
            }
        }

        return serializedData;
    }

    private void serializeArgument(LinkedList<String> keyStack, String key, Map<Argument, Object> currentDepth, StringBuilder value) throws InlineException {
        Argument temp = args().getArgument(keyStack, key);
        currentDepth.put(temp, temp.serialize(value.toString().strip()));
        value.setLength(0);
    }
}
