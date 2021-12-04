package me.deecaad.core.utils;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class VectorUtilTest {

    @ParameterizedTest
    @CsvSource({"361,1", "-1,359", "180,180", "450,90", "360,0", "0,0"})
    void test_normalizeAngle(double input, double expected) {
        assertEquals(expected, VectorUtil.normalize(input));
    }

    private static Stream<Arguments> provide_normalizeRadians() {
        return Stream.of(
                Arguments.of(2 * Math.PI, 0.0),
                Arguments.of(0.0, 0.0),
                Arguments.of(Math.PI, Math.PI),
                Arguments.of(-Math.PI, Math.PI),
                Arguments.of(Math.PI / 4.0, Math.PI / 4.0)

        );
    }

    @ParameterizedTest
    @MethodSource("provide_normalizeRadians")
    void test_normalizeRadians(double input, double expected) {
        assertEquals(expected, VectorUtil.normalizeRadians(input));
    }

    private static Stream<Arguments> provide_lerp() {
        Vector one = new Vector(1.0, 1.0, 1.0);
        Vector zero = new Vector(0.0, 0.0, 0.0);

        return Stream.of(
                Arguments.of(zero.clone(), one.clone(), 0.5, new Vector(0.5, 0.5, 0.5)),
                Arguments.of(zero.clone(), one.clone(), 0.25, new Vector(0.25, 0.25, 0.25)),
                Arguments.of(zero.clone(), one.clone(), 0.0, new Vector(0.0, 0.0, 0.0)),
                Arguments.of(zero.clone(), one.clone(), 1.0, new Vector(1.0, 1.0, 1.0))
        );
    }

    @ParameterizedTest
    @MethodSource("provide_lerp")
    void test_lerp(Vector min, Vector max, double factor, Vector expected) {
        Vector result = VectorUtil.lerp(min, max, factor);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provide_min() {
        return Stream.of(
                Arguments.of(new Vector(30, 40, 50), new Vector(20, 70, 50), new Vector(20, 40, 50)),
                Arguments.of(new Vector(-5, -3, 70), new Vector(10, -5, 0), new Vector(-5, -5, 00))
        );
    }

    @ParameterizedTest
    @MethodSource("provide_min")
    void test_min(Vector a, Vector b, Vector expected) {
        Vector result = VectorUtil.min(a, b);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({"1,1,1", "32,45,10", "-24,101,0", "0,1,0"})
    void test_perpendicular(int x, int y, int z) {
        // The dot product between 2 perpendicular vectors should be 0.
        Vector a = new Vector(x, y, z);
        Vector b = VectorUtil.getPerpendicular(a);
        assertEquals(0.0, a.dot(b));
    }

    @Test
    void when_vectorEmpty_expect_exception_test_perpendicular() {
        assertThrows(IllegalArgumentException.class, () -> VectorUtil.getPerpendicular(new Vector()));
    }

    private static Stream<Arguments> provide_angleBetween() {
        return Stream.of(
                Arguments.of(new Vector(1, 0, 0), new Vector(1, 1, 0), Math.PI / 4.0),
                Arguments.of(new Vector(1, 0, 0), new Vector(0, 1, 0), Math.PI / 2.0),
                Arguments.of(new Vector(1, 1, 1), new Vector(1, 1, 1), 0.0),
                Arguments.of(new Vector(1, 0, 0), new Vector(-1, 0, 0), Math.PI)
        );
    }

    @ParameterizedTest
    @MethodSource("provide_angleBetween")
    void test_angleBetween(Vector a, Vector b, double expected) {
        double value = VectorUtil.getAngleBetween(a, b);
        assertEquals(expected, value, 1e-6);
    }

    private static Stream<Arguments> provide_max() {
        return Stream.of(
                Arguments.of(new Vector(30, 40, 50), new Vector(20, 70, 50), new Vector(30, 70, 50)),
                Arguments.of(new Vector(-5, -3, 70), new Vector(10, -5, 0), new Vector(10, -3, 70))
        );
    }

    @ParameterizedTest
    @MethodSource("provide_max")
    void test_max(Vector a, Vector b, Vector expected) {
        Vector result = VectorUtil.max(a, b);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({"0,0,0,true", "1,-1,0,false", "1,1,1,false"})
    void test_isEmpty(int x, int y, int z, boolean expected) {
        Vector vector = new Vector(x, y, z);
        assertEquals(expected, VectorUtil.isEmpty(vector));
    }
}