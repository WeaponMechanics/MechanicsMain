package me.deecaad.core.utils;

import me.deecaad.core.MechanicsCore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
    public static int countChars(char c, String string) {
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
    public static String match(@Nonnull String regex, @Nonnull String str) {
        Matcher matcher = Pattern.compile(regex).matcher(str);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    /**
     * Colors a given <code>string</code> by replacing the <code>&</code>
     * character with <code>§</code>. This method also translates hex strings
     * formatted by <code>&#000000</code> to a minecraft chat hex string.
     *
     * <p>This method should only be called during data serialization or through
     * bukkit commands.
     *
     * @param string The non-null string to color.
     * @return The non-null colored string.
     */
    public static String color(String string) {
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
     * Returns the ordinal of an integer. The ordinal is the number + the
     * english sound it ends with. E.x. 1st, 2nd, 3rd, 4th. Useful for
     * showing the index of something to the user.
     *
     * @param i The non-negative number to translate.
     * @return The non-null string ordinal.
     */
    public static String ordinal(int i) {
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + SUFFIXES[i % 10];

        }
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
    public static String[] splitCapitalLetters(String from) {
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
    public static String[] splitAfterWord(String from) {
        return from.split("(?![\\S]+) |(?![\\S]+)");
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
    public static String[] split(String from) {
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
    public static List<String> getList(String... strings) {
        List<String> temp = new ArrayList<>(strings.length);
        for (String str : strings) {
            temp.add(StringUtil.color(str));
        }
        return temp;
    }

    /**
     * Returns a {@link String} containing the file path and configuration
     * path. Should be used to help the user find exactly where in their config
     * an error occurs.
     *
     * @param file The non-null hard file location.
     * @param path The non-null path to the exact config key.
     * @return The non-null user-readable location.
     */
    public static String foundAt(File file, String path) {
        return "Located at file " + file + " in path " + path + " in configurations";
    }

    /**
     * Returns a {@link String} containing the file path, configuration path
     * and the incorrect value. Should be used to help the user find exactly
     * where in their config an error occurs.
     *
     * @param file  The non-null hard file location.
     * @param path  The non-null path to the exact config key.
     * @param value The incorrect value that was input by the user.
     * @return The non-null user-readable location.
     */
    public static String foundAt(File file, String path, Object value) {
        return "Located at file " + file + " in path " + path + " (" + value + ") in configurations.";
    }

    /**
     * Returns a formatted {@link String} which explains which configuration
     * key is incorrect.
     *
     * @param invalid The invalid key.
     * @return The non-null invalid key.
     */
    public static String foundInvalid(String invalid) {
        return "Found an invalid " + invalid + " in configurations!";
    }

    /**
     * Returns a list of formatted strings that warns the user about a possible
     * mistake, and where that mistake may be.
     *
     * @param number The number that is absurdly large.
     * @param file   The non-null hard file location.
     * @param path   The non-null path to the exact config key.
     * @return The non-null warning to the user.
     * @see Debugger#warn(String...)
     */
    public static String[] foundLarge(double number, File file, String path) {
        return new String[]{
                "WARNING: Found a large number in configurations (" + number + ")",
                "This is not an error, but you should be careful when using large numbers, and this may be a mistake.",
                StringUtil.foundAt(file, path)
        };
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
    public static String camelToSnake(String camel) {
        return String.join("_", camel.split("(?=[A-Z])")).toLowerCase();
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
    public static String keyToRead(String key) {
        String[] split = key.toLowerCase().split("_");

        StringBuilder builder = new StringBuilder();
        for (String s : split) {
            builder.append(s.substring(0, 1).toUpperCase());
            builder.append(s.substring(1));
            builder.append(" ");
        }
        return builder.substring(0, builder.length() - 1);
    }

    /**
     * Returns the name of the most similar {@link Enum} to the
     * <code>input</code>.
     *
     * @param input     The non-null mis-spelled input.
     * @param enumClazz The non-null expected {@link Enum} type.
     * @param <T>       The <code>enumClazz</code> generic type.
     * @return The non-null user-readable correction.
     */
    public static <T extends Enum<T>> String debugDidYouMean(String input, Class<T> enumClazz) {
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
     *                possible misspelled.
     * @param options All of the possible options that the input could be.
     * @return The most similar string to <code>input</code>.
     */
    public static String didYouMean(String input, Iterable<String> options) {
        String closest = null;
        int difference = Integer.MAX_VALUE;
        int[] table = mapToCharTable(input.toLowerCase());

        for (String str : options) {
            int[] localTable = mapToCharTable(str.toLowerCase());
            int localDifference = 0;

            for (int i = 0; i < table.length; i++) {
                localDifference += Math.abs(table[i] - localTable[i]);
            }

            if (localDifference < difference) {
                closest = str;
                difference = localDifference;
            }
        }

        return closest;
    }

    private static int[] mapToCharTable(String str) {
        int[] table = new int[LOWER_ALPHABET.length()];
        for (int i = 0; i < str.length(); i++) {
            try {
                table[Character.toLowerCase(str.charAt(i)) - 97]++;
            } catch (ArrayIndexOutOfBoundsException e) {
                MechanicsCore.debug.error("Unexpected character: " + str.charAt(i));
            }
        }
        return table;
    }
}