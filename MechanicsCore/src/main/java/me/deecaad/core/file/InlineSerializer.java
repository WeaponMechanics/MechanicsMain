package me.deecaad.core.file;

import me.deecaad.core.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface InlineSerializer<T> extends Serializer<T> {

    String UNIQUE_IDENTIFIER = "uniqueIdentifier";

    @Override
    default boolean shouldSerialize(SerializeData data) {
        // We don't want FileReader activating on these by default
        return false;
    }

    /**
     * Reformats the <u>Upper_Snake_Case</u> YAML config key to
     * <u>UpperCamelCase</u> inline key. This helps make inline serialization
     * less sensitive to errors.
     *
     * @return The non-null lowerCamelCase keyword.
     */
    default String getInlineKeyword() {
        String[] split = getKeyword().split("_");
        //split[0] = split[0].toLowerCase(Locale.ROOT); // lower camel case
        return String.join("", split);
    }


    static Map<String, Object> inlineFormat(String line) throws FormatException {

        // Count trailing whitespace, so we can trim() the edges of the string
        // while keeping track of the index of errors in the string.
        int trailingWhitespace;
        for (trailingWhitespace = 0; trailingWhitespace < line.length(); trailingWhitespace++) {
            if (line.charAt(trailingWhitespace) != ' ')
                break;
        }
        line = line.trim();

        // This patterns groups the start of the string until the first '(' or
        // the end of the string. This is used to determine WHICH mechanic/
        // targeter/condition the user is trying to use.
        Pattern nameFinder = Pattern.compile(".+?(?=\\()");
        Matcher matcher = nameFinder.matcher(line);
        if (!matcher.find())
            throw new FormatException(trailingWhitespace, "Could not determine name... Before the first '(' you should have a name like 'Sound'");
        String uniqueIdentifier = matcher.group();

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
            throw new FormatException(index, "Missing" + (parenthesis > 0 ? " closing ')'" : " opening '('"));
        }

        // Every opening '[' needs a closing ']'
        if (squareBrackets != 0) {
            int index = trailingWhitespace + (squareBrackets > 0
                    ? index(line, '[', 0, line.lastIndexOf(']'), 1)
                    : index(line, ']', line.length() - 1, line.indexOf('['), -1));
            throw new FormatException(index, "Missing" + (squareBrackets > 0 ? " closing ']'" : " opening '['"));
        }

        // Take away the unique identifier and the outside parens
        line = line.substring(uniqueIdentifier.length());
        trailingWhitespace += uniqueIdentifier.length();
        if (line.startsWith("(") && line.endsWith(")")) {
            line = line.substring(1, line.length() - 1);
            trailingWhitespace++;
        }

        // This will return a map of strings, lists, and maps.
        Map<String, Object> map = mapify(line, trailingWhitespace);
        map.put(UNIQUE_IDENTIFIER, uniqueIdentifier.trim());
        return map;
    }

    static Map<String, Object> mapify(String line, int offset) throws FormatException {
        Map<String, Object> map = new HashMap<>();
        String key = null;
        StringBuilder value = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {

            // When a character is escaped, we should skip the special parsing.
            char c = line.charAt(i);
            if (StringUtil.isEscaped(line, i)) {
                if (StringUtil.isEscapedAndInclude(line, i))
                    value.append(c);

                // If the escaped character was the last character, we need an
                // extra check to make sure we add that value to the list.
                if (i + 1 >= line.length()) {
                    if (key == null)
                        throw new FormatException(offset + i, "Expected key=value, but was missing key... value=" + value);

                    map.put(key, value.substring(value.indexOf(" ") == 0 ? 1 : 0));
                }

                continue;
            }

            // Handle nested objects and lists
            if (c == '[' || c == '(') {
                int start = i + 1;
                int stop = start + findMatch(c, c == '[' ? ']' : ')', line.substring(start));

                if (c == '(') {
                    Map<String, Object> tempMap = mapify(line.substring(start, stop), start + offset);
                    map.put(key, tempMap);
                    if (!value.toString().isBlank())
                        tempMap.put(UNIQUE_IDENTIFIER, value.toString().trim());
                } else {
                    map.put(key, listify(line.substring(start, stop), start + offset));
                    if (!value.toString().isBlank())
                        throw new FormatException(offset + i, "Found '" + value + "' before a list... It should not be there!");
                }

                // Skip ahead
                i = stop;
                key = null;
                value.setLength(0);

                // If there is a comma, we should skip it
                if (i + 1 < line.length() && line.charAt(i + 1) == ',')
                    i++;
            }

            // We found the key! Now what is the value...?
            else if (c == '=') {
                if (key != null)
                    throw new FormatException(offset + i, "Found a duplicate '=' after '" + key + "'... Use '\\\\=' for escaped characters.");
                if (value.toString().isBlank())
                    throw new FormatException(offset + i, "Found an empty key");

                key = value.toString().trim();
                value.setLength(0);
            }

            // When we reach the end of the line or find a comma, then we *should*
            // have a key-value pair. So we have to save it.
            else if (c == ',' || i + 1 == line.length()) {

                // If this is the last character, make sure it is added to the
                // value. Don't add if the character is a comma since users can
                // end their list with an extra comma, and we will ignore it.
                if (i + 1 == line.length() && c != ',')
                    value.append(c);

                if (key == null)
                    throw new FormatException(offset + i, "Expected key=value, but was missing key... value=" + value);
                if (value.isEmpty())
                    throw new FormatException(offset + i, "Found an empty value");

                map.put(key, value.toString());
                key = null;
                value.setLength(0);
            }

            // When there is no special character to handle, the character
            // is probably just from a key or value.
            else {
                value.append(c);
            }
        }

        return map;
    }

    static List<Object> listify(String line, int offset) throws FormatException {
        List<Object> list = new ArrayList<>();
        StringBuilder value = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {

            // When a character is escaped, we should skip the special parsing.
            char c = line.charAt(i);
            if (StringUtil.isEscaped(line, i)) {
                if (StringUtil.isEscapedAndInclude(line, i))
                    value.append(c);

                // If the escaped character was the last character, we need an
                // extra check to make sure we add that value to the list.
                if (i + 1 >= line.length())
                    list.add(value.substring(value.indexOf(" ") == 0 ? 1 : 0));

                continue;
            }

            // Illegal characters
            if (c == '[' || c == '=')
                throw new FormatException(i + offset, "Illegal character '" + c + "'");

            // Handle nested objects
            if (c == '(') {
                int start = i + 1;
                int stop = start + findMatch(c, ')', line.substring(start));

                Map<String, Object> map = mapify(line.substring(start, stop), offset + stop);
                if (!value.toString().isBlank())
                    map.put(UNIQUE_IDENTIFIER, value.toString().trim());
                list.add(map);

                i = stop;
                value.setLength(0);

                // If there is a comma, we should skip it
                if (i + 1 < line.length() && line.charAt(i + 1) == ',')
                    i++;
            }

            // When we reach the end of the line or find a comma, then we
            // should add the value to the list.
            else if (c == ',' || i + 1 == line.length()) {

                // If this is the last character, make sure it is added to the
                // value. Don't add if the character is a comma since users can
                // end their list with an extra comma, and we will ignore it.
                if (i + 1 == line.length() && c != ',')
                    value.append(c);

                if (value.isEmpty())
                    throw new FormatException(i + offset, "Found duplicate commas... Use '\\\\,' for an escaped comma");

                list.add(value.substring(value.indexOf(" ") == 0 ? 1 : 0));
                value.setLength(0);
            }

            else {
                value.append(c);
            }
        }

        return list;
    }

    private static int findMatch(char open, char close, String line) {
        int nested = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (StringUtil.isEscaped(line, i))
                continue;

            if (c == open)
                nested++;
            else if (c == close && nested > 0)
                nested--;
            else if (c == close && nested == 0)
                return i;
        }

        throw new IllegalArgumentException("Could not find match for '" + open + "' in '" + line + "'");
    }

    private static int index(String str, char c, int start, int stop, int step) {
        for (int i = start; i != stop; i += step) {
            if (StringUtil.isEscaped(str, i))
                continue;

            if (str.charAt(i) == c)
                return i;
        }

        return stop;
    }

    /**
     * This exception is used whenever the user inputs an improperly formatted
     * string. This is basically a Syntax Error.
     */
    class FormatException extends Exception {

        private int index;

        public FormatException(int index) {
            this.index = index;
        }

        public FormatException(int index, String message) {
            super(message);
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
