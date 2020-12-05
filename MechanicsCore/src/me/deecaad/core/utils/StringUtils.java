package me.deecaad.core.utils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This utility class contains methods wrapping
 * around the idea of a <code>String</code>.
 * <p>
 * Also contains formatting methods to change
 * the format of a <code>String</code>
 */
public class StringUtils {

    public static final String LOWER_ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    public static final String VALID_HEX = "0123456789AaBbCcDdEeFf";
    public static final String CODES = VALID_HEX + "KkLlMmNnOoRrXx";

    /**
     * Don't let anyone instantiate this class
     */
    private StringUtils() {
    }

    /**
     * Counts the number of a given character in a String
     *
     * @param c      The character to check for
     * @param string The string to check in
     * @return How many c's are found in the string
     */
    public static int countChars(char c, String string) {
        return (int) string.chars().filter(character -> character == c).count();
    }

    @Nullable
    public static String match(String regex, String str) {
        Matcher matcher = Pattern.compile(regex).matcher(str);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    /**
     * Colors a given string
     *
     * @param string String to color
     * @return Colored String
     */
    public static String color(String string) {
        if (string == null) {
            return null;
        }

        StringBuilder result = new StringBuilder(string.length());

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (c != '&') {
                result.append(c);
            } else if (i != 0 && string.charAt(i - 1) == '/') {
                result.setCharAt(result.length() - 1, '&');
            } else if (i + 1 != string.length()) {
                if (CODES.indexOf(string.charAt(i + 1)) != -1) {
                    result.append('§');
                } else if (string.charAt(i + 1) == '#') {
                    int bound = i + 7;
                    if (bound <= string.length()) {

                        result.append('§').append('x');

                        // We have to skip forward 2 for the color code and hex symbol
                        i += 2;

                        // We could validate the hex, but we are just going
                        // to let people debug that themselves. The msg
                        // in chat will look obviously wrong, so people
                        // should have no problem checking their hex.

                        for (; i <= bound; i++) {
                            result.append('§').append(string.charAt(i));
                        }

                        i--;
                    }
                }
            }
        }

        return result.toString();
    }

    private static final String[] SUFFIXES = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};

    /**
     * The ordinal of an integer is
     *
     * @param i
     * @return
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
     * Example code:
     * <pre>
     * String pluginName = "ThisIsMyPluginName";
     * String[] splittedPluginName = StringUtil.splitCapitalLetters(pluginName);
     * System.out.println(Arrays.toString(splittedPluginName));
     * </pre>
     * Output:
     * <pre>
     * [This, Is, My, Plugin, Name]
     * </pre>
     *
     * @param from the string to split
     * @return the given string as array of strings
     */
    public static String[] splitCapitalLetters(String from) {
        return from.split("(?<=.)(?=\\p{Lu})");
    }

    /**
     * Splits after each word, removing spaces if present.
     * Example:
     * <blockquote><pre>{@code
     *     splitAfterWord("Hello"); // ["Hello"]
     *     splitAfterWord("Hello World"); // ["Hello", "World"]
     *     splitAfterWord("^<38 7&*9()"); // ["^<38", "7&*9()"]
     * }</pre></blockquote>
     * A word being defined as not whitespace, meaning that numbers
     * and special characters do count as words in this context
     *
     * @param from the string to split
     * @return the given string as an array
     */
    public static String[] splitAfterWord(String from) {
        return from.split("(?![\\S]+) |(?![\\S]+)");
    }

    /**
     * Splits with whitespaces (" "), minus ("-") and ("~") while allowing negative values also
     * Example:
     * <pre>
     *    split("SOUND-1-5"); // ["SOUND", "1", "1"]
     *    split("Value--2-6"); // ["Value", "-2", "6"]
     *    split("Something 22 -634"); // ["Something", "22", "-634"]
     *    split("Yayyy~6423~-2"); // ["Yayyy", "6424", "-2"]
     * </pre>
     *
     * @param from the string to split
     * @return the given string as an array
     */
    public static String[] split(String from) {
        return from.split("[~ ]+|(?<![~ -])-");
    }

    /**
     * Colors a given Array and returns it as a list.
     * Useful for TabCompletion stuff.
     * <p>
     * Note: Players can NOT use colors in chat/commands
     *
     * @param strings Array to convert
     * @return The list version of the array
     */
    public static List<String> getList(String... strings) {
        return Arrays.stream(strings).map(StringUtils::color).collect(Collectors.toList());
    }

    /**
     * Easy way to display where an error occurred in a file
     * during data serialization
     *
     * @param file Which file the error occurred in
     * @param path The path of configuration where the error occured
     * @return User readable location of error
     */
    public static String foundAt(File file, String path) {
        return "Located at file " + file + " in path " + path + " in configurations";
    }

    /**
     * Easy way to display where an error occurred in a file
     * during data serialization
     *
     * @param file          Which file the error occurred in
     * @param path          The path of configuration where the error occured
     * @param specification The specific thing that was wrong
     * @return User readable location of error
     */
    public static String foundAt(File file, String path, Object specification) {
        return "Located at file " + file + " in path " + path + " (" + specification.toString() + ") in configurations.";
    }

    /**
     * @param invalid what was invalid
     * @return general invalid thing notifier for debugger
     */
    public static String foundInvalid(String invalid) {
        return "Found an invalid " + invalid + " in configurations!";
    }

    /**
     * Gets the Minecraft <code>NamespacedKey</code> format
     * from the given camel case format. See below examples.
     *
     * <blockquote><pre>{@code
     *      camelToKey("iAmBob") // "i_am_bob"
     *      camelToKey("silkTouch") // "silk_touch"
     *      camelToKey("hey") // "hey"
     * }</pre></blockquote>
     *
     * @param camel The camelcase to reformat
     * @return The reformatted case
     */
    public static String camelToKey(String camel) {
        return String.join("_", camel.split("(?=[A-Z])")).toLowerCase();
    }

    /**
     * Gets constant format from the given
     * camel case format. See below examples.
     *
     * <blockquote><pre>{@code
     *      camelToConst("iAmBob") // "I_AM_BOB"
     *      camelToConst("silkTouch") // "SILK_TOUCH"
     *      camelToConst("hey") // "HEY"
     * }</pre></blockquote>
     *
     * @param camel The camelcase to reformat
     * @return The reformatted case
     */
    public static String camelToConst(String camel) {
        return String.join("_", camel.split("(?=[A-Z])")).toUpperCase();
    }

    /**
     * Translated a minecraft <code>NamespacedKey</code> formatted
     * <code>String</code> <i>OR</i> a java constant formatted
     * <code>String</code> into a more user readable format. See
     * below examples.
     *
     * <blockquote><pre>{@code
     *      keyToRead("SILK_TOUCH") // "Silk Touch"
     *      keyToRead("JAR_OF_DIRT") // "Jar Of Dirt"
     *      keyToRead("GOLDEN_SWORD") // "Golden Sword"
     * }</pre></blockquote>
     *
     * @param key The key to reformat
     * @return The user readable format
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
     * @return the string in usable format at error debugs
     * @see #didYouMean(String, Iterable)
     */
    public static <T extends Enum<T>> String debugDidYouMean(String input, Class<T> enumClazz) {
        return "Did you mean " + didYouMean(input, Enums.getOptions(enumClazz)) + " instead of " + input + "?";
    }

    /**
     * Gets the most similar <code>String</code> to
     * <code>input</code> found in <code>options</code>
     * <p>
     * Example:
     * <blockquote><pre>{@code
     *
     *      // Input:
     *      String correction = didYouMean("endermen", {"pig", "zombie", enderman});
     *      System.out.println("Unknown mob " + "endermen" + "... Did you mean " + correction + "?")
     *
     *      // Output
     *      [...] Unkown mob endermen... Did you mean enderman?
     * }</pre></blockquote>
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
            table[Character.toLowerCase(str.charAt(i)) - 97]++;
        }

        return table;
    }
}