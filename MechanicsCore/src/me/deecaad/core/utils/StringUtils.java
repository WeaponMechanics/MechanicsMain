package me.deecaad.core.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This final utility class outlines static methods that operate on or return
 * a {@link String}. This class also contains methods to help user-end
 * debugging of {@link me.deecaad.core.file.Configuration}.
 */
public final class StringUtils {

    public static final String LOWER_ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    public static final String VALID_HEX = "0123456789AaBbCcDdEeFf";
    public static final String CODES = VALID_HEX + "KkLlMmNnOoRrXx";
    private static final String[] SUFFIXES = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};

    // Don't let anyone instantiate this class
    private StringUtils() {
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
     * This method should only be called during data serialization or through
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
        return from.split("(?<=.)(?=\\p{Lu})");
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
        return from.split("[~ ]+|(?<![~ -])-");
    }

    /**
     * Returns a list of colored strings from the given array.
     *
     * @param strings The non-null strings to color and add to a list.
     * @return The non-null list of colored strings.
     */
    public static List<String> getList(String...strings) {
        List<String> temp = new ArrayList<>(strings.length);
        for (String str : strings) {
            temp.add(StringUtils.color(str));
        }
        return temp;
    }

    /**
     * Returns a {@link String} containing the file path and configuration
     * path. Should be used to help the user find exactly where in their config
     * an errors occurs.
     *
     * @param file The non-null hard file location.
     * @param path The non-null path to the exact config key.
     * @return The non-null user-readable location.
     */
    public static String foundAt(File file, String path) {
        return "Located at file " + file + " in path " + path + " in configurations";
    }

    /**
     * Returns a {@link String} containing the file path and configuration path.
     * @param file
     * @param path
     * @param specification
     * @return
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
     * A warning message for when somebody uses an absurdly wrong number in configurations
     *
     * @see Debugger#warn(String...)
     *
     * @param number The absurd number
     * @param file The file the number is found in
     * @param path The path to the number
     * @return Predetermined warning message
     */
    public static String[] foundLarge(double number, File file, String path) {
        return new String[]{
                "WARNING: Found a large number in configurations (" + number + ")",
                "This is not an error, but you should be careful when using large numbers, and this may be a mistake.",
                StringUtils.foundAt(file, path + ".Airstrike.Maximum_Bombs")
        };
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
        for (String s: split) {
            builder.append(s.substring(0, 1).toUpperCase());
            builder.append(s.substring(1));
            builder.append(" ");
        }
        return builder.substring(0, builder.length() - 1);
    }

    /**
     * @see #didYouMean(String, Iterable)
     *
     * @return the string in usable format at error debugs
     */
    public static <T extends Enum<T>> String debugDidYouMean(String input, Class<T> enumClazz) {
        return "Did you mean " + didYouMean(input, Enums.getOptions(enumClazz)) + " instead of " + input + "?";
    }
    
    /**
     * Gets the most similar <code>String</code> to
     * <code>input</code> found in <code>options</code>
     *
     * Example:
     * <blockquote><pre>{@code
     *
     *      // Input:
     *      String correction = didYouMean("endermen", {"pig", "zombie", "enderman"});
     *      System.out.println("Unknown mob " + "endermen" + "... Did you mean " + correction + "?")
     *
     *      // Output
     *      [...] Unknown mob endermen... Did you mean enderman?
     * }</pre></blockquote>
     *
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