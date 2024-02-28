package me.deecaad.core.utils

import me.deecaad.core.utils.StringUtil.colorBukkit
import me.deecaad.core.utils.StringUtil.countOccurrences
import me.deecaad.core.utils.StringUtil.didYouMean
import me.deecaad.core.utils.StringUtil.isEscaped
import me.deecaad.core.utils.StringUtil.match
import me.deecaad.core.utils.StringUtil.ordinal
import me.deecaad.core.utils.StringUtil.repeat
import me.deecaad.core.utils.StringUtil.split
import me.deecaad.core.utils.StringUtil.splitAfterWord
import me.deecaad.core.utils.StringUtil.splitCapitalLetters
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class StringUtilTest {

    @ParameterizedTest
    @CsvSource("bob,b,2", "deecaad,d,2", "cjcrafter,z,0", "darkman,k,1", "mississippi,i,4")
    fun test_countChars(str: String, c: Char, expected: Int) {
        assertEquals(expected, countOccurrences(str, c))
    }

    @ParameterizedTest
    @CsvSource("  ,2,    ", "a,3,aaa", ",10,", "hello,0,", "bob,1,bob")
    fun test_repeat(str: String, count: Int, expected: String) {
        assertEquals(expected, repeat(str, count))
    }

    @ParameterizedTest
    @CsvSource(
        "abc\\\\def,3,true",
        "abc\\\\def,4,true",
        "abc\\\\def,2,false",
        "abc\\\\def,5,false",
        "abc\\def,3,true",
        "abc\\def,4,true"
    )
    fun test_isEscaped(str: String, index: Int, expected: Boolean) {
        assertEquals(expected, isEscaped(str, index))
    }

    @ParameterizedTest
    @CsvSource(
        "testing testing 123 )(bob) hello (second is ignored),(?=\\().+?(?<=\\)),(bob)",
        "we only want the number 12345 <--,\\d+,12345",
        "match first word,\\w+,match"
    )
    fun test_match(str: String, pattern: String, expected: String) {
        assertEquals(expected, match(pattern, str))
    }

    @ParameterizedTest
    @CsvSource(
        "&eBasic test,\u00A7eBasic test",
        "&f&lSome more,\u00A7f\u00A7lSome more",
        "\\&fake &oout,&fake \u00A7oout",
        "&#feab45Hex test,\u00A7x\u00A7f\u00A7e\u00A7a\u00A7b\u00A74\u00A75Hex test",
        "&#ABCDEFhEX AgAIN,\u00A7x\u00A7A\u00A7B\u00A7C\u00A7D\u00A7E\u00A7FhEX AgAIN",
        "&fone last &rtest &ocombining \\&ea few features&,\u00A7fone last \u00A7rtest \u00A7ocombining &ea few features&"
    )
    fun test_color(str: String, expected: String) {
        assertEquals(expected, colorBukkit(str))
    }

    @ParameterizedTest
    @CsvSource("1,1st", "2,2nd", "0,0th", "10,10th", "521,521st", "13,13th", "11,11th", "12,12th", "111,111th")
    fun test_ordinal(num: Int, expected: String) {
        assertEquals(expected, ordinal(num))
    }


    @ParameterizedTest
    @CsvSource(
        "camelCase, camel_case",
        "camelCaseWithNumber1, camel_case_with_number1",
        "camelCaseWithNumber1And2, camel_case_with_number1_and2",
        "UpperCamelCase, upper_camel_case",
    )
    fun camelToSnake(camel: String, snake: String) {
        assertEquals(snake, StringUtil.camelToSnake(camel))
    }

    @ParameterizedTest
    @CsvSource(
        "snake_case, Snake_Case",
        "snake_case_with_number1, Snake_Case_With_Number1",
        "ENUM_VALUE, Enum_Value",
    )
    fun snakeToUpperSnake(snake: String, upperSnake: String) {
        assertEquals(upperSnake, StringUtil.snakeToUpperSnake(snake))
    }

    @ParameterizedTest
    @CsvSource(
        "snake_case, Snake Case",
        "snake_case_with_number1, Snake Case With Number1",
        "ENUM_VALUE, Enum Value",
    )
    fun snakeToReadable(snake: String, readable: String) {
        assertEquals(readable, StringUtil.snakeToReadable(snake))
    }

    @ParameterizedTest
    @MethodSource("provide_splitCapitalLetters")
    fun test_splitCapitalLetters(input: String, expected: List<String>) {
        assertEquals(expected, splitCapitalLetters(input))
    }

    @ParameterizedTest
    @MethodSource("provide_splitAfterWord")
    fun test_splitAfterWord(input: String, expected: List<String>) {
        assertEquals(expected, splitAfterWord(input))
    }

    @ParameterizedTest
    @MethodSource("provide_split")
    fun test_split(input: String, expected: List<String>) {
        assertEquals(expected, split(input))
    }

    @ParameterizedTest
    @MethodSource("provide_didYouMean")
    fun test_didYouMean(input: String, options: List<String>, expected: String) {
        val actual = didYouMean(input, options)
        assertEquals(expected, actual)
    }

    companion object {
        @JvmStatic
        private fun provide_didYouMean(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("endermen", mutableListOf("cat", "dog", "enderman", "endermite", "ender", "man"), "enderman"),
                Arguments.of("dirt", mutableListOf("dirt", "dirty", "dirts", "trid", "treat", "cat", "dog"), "dirt"),
                Arguments.of(
                    "block_sand_break",
                    mutableListOf("sand_break", "block_snad_break", "sand", "block", "cat"),
                    "block_snad_break"
                )
            )
        }

        @JvmStatic
        private fun provide_split(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("1-2-3-4-5", listOf("1", "2", "3", "4", "5")),
                Arguments.of("-1--2--3--4-5", listOf("-1", "-2", "-3", "-4", "5")),
                Arguments.of("hello-world", listOf("hello", "world")),
                Arguments.of("parse~values~-4~from~-config", listOf("parse", "values", "-4", "from", "-config")),
                Arguments.of("Something 22 -634", listOf("Something", "22", "-634"))
            )
        }

        @JvmStatic
        private fun provide_splitAfterWord(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("So here's the thing", listOf("So", "here's", "the", "thing")),
                Arguments.of("One", listOf("One")),
                Arguments.of("Hello World", listOf("Hello", "World"))
            )
        }

        @JvmStatic
        private fun provide_splitCapitalLetters(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("MyPluginName", listOf("My", "Plugin", "Name")),
                Arguments.of("WeaponMechanics", listOf("Weapon", "Mechanics")),
                Arguments.of("Test", listOf("Test")),
                Arguments.of(
                    "HereIsAStringThatIsALittleBitLonger",
                    listOf("Here", "Is", "A", "String", "That", "Is", "A", "Little", "Bit", "Longer")
                )
            )
        }
    }
}