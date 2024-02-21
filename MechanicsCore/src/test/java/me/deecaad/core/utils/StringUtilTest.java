package me.deecaad.core.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilTest {

    @ParameterizedTest
    @CsvSource({"bob,b,2", "deecaad,d,2", "cjcrafter,z,0", "darkman,k,1", "mississippi,i,4"})
    void test_countChars(String str, char c, int expected) {
        assertEquals(expected, StringUtil.countOccurrences(str, c));
    }

    @ParameterizedTest
    @CsvSource({"  ,2,    ", "a,3,aaa", ",10,", "hello,0,", "bob,1,bob"})
    void test_repeat(String str, int count, String expected) {
        if (str == null)
            str = "";
        if (expected == null)
            expected = "";

        assertEquals(expected, StringUtil.repeat(str, count));
    }

    @ParameterizedTest
    @CsvSource({"abc\\\\def,3,true", "abc\\\\def,4,true", "abc\\\\def,2,false", "abc\\\\def,5,false", "abc\\def,3,true", "abc\\def,4,true"})
    void test_isEscaped(String str, int index, boolean expected) {
        assertEquals(expected, StringUtil.isEscaped(str, index));
    }

    @ParameterizedTest
    @CsvSource({
            "testing testing 123 )(bob) hello (second is ignored),(?=\\().+?(?<=\\)),(bob)",
            "we only want the number 12345 <--,\\d+,12345",
            "match first word,\\w+,match"
    })
    void test_match(String str, String pattern, String expected) {
        assertEquals(expected, StringUtil.match(pattern, str));
    }

    @ParameterizedTest
    @CsvSource({
            "&eBasic test,\u00A7eBasic test",
            "&f&lSome more,\u00A7f\u00A7lSome more",
            "\\&fake &oout,&fake \u00A7oout",
            "&#feab45Hex test,\u00A7x\u00A7f\u00A7e\u00A7a\u00A7b\u00A74\u00A75Hex test",
            "&#ABCDEFhEX AgAIN,\u00A7x\u00A7A\u00A7B\u00A7C\u00A7D\u00A7E\u00A7FhEX AgAIN",
            "&fone last &rtest &ocombining \\&ea few features&,\u00A7fone last \u00A7rtest \u00A7ocombining &ea few features&"
    })
    void test_color(String str, String expected) {
        assertEquals(expected, StringUtil.colorBukkit(str));
    }

    @ParameterizedTest
    @CsvSource({"1,1st", "2,2nd", "0,0th", "10,10th", "521,521st", "13,13th", "11,11th", "12,12th", "111,111th"})
    void test_ordinal(int num, String expected) {
        assertEquals(expected, StringUtil.ordinal(num));
    }

    private static Stream<Arguments> provide_splitCapitalLetters() {
        return Stream.of(
            Arguments.of("MyPluginName", List.of("My", "Plugin", "Name")),
            Arguments.of("WeaponMechanics", List.of("Weapon", "Mechanics")),
            Arguments.of("Test", List.of("Test")),
            Arguments.of("HereIsAStringThatIsALittleBitLonger", List.of("Here", "Is", "A", "String", "That", "Is", "A", "Little", "Bit", "Longer")));
    }

    @ParameterizedTest
    @MethodSource("provide_splitCapitalLetters")
    void test_splitCapitalLetters(String input, List<String> expected) {
        assertEquals(expected, StringUtil.splitCapitalLetters(input));
    }

    private static Stream<Arguments> provide_splitAfterWord() {
        return Stream.of(
            Arguments.of("So here's the thing", List.of("So", "here's", "the", "thing")),
            Arguments.of("One", List.of("One")),
            Arguments.of("Hello World", List.of("Hello", "World")));
    }

    @ParameterizedTest
    @MethodSource("provide_splitAfterWord")
    void test_splitAfterWord(String input, List<String> expected) {
        assertEquals(expected, StringUtil.splitAfterWord(input));
    }

    private static Stream<Arguments> provide_split() {
        return Stream.of(
            Arguments.of("1-2-3-4-5", List.of("1", "2", "3", "4", "5")),
            Arguments.of("-1--2--3--4-5", List.of("-1", "-2", "-3", "-4", "5")),
            Arguments.of("hello-world", List.of("hello", "world")),
            Arguments.of("parse~values~-4~from~-config", List.of("parse", "values", "-4", "from", "-config")),
            Arguments.of("Something 22 -634", List.of("Something", "22", "-634")));
    }

    @ParameterizedTest
    @MethodSource("provide_split")
    void test_split(String input, List<String> expected) {
        assertEquals(expected, StringUtil.split(input));
    }

    private static Stream<Arguments> provide_didYouMean() {
        return Stream.of(
            Arguments.of("endermen", Arrays.asList("cat", "dog", "enderman", "endermite", "ender", "man"), "enderman"),
            Arguments.of("dirt", Arrays.asList("dirt", "dirty", "dirts", "trid", "treat", "cat", "dog"), "dirt"),
            Arguments.of("block_sand_break", Arrays.asList("sand_break", "block_snad_break", "sand", "block", "cat"), "block_snad_break"));
    }

    @ParameterizedTest
    @MethodSource("provide_didYouMean")
    void test_didYouMean(String input, List<String> options, String expected) {
        String actual = StringUtil.didYouMean(input, options);
        assertEquals(expected, actual);
    }
}