package me.deecaad.core.utils

import org.intellij.lang.annotations.RegExp
import java.util.regex.Pattern
import kotlin.math.abs

/**
 * Utility class for working with strings.
 */
object StringUtil {
    const val LOWER_ALPHABET = "abcdefghijklmnopqrstuvwxyz"
    const val VALID_HEX = "0123456789AaBbCcDdEeFf"
    const val MINECRAFT_COLOR_CODES = VALID_HEX + "KkLlMmNnOoRrXx"

    private val SUFFIXES = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")

    /**
     * Counts the occurrences of a character in a string.
     *
     * @param str The string to search in.
     * @param char The character to search for.
     * @return The number of occurrences of the character in the string.
     */
    @JvmStatic
    fun countOccurrences(
        str: String,
        char: Char,
    ): Int {
        return str.count { it == char }
    }

    /**
     * Repeat a string a number of times.
     *
     * If the string is empty, or the number of times is less than or equal to
     * 0, an empty string is returned.
     *
     * @param str The string to repeat.
     * @param times The number of times to repeat the string.
     * @return The repeated string.
     */
    @JvmStatic
    fun repeat(
        str: String,
        times: Int,
    ): String {
        if (str.isEmpty() || times <= 0) {
            return ""
        }
        return str.repeat(times)
    }

    /**
     * Returns `true` if the character at the given index is escaped.
     *
     * A character is considered escaped if it is preceded by a **non-escaped**
     * backslash (`\`).
     *
     * When `includeBackslash` is `true`, the backslash character itself is
     * considered an escape character.
     *
     * @param str The string to check.
     * @param index The index of the character to check.
     * @return `true` if the character is escaped, `false` otherwise.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    @JvmStatic
    @JvmOverloads
    fun isEscaped(
        str: String,
        index: Int,
        includeBackslash: Boolean = false,
    ): Boolean {
        if (index == 0) {
            return false
        }

        // An odd number of backslashes means the character is escaped
        var backslashes = 0
        for (i in index - 1 downTo 0) {
            if (str[i] != '\\') {
                break
            }
            backslashes++
        }

        // The `|| str[index] == '\\'` part is needed to handle the characters
        // that ESCAPE a character. It counts the backslashes preceding the
        // escape character as an escaped character.
        return backslashes % 2 == 1 || (!includeBackslash && str[index] == '\\')
    }

    /**
     * Matches the first occurrence of a regex in a string, or returns `null`.
     *
     * @param regex The regex to match.
     * @param str The string to search in.
     * @return The first occurrence of the regex in the string, or `null`.
     */
    @JvmStatic
    fun match(
        @RegExp regex: String,
        str: String,
    ): String? {
        return match(regex.toRegex(), str)
    }

    /**
     * Matches the first occurrence of a regex in a string, or returns `null`.
     *
     * @param regex The regex to match.
     * @param str The string to search in.
     * @return The first occurrence of the regex in the string, or `null`.
     */
    @JvmStatic
    fun match(
        regex: Regex,
        str: String,
    ): String? {
        val match = regex.find(str)
        return match?.value
    }

    /**
     * Colors a given string using Minecraft color codes by replacing the
     * ampersand (`&`) with the section symbol (`§`).
     *
     * Supports hex strings formatted by `&#RRGGBB`, by using the `&x` code.
     * For example, `&#ff0000` gets translates to `§x§f§f§0§0§0§0`.
     *
     * @param str The string to colorize.
     * @return The colorized string.
     */
    @JvmStatic
    fun colorBukkit(str: String): String {
        val result = StringBuilder(str.length)

        var i = 0
        while (i < str.length) {
            val c: Char = str[i]
            if (c != '&') {
                result.append(c)
            } else if (i != 0 && str[i - 1] == '\\') {
                result.setCharAt(result.length - 1, '&')
            } else if (i + 1 != str.length) {
                if (str[i + 1] in MINECRAFT_COLOR_CODES) {
                    result.append('§')
                } else if (str[i + 1] == '#') {
                    val bound = i + 7
                    if (bound <= str.length) {
                        result.append('§').append('x')

                        // We have to skip forward 2 for the color code and hex symbol
                        i += 2

                        // We could validate the hex, but we are just going
                        // to let people debug that themselves. The msg
                        // in chat will look obviously wrong, so people
                        // should have no problem checking their hex.
                        while (i <= bound) {
                            result.append('§').append(str[i])
                            i++
                        }

                        // We have to go back one because the loop will
                        // increment the index one more time
                        i--
                    }
                } else {
                    result.append('&')
                }
            } else {
                result.append('&')
            }
            i++
        }

        return result.toString()
    }

    /**
     * Colors a given string using Adventure color codes by replacing the
     * ampersand (`&`) with the corresponding adventure tag.
     *
     * This is useful for converting strings to the MiniMessage format provided
     * by the Adventure library. Since users mostly prefer to use the Minecraft
     * color codes, this method will convert the codes to the Adventure format.
     *
     * @param value The string to colorize.
     * @return The colorized string.
     */
    @JvmStatic
    fun colorAdventure(value: String): String {
        // Basically, if the user is trying to use MC color codes, we try to
        // translate them to Bukkit codes. Doesn't properly handle &x format.
        var value = value.replace('§', '&')

        // Adventure text is formatted using tags <color></color> instead
        // of with symbols &7. While not a perfect fix, we can replace the
        // symbols with their equivalent open color tags.

        // Hardcoded literal colors (Hex is handled separately)
        val replacements: MutableMap<String, String> = HashMap()
        replacements["&0"] = "<black>"
        replacements["&1"] = "<dark_blue>"
        replacements["&2"] = "<dark_green>"
        replacements["&3"] = "<dark_aqua>"
        replacements["&4"] = "<dark_red>"
        replacements["&5"] = "<dark_purple>"
        replacements["&6"] = "<gold>"
        replacements["&7"] = "<gray>"
        replacements["&8"] = "<dark_gray>"
        replacements["&9"] = "<blue>"
        replacements["&(a|A)"] = "<green>"
        replacements["&(b|B)"] = "<aqua>"
        replacements["&(c|C)"] = "<red>"
        replacements["&(d|D)"] = "<light_purple>"
        replacements["&(e|E)"] = "<yellow>"
        replacements["&(f|F)"] = "<white>"

        // Hardcoded literal decorations
        replacements["&(k|K)"] = "<obfuscated>"
        replacements["&(l|L)"] = "<bold>"
        replacements["&(m|M)"] = "<strikethrough>"
        replacements["&(n|N)"] = "<underline>"
        replacements["&(o|O)"] = "<italic>"
        replacements["&(r|R)"] = "<reset>"

        // Regex matcher to find hex color strings
        val regex = Pattern.compile("&#([a-f]|[A-F]|\\d){6}")
        val matcher = regex.matcher(value)
        val builder = java.lang.StringBuilder()
        while (matcher.find()) {
            val match = matcher.group(0)
            val replacement = "<" + match.substring(1) + ">"
            matcher.appendReplacement(builder, replacement)
        }
        matcher.appendTail(builder)
        value = builder.toString()

        // Now convert simple colors and decorations
        for ((key, value1) in replacements) {
            value = value.replace(key.toRegex(), value1)
        }
        return value
    }

    /**
     * Returns the ordinal representation of a number.
     *
     * For example, `1` becomes `1st`, `2` becomes `2nd`, `3` becomes `3rd`,
     * `4` becomes `4th`, and so on.
     *
     * @param number The number to convert.
     * @return The ordinal representation of the number.
     */
    @JvmStatic
    fun ordinal(number: Int): String {
        return when {
            number % 100 in 11..13 -> number.toString() + "th"
            else -> number.toString() + SUFFIXES[number % 10]
        }
    }

    /**
     * Splits a string before uppercase letters.
     *
     * ```
     *  List<String> split = StringUtils.splitCapitalLetters("HelloWorld");
     *  // split = ["Hello", "World"]
     * ```
     *
     * @param str The string to split.
     * @return The split string.
     */
    @JvmStatic
    fun splitCapitalLetters(str: String): List<String> {
        return str.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])".toRegex())
    }

    /**
     * Splits a string after a word.
     *
     * ```
     *  List<String> split = StringUtils.splitAfterWord("Hello World");
     *  // split = ["Hello", "World"]
     * ```
     *
     * @param str The string to split.
     * @return The split string.
     */
    @JvmStatic
    fun splitAfterWord(str: String): List<String> {
        return str.split("(?!\\S+) |(?!\\S+)".toRegex()).filter {
            it.isNotBlank()
        }
    }

    /**
     * Splits a string at common delimiters (whitespace, tilde `~`, and
     * dashes `-`).
     *
     * This method correctly handles negative numbers separated by dashes.
     *
     * ```
     *  StringUtil.split("SOUND-1-5"); // ["SOUND", "1", "1"]
     *  StringUtil.split("Value--2-6"); // ["Value", "-2", "6"]
     *  StringUtil.split("Something 22 -634"); // ["Something", "22", "-634"]
     *  StringUtil.split("Yayyy~6423~-2"); // ["Yayyy", "6424", "-2"]
     * ```
     *
     * @param str The string to split.
     * @return The split string.
     */
    @JvmStatic
    fun split(str: String): List<String> {
        var str = str
        var addDash = false
        if (str.startsWith("-")) {
            addDash = true
            str = str.substring(1)
        }
        val split = str.split("[~ ]+|(?<![~ -])-".toRegex()).toMutableList()
        if (addDash) split[0] = "-" + split[0]
        return split
    }

    /**
     * Converts a string to snake case.
     *
     * For example, `helloWorld` becomes `hello_world`.
     *
     * @param camel The string to convert.
     * @return The snake case string.
     */
    @JvmStatic
    fun camelToSnake(camel: String): String {
        val builder = StringBuilder()
        for (i in camel.indices) {
            val c = camel[i]
            if (i != 0 && c.isUpperCase()) {
                builder.append('_')
            }
            builder.append(c)
        }
        return builder.toString()
    }

    /**
     * Converts a string from lower snake case to upper snake case.
     *
     * For example, `hello_world` becomes `Hello_World`.
     *
     * @param snake The string in lower snake case to convert.
     * @return The string in upper snake case.
     */
    @JvmStatic
    fun snakeToUpperSnake(snake: String): String {
        val builder = StringBuilder()
        for (i in snake.indices) {
            val c = snake[i]
            if (i == 0 || snake[i - 1] == '_') {
                builder.append(c.uppercaseChar())
            } else {
                builder.append(c)
            }
        }
        return builder.toString()
    }

    /**
     * Converts a string from snake case (upper or lower, doesn't matter) to
     * a user readable string.
     *
     * For example, `hello_world` becomes `Hello World`.
     *
     * @param snake The string in snake case to convert.
     * @return The readable string.
     */
    @JvmStatic
    fun snakeToReadable(snake: String): String {
        val builder = StringBuilder()
        for (i in snake.indices) {
            val c = snake[i]
            if (c == '_') {
                builder.append(' ')
            } else if (i == 0 || snake[i - 1] == '_') {
                builder.append(c.uppercaseChar())
            } else {
                builder.append(c)
            }
        }
        return builder.toString()
    }

    /**
     * Returns the option from `possibilities` that is most similar to `input`.
     *
     * This method uses a primitive character table to compare the similarity
     * of the strings. The string with the smallest difference is returned.
     *
     * @param input The input string to compare.
     * @param possibilities The list of possibilities to compare to.
     * @return The most similar string from `possibilities`.
     * @throws IllegalArgumentException if `possibilities` is empty.
     */
    @JvmStatic
    fun didYouMean(
        input: String,
        possibilities: Iterable<String>,
    ): String {
        var closest: String? = null
        var closestDistance = Int.MAX_VALUE
        val table = toCharTable(input)

        for (possibility in possibilities) {
            val localTable = toCharTable(possibility)
            var localDifference = abs(input.length - possibility.length)

            for (i in table.indices) {
                localDifference += abs(table[i] - localTable[i])
            }

            if (localDifference < closestDistance) {
                closest = possibility
                closestDistance = localDifference
            }
        }

        return closest ?: throw IllegalArgumentException("You passed 0 possibilities to the didYouMean function.")
    }

    /**
     * Returns a character table for a string.
     *
     * The character table is an array of integers where each index represents
     * a character in the alphabet. The value at each index is the number of
     * occurrences of the character in the string.
     *
     * @param str The string to convert to a character table.
     * @return The character table.
     */
    @JvmStatic
    fun toCharTable(str: String): IntArray {
        val str = str.lowercase()
        val table: IntArray = IntArray(LOWER_ALPHABET.length)
        for (c in str) {
            val index = c.lowercaseChar() - 'a'
            if (index < 0 || index >= table.size) {
                continue
            }

            table[index]++
        }

        return table
    }
}
