package me.deecaad.core.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class NumberUtilTest {

    // * ------------------------------ * //
    // *       Randomness Tests         * //
    // * ------------------------------ * //

    // ! Testing randomness is not a standard thing to do.
    // ! We can roughly test the behavior of the method, but we cannot
    // ! ensure that the results are random.

    public static int TEST_ITERATIONS = 50;

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 10, 100, 21})
    void test_randomBound(int bound) {
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            int random = NumberUtil.random(bound);
            assertTrue(random >= 0);
            assertTrue(random < bound);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1000, -378, -2})
    void when_testRandomBoundNegative_expect_exception(int bound) {
        assertThrows(IllegalArgumentException.class, () -> NumberUtil.random(bound));
    }

    @Test
    void test_randomArrayElement() {
        Integer[] arr = new Integer[]{ 10, 32, -10, -1, 0, 4, 100, 3, -203 };

        // Test 10 times that the returned random element is contained in
        // the array.
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            int random = NumberUtil.random(arr);

            boolean fail = true;
            for (int element : arr) {
                if (random == element) {
                    fail = false;
                    break;
                }
            }

            if (fail)
                fail();
        }
    }

    @Test
    void test_randomListElement() {
        List<Integer> list = Arrays.asList(-20, 0, 302, 58, -20, -102, 1000);

        // Test 10 times that the returned random element is contained in
        // the list.
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            int random = NumberUtil.random(list);

            if (!list.contains(random))
                fail();
        }
    }

    @ParameterizedTest
    @CsvSource({"1,1", "1,3", "10,12", "-10,10", "200,210", "0,25"})
    void test_randomRange(int min, int max) {
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            int random = NumberUtil.random(min, max);

            if (random < min || random > max)
                fail();
        }
    }

    @ParameterizedTest
    @CsvSource({"3,1", "-30,-40", "0,-5", "10,0"})
    void when_minGreaterThenMax_expect_exception(int min, int max) {
        assertThrows(IllegalArgumentException.class, () -> NumberUtil.random(min, max));
    }

    @ParameterizedTest
    @CsvSource({"1.0,4.5", "5.0,5.0", "9.2,12.45", "-10.1,10.1", "200.0,210.0", "0.24,22.5"})
    void test_randomRange(double min, double max) {
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            double random = NumberUtil.random(min, max);

            if (random < min || random > max)
                fail();
        }
    }

    @ParameterizedTest
    @CsvSource({"3.2,1.3", "-30.12,-40.1", "0.29,-5.32", "10.9,0.0"})
    void when_minGreaterThenMax_expect_exception(double min, double max) {
        assertThrows(IllegalArgumentException.class, () -> NumberUtil.random(min, max));
    }

    @ParameterizedTest
    @CsvSource({"1.0,true", "0.0,false"})
    void test_chance(double chance, boolean expected) {
        // We cannot test any more than these 2 test cases (1 and 0)
        assertEquals(expected, NumberUtil.chance(chance));
    }

    // * ------------------------------ * //
    // *    End of Randomness Tests     * //
    // * ------------------------------ * //

    private static Stream<Arguments> provide_minMax() {
        return Stream.of(
                Arguments.of(0, 1, 2, 1),
                Arguments.of(-5, 0, 5, 0),
                Arguments.of(0, 10, 5, 5),
                Arguments.of(0, -5, 10, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("provide_minMax")
    void test_minMax(int min, int value, int max, int expected) {
        int actual = NumberUtil.minMax(min, value, max);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"0.0,0.0,true", "1.00000001,1,true", "9.999999999,10,true", "5,10,false", "10,10.1,false"})
    void test_equals(double a, double b, boolean expected) {
        boolean actual = NumberUtil.equals(a, b);
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> provide_lerp() {
        return Stream.of(
                Arguments.of(0.0, 10.0, 0.5, 5.0),
                Arguments.of(-10.0, 10.0, 0.5, 0.0),
                Arguments.of(0.0, 100.0, 0.25, 25.0),
                Arguments.of(0.0, 100.0, 0.66, 66.0),
                Arguments.of(0.0, 100.0, 1.0, 100.0)
        );
    }

    @ParameterizedTest
    @MethodSource("provide_lerp")
    void test_lerp(double min, double max, double factor, double expected) {
        double actual = NumberUtil.lerp(min, max, factor);
        assertEquals(expected, actual, 1e-6);
    }

    @ParameterizedTest
    @CsvSource({"10,X", "6,VI", "4,IV", "0,nulla", "1,I", "8765,MMMMMMMMDCCLXV", "101,CI"})
    void test_toRomanNumeral(int from, String expected) {
        String actual = NumberUtil.toRomanNumeral(from);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"1,1s", "60,1m", "3600,1h", "86400,1d", "31536000,1y","61,1m 1s","172925,2d 2m 5s"})
    void test_toTime(int from, String expected) {
        String actual = NumberUtil.toTime(from);
        assertEquals(expected, actual);
    }
}