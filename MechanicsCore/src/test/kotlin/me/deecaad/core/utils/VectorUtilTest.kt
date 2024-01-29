package me.deecaad.core.utils

import me.deecaad.core.utils.VectorUtil.getAngleBetween
import me.deecaad.core.utils.VectorUtil.getPerpendicular
import me.deecaad.core.utils.VectorUtil.isZero
import me.deecaad.core.utils.VectorUtil.lerp
import me.deecaad.core.utils.VectorUtil.max
import me.deecaad.core.utils.VectorUtil.min
import org.bukkit.util.Vector
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class VectorUtilTest {

    @ParameterizedTest
    @MethodSource("provide_lerp")
    fun test_lerp(min: Vector?, max: Vector?, factor: Double, expected: Vector?) {
        val result = lerp(min!!, max!!, factor)
        Assertions.assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("provide_min")
    fun test_min(a: Vector?, b: Vector?, expected: Vector?) {
        val result = min(a!!, b!!)
        Assertions.assertEquals(expected, result)
    }

    @ParameterizedTest
    @CsvSource("1,1,1", "32,45,10", "-24,101,0", "0,1,0")
    fun test_perpendicular(x: Int, y: Int, z: Int) {
        // The dot product between 2 perpendicular vectors should be 0.
        val a = Vector(x, y, z)
        val b = getPerpendicular(a)
        Assertions.assertEquals(0.0, a.dot(b))
    }

    @Test
    fun when_vectorEmpty_expect_exception_test_perpendicular() {
        Assertions.assertThrows(IllegalArgumentException::class.java) { getPerpendicular(Vector()) }
    }

    @ParameterizedTest
    @MethodSource("provide_angleBetween")
    fun test_angleBetween(a: Vector?, b: Vector?, expected: Double) {
        val value = getAngleBetween(a!!, b!!)
        Assertions.assertEquals(expected, value, 1e-6)
    }

    @ParameterizedTest
    @MethodSource("provide_max")
    fun test_max(a: Vector?, b: Vector?, expected: Vector?) {
        val result = max(a!!, b!!)
        Assertions.assertEquals(expected, result)
    }

    @ParameterizedTest
    @CsvSource("0,0,0,true", "1,-1,0,false", "1,1,1,false")
    fun test_isEmpty(x: Int, y: Int, z: Int, expected: Boolean) {
        val vector = Vector(x, y, z)
        Assertions.assertEquals(expected, isZero(vector))
    }

    companion object {
        @JvmStatic
        private fun provide_lerp(): Stream<Arguments> {
            val one = Vector(1.0, 1.0, 1.0)
            val zero = Vector(0.0, 0.0, 0.0)
            return Stream.of(
                Arguments.of(zero.clone(), one.clone(), 0.5, Vector(0.5, 0.5, 0.5)),
                Arguments.of(zero.clone(), one.clone(), 0.25, Vector(0.25, 0.25, 0.25)),
                Arguments.of(zero.clone(), one.clone(), 0.0, Vector(0.0, 0.0, 0.0)),
                Arguments.of(zero.clone(), one.clone(), 1.0, Vector(1.0, 1.0, 1.0))
            )
        }

        @JvmStatic
        private fun provide_min(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Vector(30, 40, 50), Vector(20, 70, 50), Vector(20, 40, 50)),
                Arguments.of(Vector(-5, -3, 70), Vector(10, -5, 0), Vector(-5, -5, 0))
            )
        }

        @JvmStatic
        private fun provide_angleBetween(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Vector(1, 0, 0), Vector(1, 1, 0), Math.PI / 4.0),
                Arguments.of(Vector(1, 0, 0), Vector(0, 1, 0), Math.PI / 2.0),
                Arguments.of(Vector(1, 1, 1), Vector(1, 1, 1), 0.0),
                Arguments.of(Vector(1, 0, 0), Vector(-1, 0, 0), Math.PI)
            )
        }

        @JvmStatic
        private fun provide_max(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Vector(30, 40, 50), Vector(20, 70, 50), Vector(30, 70, 50)),
                Arguments.of(Vector(-5, -3, 70), Vector(10, -5, 0), Vector(10, -3, 70))
            )
        }
    }
}