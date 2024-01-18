package me.deecaad.core.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class NumberUtilTest {

    @ParameterizedTest
    @CsvSource(
        "180, -180, 0",
        "0, 360, 0",
        "0, 180, -180",
        "0, 90, 90",
        "-180, 180, 0",
    )
    fun testDeltaDegrees(a: Float, b: Float, expected: Float) {
        assertEquals(expected, NumberUtil.deltaDegrees(a, b))
    }

    @Test
    fun testSmoothDamp() {
        // Move from 0.0 to 10.0. After enough iterations, we should be at 10.0
        // (any mathematician would cringe at that statement, but in the realm
        // of computer science, approaching a value with floating point numbers
        // means that we will actually reach that value quite quickly).
        var position = 0.0f
        val target = 10.0f
        val velocity = FloatRef(0.0f)

        for (i in 0..1000) {
            val result = NumberUtil.smoothDamp(position, target, velocity, 1f)
            println("Iteration $i: $result, which is a difference of ${result - position}. Velocity is ${velocity.value}")
            position = result
        }
        
        assertEquals(target, position, 0.001f)
    }

    @Test
    fun testSmoothDampDegrees() {
        var position = 0.0f
        val target = 350.0f
        val velocity = FloatRef(0.0f)

        for (i in 0..1000) {
            val result = NumberUtil.smoothDampDegrees(position, target, velocity, 1f)
            println("Iteration $i: $result, which is a difference of ${result - position}. Velocity is ${velocity.value}")
            position = result
        }

        assertEquals(target, position, 0.001f)
    }
}