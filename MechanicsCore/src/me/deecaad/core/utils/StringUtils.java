package me.deecaad.core.utils;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
}