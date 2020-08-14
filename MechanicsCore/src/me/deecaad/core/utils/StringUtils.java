package me.deecaad.core.utils;

import me.deecaad.compatibility.CompatibilityAPI;

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
 *
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
     * @param c The character to check for
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

        char[] chars = string.toCharArray();

        // Gets the current minecraft version. Used for 1.16+ hex codes
        double ver = CompatibilityAPI.getVersion();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            // Check for alternate color code and escape character
            if (c != '&') {
                result.append(c);
                continue;
            } else if (i != 0 && chars[i - 1] == '/') {
                result.deleteCharAt(result.length() - 1);
                result.append('&');
                continue;
            }

            // Check for advanced 1.16+ hex colors
            if (ver >= 1.16) {

                int bound = i + 7;
                if (bound <= chars.length) {
                    StringBuilder hex = new StringBuilder("§x");
                    boolean isHex = true;   // true until proven false

                    for (int j = i + 1; j < bound; j++) {
                        if (VALID_HEX.indexOf(chars[j]) != -1) {
                            hex.append('§').append(chars[j]);
                        } else {
                            isHex = false;
                            break;
                        }
                    }

                    if (isHex) {
                        result.append(hex);
                        i += 6;
                        continue;
                    }
                }
            }

            if (i + 1 != chars.length) {
                char code = chars[i + 1];
                if (CODES.indexOf(code) != -1) {
                    result.append('§').append(code);
                    i++;
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
     *    split("SOUND-1-5"); // [SOUND, 1, 1]
     *    split("Value--2-6"); // [Value, -2, 6]
     *    split("Something 22 -634"); // [Something, 22, -634]
     *    split("Yayyy~6423~-2"); // [Yayyy, 6424, -2]
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
     *
     * Note: Players can NOT use colors in chat/commands
     *
     * @param strings Array to convert
     * @return The list version of the array
     */
    public static List<String> getList(String...strings) {
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
        return "Error found in directory \"" + file + "\" at path \"" + path + "\"";
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
     * @param enumClazz the enum class
     */
    public static String didYouMean(String input, Class<? extends Enum<?>> enumClazz) {
        return didYouMean(input, Arrays.stream(enumClazz.getEnumConstants()).map(Enum::name).collect(Collectors.toList()));
    }
    
    /**
     * Gets the most similar <code>String</code> to
     * <code>input</code> found in <code>options</code>
     *
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
            table[str.charAt(i) - 97]++;
        }
        
        return table;
    }
}