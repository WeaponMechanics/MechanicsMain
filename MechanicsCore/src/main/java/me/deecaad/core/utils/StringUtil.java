package me.deecaad.core.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This final utility class outlines static methods that operate on or return
 * a {@link String}. This class also contains methods to help user-end
 * debugging of {@link me.deecaad.core.file.Configuration}.
 */
public final class StringUtil {

    public static final String LOWER_ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    public static final String VALID_HEX = "0123456789AaBbCcDdEeFf";
    public static final String CODES = VALID_HEX + "KkLlMmNnOoRrXx";
    private static final String[] SUFFIXES = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};

    // Don't let anyone instantiate this class.
    private StringUtil() {
    }

    /**
     * Counts the number of occurrences of <code>c</code> in the given
     * <code>string</code>.
     *
     * @param c      The character to check for.
     * @param string The non-null string to search in.
     * @return The number of occurrences.
     */
    public static int countChars(char c, @NotNull String string) {
        int count = 0;
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (character == c) {
                count++;
            }
        }
        return count;
    }

    /**
     * Repeats the given string a given number of times.
     *
     * @param str   The non-null string to repeat.
     * @param count The non-negative number of times to repeat.
     * @return The non-null repeated string.
     * @throws IllegalArgumentException If count is negative.
     */
    @NotNull
    public static String repeat(@NotNull String str, int count) {
        if (count < 0)
            throw new IllegalArgumentException("count cannot be negative");

        if (count == 0 || str.isEmpty()) {
            return "";
        } else if (count == 1) {
            return str;
        } else {
            StringBuilder builder = new StringBuilder(str.length() * count);
            while (count-- > 0)
                builder.append(str);
            return builder.toString();
        }
    }

    /**
     * Returns <code>true</code> if the character at the given index is an
     * "escaped" character. A character is considered escaped when a
     * NON-ESCAPED backslash ('\') precedes it.
     *
     * @param str   The non-null string to check.
     * @param index The index of the character to check.
     * @return true if the character is escaped.
     * @throws IndexOutOfBoundsException If index is out of bounds.
     */
    public static boolean isEscaped(@NotNull String str, int index) {
        if (index == 0)
            return false;

        // We have to count the number of preceding backslashes. An odd number
        // suggests that this character is escaped.
        int backslashes = 0;
        for (int i = index - 1; i >= 0; i--) {
            if (str.charAt(i) != '\\')
                break;
            backslashes++;
        }

        return backslashes % 2 == 1 || (str.charAt(index) == '\\' && backslashes % 2 == 0);
    }

    /**
     * Same as {@link #isEscaped(String, int)}, but the preceding backslash is
     * not considered an escaped character.
     *
     * @param str   The non-null string to test.
     * @param index The index of the character to test.
     * @return true if the character is escaped and should be included.
     */
    public static boolean isEscapedAndInclude(@NotNull String str, int index) {
        if (index == 0)
            return false;

        // We have to count the number of preceding backslashes. An odd number
        // suggests that this character is escaped.
        int backslashes = 0;
        for (int i = index - 1; i >= 0; i--) {
            if (str.charAt(i) != '\\')
                break;
            backslashes++;
        }

        return backslashes % 2 == 1;
    }

    /**
     * Matches the first possible string for a given regular expression in the
     * given <code>string</code>. If the expression's matcher does not match
     * any string, then this method will return <code>null</code>.
     *
     * <p>This method should only be used during data serialization as a
     * shorthand, since this method has to compile the regular expression for
     * every usage.
     *
     * @param regex The non-null regular expression to match.
     * @param str   The non-null string to search in.
     * @return The first found string, or <code>null</code>.
     */
    @Nullable
    public static String match(@NotNull String regex, @NotNull String str) {
        Matcher matcher = Pattern.compile(regex).matcher(str);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    /**
     * Colors a given <code>string</code> by replacing the <code>{@literal &}</code>
     * character with <code>{@literal §}</code>. This method also translates hex strings
     * formatted by <code>{@literal &}#000000</code> to a minecraft chat hex string.
     *
     * <p>This method should only be called during data serialization or through
     * bukkit commands.
     *
     * @param string The non-null string to color.
     * @return The non-null colored string.
     */
    @NotNull
    public static String color(@NotNull String string) {
        if (string == null)
            throw new IllegalArgumentException("string cannot be null!");

        StringBuilder result = new StringBuilder(string.length());

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (c != '&') {
                result.append(c);
            } else if (i != 0 && string.charAt(i - 1) == '\\') {
                result.setCharAt(result.length() - 1, '&');
            } else if (i + 1 != string.length()) {
                if (CODES.indexOf(string.charAt(i + 1)) != -1) {
                    result.append('\u00a7');
                } else if (string.charAt(i + 1) == '#') {
                    int bound = i + 7;
                    if (bound <= string.length()) {

                        result.append('\u00a7').append('x');

                        // We have to skip forward 2 for the color code and hex symbol
                        i += 2;

                        // We could validate the hex, but we are just going
                        // to let people debug that themselves. The msg
                        // in chat will look obviously wrong, so people
                        // should have no problem checking their hex.

                        for (; i <= bound; i++) {
                            result.append('\u00a7').append(string.charAt(i));
                        }

                        i--;
                    }
                } else {
                    result.append('&');
                }
            } else {
                result.append('&');
            }
        }

        return result.toString();
    }

    /**
     * Returns the string value of the config, adjusted to fit the
     * adventure format. Adventure text is formatting using html-like tags
     * instead of the legacy <code>{@literal &}</code> symbol. If the string in config
     * contains the legacy color system, we will attempt to convert it.
     *
     * <p>The returned string should be parsed using
     * {@link net.kyori.adventure.text.minimessage.MiniMessage}. You may
     * use MechanicsCore's instance {@link me.deecaad.core.MechanicsCore#message}.
     *
     * @return The string with the new format.
     */
    @Nullable
    public static String colorAdventure(@Nullable String value) {
        if (value == null)
            return null;

        value = value.replaceAll("\u00a7", "&");

        // Adventure text is formatted using tags <color></color> instead
        // of with symbols &7. While not a perfect fix, we can replace the
        // symbols with their equivalent open color tags.

        // Hardcoded literal colors (Hex is handled separately)
        final Map<String, String> replacements = new HashMap<>();
        replacements.put("&0", "<black>");
        replacements.put("&1", "<dark_blue>");
        replacements.put("&2", "<dark_green>");
        replacements.put("&3", "<dark_aqua>");
        replacements.put("&4", "<dark_red>");
        replacements.put("&5", "<dark_purple>");
        replacements.put("&6", "<gold>");
        replacements.put("&7", "<gray>");
        replacements.put("&8", "<dark_gray>");
        replacements.put("&9", "<blue>");
        replacements.put("&(a|A)", "<green>");
        replacements.put("&(b|B)", "<aqua>");
        replacements.put("&(c|C)", "<red>");
        replacements.put("&(d|D)", "<light_purple>");
        replacements.put("&(e|E)", "<yellow>");
        replacements.put("&(f|F)", "<white>");

        // Hardcoded literal decorations
        replacements.put("&(k|K)", "<obfuscated>");
        replacements.put("&(l|L)", "<bold>");
        replacements.put("&(m|M)", "<strikethrough>");
        replacements.put("&(n|N)", "<underline>");
        replacements.put("&(o|O)", "<italic>");
        replacements.put("&(r|R)", "<reset>");

        // Regex matcher to find hex color strings
        Pattern regex = Pattern.compile("&#([a-f]|[A-F]|\\d){6}");
        Matcher matcher = regex.matcher(value);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String match = matcher.group(0);
            String replacement = "<" + match.substring(1) + ">";
            matcher.appendReplacement(builder, replacement);
        }
        matcher.appendTail(builder);
        value = builder.toString();

        // Now convert simple colors and decorations
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            value = value.replaceAll(entry.getKey(), entry.getValue());
        }

        return value;
    }

    /**
     * Returns the ordinal of an integer. The ordinal is the number + the
     * english sound it ends with. E.x. 1st, 2nd, 3rd, 4th. Useful for
     * showing the index of something to the user.
     *
     * @param i The non-negative number to translate.
     * @return The non-null string ordinal.
     */
    @NotNull
    public static String ordinal(int i) {
        return switch (i % 100) {
            case 11, 12, 13 -> i + "th";
            default -> i + SUFFIXES[i % 10];
        };
    }

    /**
     * Splits a {@link String} before uppercase letters.
     *
     * <blockquote><pre>{@code
     *     String[] split = StringUtils.splitCapitalLetters("MyPluginName");
     *     System.out.println(Arrays.toString(split));
     *     // ["My", "Plugin", "Name"]
     * }</pre></blockquote>
     *
     * @param from The non-null strings to split.
     * @return The non-null split strings.
     */
    @NotNull
    public static String[] splitCapitalLetters(@NotNull String from) {
        return from.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
    }

    /**
     * Splits a {@link String} at whitespace between words.
     *
     * <blockquote><pre>{@code
     *     splitAfterWord("Hello"); // ["Hello"]
     *     splitAfterWord("Hello World"); // ["Hello", "World"]
     *     splitAfterWord("^<38 7&*9()"); // ["^<38", "7&*9()"]
     * }</pre></blockquote>
     *
     * @param from The non-null string to split.
     * @return The non-null split strings.
     */
    @NotNull
    public static String[] splitAfterWord(@NotNull String from) {
        return from.split("(?!\\S+) |(?!\\S+)");
    }

    /**
     * Splits a {@link String} at common delimiters, including white spaces,
     * tildes, and dashes. If there is a negative number separated by dashes,
     * the negative number will be formatted correctly.
     *
     * <blockquote><pre>{@code
     *    split("SOUND-1-5"); // ["SOUND", "1", "1"]
     *    split("Value--2-6"); // ["Value", "-2", "6"]
     *    split("Something 22 -634"); // ["Something", "22", "-634"]
     *    split("Yayyy~6423~-2"); // ["Yayyy", "6424", "-2"]
     * }</pre></blockquote>
     *
     * @param from The non-null string to split.
     * @return The non-null split strings.
     */
    @NotNull
    public static String[] split(@NotNull String from) {
        boolean addDash = false;
        if (from.startsWith("-")) {
            addDash = true;
            from = from.substring(1);
        }

        String[] split = from.split("[~ ]+|(?<![~ -])-");
        if (addDash)
            split[0] = "-" + split[0];

        return split;
    }

    /**
     * Returns a list of colored strings from the given array.
     *
     * @param strings The non-null strings to color and add to a list.
     * @return The non-null list of colored strings.
     */
    @NotNull
    public static List<String> getList(@NotNull String... strings) {
        List<String> temp = new ArrayList<>(strings.length);
        for (String str : strings) {
            temp.add(StringUtil.color(str));
        }
        return temp;
    }

    /**
     * Returns a string containing the location of key (Which file it is in,
     * and the path).
     *
     * @param file The non-null file containing the key.
     * @param path The non-null key in config.
     * @return The non-null human-readable location.
     */
    @NotNull
    public static String foundAt(@NotNull File file, @NotNull String path) {
        return "Located in file '" + file + "' at '" + path + "'";
    }

    @NotNull
    public static String foundAt(@NotNull File file, @NotNull String path, int i) {
        return "Located in file '" + file + "' at '" + path + "' (The " + ordinal(i) + " list item)";
    }

    /**
     * Translates a camel case {@link String} to a snake case {@link String}.
     *
     * <blockquote><pre>{@code
     *      camelToKey("iAmBob") // "i_am_bob"
     *      camelToKey("silkTouch") // "silk_touch"
     *      camelToKey("hey") // "hey"
     * }</pre></blockquote>
     *
     * @param camel The camel-case formatted {@link String}.
     * @return The name-spaced-key formatted {@link String}.
     */
    @NotNull
    public static String camelToSnake(@NotNull String camel) {
        return String.join("_", camel.split("(?=[A-Z])")).toLowerCase(Locale.ROOT);
    }

    @NotNull
    public static String upperSnakeCase(@NotNull String snake) {
        StringBuilder builder = new StringBuilder(snake.length());
        String[] split = snake.split("_");
        for (String str : split)
            builder.append(Character.toUpperCase(str.charAt(0))).append(str.substring(1)).append('_');

        if (split.length != 0)
            builder.setLength(builder.length() - 1);

        return builder.toString();
    }

    /**
     * Translates a snake case {@link String} to a user readable
     * {@link String}.
     *
     * <blockquote><pre>{@code
     *      keyToRead("SILK_TOUCH") // "Silk Touch"
     *      keyToRead("jar_of_dirt") // "Jar Of Dirt"
     *      keyToRead("GOLDEN_SWORD") // "Golden Sword"
     * }</pre></blockquote>
     *
     * @param key The non-null upper or lower snake-case {@link String}.
     * @return The non-null user readable format
     */
    @NotNull
    public static String keyToRead(@NotNull String key) {
        String[] split = key.toLowerCase(Locale.ROOT).split("_");

        StringBuilder builder = new StringBuilder();
        for (String s : split) {
            builder.append(s.substring(0, 1).toUpperCase(Locale.ROOT));
            builder.append(s.substring(1));
            builder.append(" ");
        }
        return builder.substring(0, builder.length() - 1);
    }

    /**
     * Returns the name of the most similar {@link Enum} to the
     * <code>input</code>.
     *
     * @param input     The non-null misspelled input.
     * @param enumClazz The non-null expected {@link Enum} type.
     * @param <T>       The <code>enumClazz</code> generic type.
     * @return The non-null user-readable correction.
     */
    @NotNull
    public static <T extends Enum<T>> String debugDidYouMean(@NotNull String input, @NotNull Class<T> enumClazz) {
        return "Did you mean " + didYouMean(input, EnumUtil.getOptions(enumClazz)) + " instead of " + input + "?";
    }

    /**
     * Maps the <code>input</code> to a table, and compares that table to every
     * {@link String} in <code>options</code>. This method then returns the
     * option that is most similar to the <code>input</code>.
     *
     * <p>This is most useful for spelling mistakes at the user end. By checking
     * their {@link Enum} inputs, for example, you can help the user debug
     * their issue automatically.
     *
     * <blockquote><pre>{@code
     *
     *      // Input:
     *      List<String> a = Arrays.asList("pig", "zombie", "enderman");
     *      String correction = didYouMean("endermen", a);
     *      System.out.println(correction);
     *
     *      // Output:
     *      enderman
     * }</pre></blockquote>
     *
     * @param input   The non-null user end input. This is the input that is
     *                possibly misspelled.
     * @param options All possible options that the input could be.
     * @return The most similar string to <code>input</code>.
     */
    @NotNull
    public static String didYouMean(@NotNull String input, @NotNull Iterable<String> options) {
        String closest = null;
        int difference = Integer.MAX_VALUE;
        int[] table = mapToCharTable(input.toLowerCase(Locale.ROOT));

        for (String str : options) {
            int[] localTable = mapToCharTable(str.toLowerCase(Locale.ROOT));
            int localDifference = Math.abs(str.length() - input.length());

            for (int i = 0; i < table.length; i++) {
                localDifference += Math.abs(table[i] - localTable[i]);
            }

            if (localDifference < difference) {
                closest = str;
                difference = localDifference;
            }
        }

        if (closest == null)
            throw new IllegalArgumentException("Passed 0 options");

        return closest;
    }

    public static int[] mapToCharTable(@NotNull String str) {
        int[] table = new int[LOWER_ALPHABET.length()];
        for (int i = 0; i < str.length(); i++) {

            // For performance reasons, we should check if the character is
            // in the array. We can ignore these characters, since they are
            // not as important as the a-z characters when identifying enums
            // and other options.
            int index = Character.toLowerCase(str.charAt(i)) - 97;
            if (index < 0 || index >= table.length)
                continue;

            table[index]++;
        }

        return table;
    }
}