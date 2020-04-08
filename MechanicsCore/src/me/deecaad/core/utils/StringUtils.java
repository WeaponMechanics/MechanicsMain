package me.deecaad.core.utils;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This utility class contains methods wrapping
 * around the idea of a <code>String</code>.
 *
 * Also contains formatting methods to change
 * the format of a <code>String</code>
 */
public class StringUtils {
    
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
    
    /**
     * Colors a given string
     *
     * @param string String to color
     * @return Colored String
     */
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
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
}