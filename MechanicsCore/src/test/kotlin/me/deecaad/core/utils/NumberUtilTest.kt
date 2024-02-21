package me.deecaad.core.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.concurrent.ThreadLocalRandom

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NumberUtilTest {
    @ParameterizedTest
    @CsvSource(
        "4.0, 16.0",
        "-4.0, 16.0",
        "0.0, 0.0",
        "1.0, 1.0",
        "0.5, 0.25",
        "10000,100000000",
    )
    fun testSquare(
        value: Double,
        expected: Double,
    ) {
        assertEquals(expected, NumberUtil.square(value))
    }

    @Test
    fun `square returns positive number`() {
        val attempts = 1000
        val random = ThreadLocalRandom.current()

        for (i in 0 until attempts) {
            val number = random.nextDouble(-10000.0, 10000.0)
            val square = NumberUtil.square(number)

            assertTrue(square >= 0.0)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "10.0, 0.0, 1.0, 1.0",
        "0.5, 0.0, 1.0, 0.5",
        "0.0, 0.0, 1.0, 0.0",
        "1.0, 0.0, 1.0, 1.0",
    )
    fun testClamp(
        value: Double,
        min: Double,
        max: Double,
        expected: Double,
    ) {
        assertEquals(expected, value.clamp(min, max))
    }

    @ParameterizedTest
    @CsvSource(
        "10.0, 1.0",
        "0.5, 0.5",
        "0.0, 0.0",
        "1.0, 1.0",
        "-1.0, 0.0",
        "2.0, 1.0",
    )
    fun testClamp01(
        value: Double,
        expected: Double,
    ) {
        assertEquals(expected, value.clamp01())
    }

    @ParameterizedTest
    @CsvSource(
        "180, -180, 0",
        "0, 360, 0",
        "0, 180, 180",
        "0, 90, 90",
        "-180, 180, 0",
        "0, -45, -45",
    )
    fun testDeltaDegrees(
        a: Float,
        b: Float,
        expected: Float,
    ) {
        assertEquals(expected, NumberUtil.deltaDegrees(a, b))
    }

    @ParameterizedTest
    @CsvSource(
        "361, 1",
        "-1, -1",
        "180, 180",
        "450, 90",
        "360, 0",
        "0, 0",
    )
    fun test_normalizeAngle(
        input: Double,
        expected: Double,
    ) {
        assertEquals(expected, NumberUtil.normalizeDegrees(input))
    }

    @ParameterizedTest
    @CsvSource(
        "${2 * Math.PI}, 0.0",
        "0.0, 0.0",
        "${Math.PI}, ${Math.PI}",
        "${-Math.PI}, ${Math.PI}",
        "${Math.PI / 4.0}, ${Math.PI / 4.0}",
    )
    fun test_normalizeRadians(
        input: Double,
        expected: Double,
    ) {
        assertEquals(expected, NumberUtil.normalizeRadians(input))
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

    @ParameterizedTest
    @CsvSource(
        "-1, nulla",
        "0, nulla",
        "1, I",
        "2, II",
        "3, III",
        "4, IV",
        "5, V",
        "6, VI",
        "7, VII",
        "8, VIII",
        "9, IX",
        "10, X",
        "513, DXIII",
    )
    fun testRomanNumeral(
        value: Int,
        expected: String,
    ) {
        assertEquals(expected, NumberUtil.toRomanNumeral(value))
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0s",
        "1, 1s",
        "60, 1m",
        "61, 1m 1s",
        "3600, 1h",
        "3661, 1h 1m 1s",
    )
    fun testToTime(
        seconds: Int,
        expected: String,
    ) {
        assertEquals(expected, NumberUtil.toTime(seconds))
    }
}
