package me.deecaad.core.utils

import java.util.*
import kotlin.math.abs
import kotlin.math.floor

/**
 * A collection of utility functions for math, especially for random number
 * generation, interpolation, and angle calculations.
 *
 * This class is inspired by Unity's Mathf class, but is not a direct port.
 */
object NumberUtil {

    /**
     * A small value used to compare floating point numbers.
     */
    const val EPSILON: Float = 1e-6f

    /**
     * A small value used to compare floating point numbers.
     */
    const val EPSILON_DOUBLE: Double = 1e-6

    /**
     * The value of PI.
     */
    const val PI: Float = Math.PI.toFloat()

    /**
     * The value of PI.
     */
    const val PI_DOUBLE: Double = Math.PI

    /**
     * The value of PI / 2.
     */
    const val HALF_PI: Float = PI / 2

    /**
     * The value of PI / 2.
     */
    const val HALF_PI_DOUBLE: Double = PI_DOUBLE / 2

    /**
     * The value of 2 PIs.
     */
    const val TAU: Float = PI * 2

    /**
     * The value of 2 PIs.
     */
    const val TAU_DOUBLE: Double = PI_DOUBLE * 2

    // Internals
    private val NUMERALS: TreeMap<Int, String> = TreeMap()
    private val TIME: TreeMap<Int, String> = TreeMap()

    init {
        NUMERALS[1000] = "M"
        NUMERALS[900] = "CM"
        NUMERALS[500] = "D"
        NUMERALS[400] = "CD"
        NUMERALS[100] = "C"
        NUMERALS[90] = "XC"
        NUMERALS[50] = "L"
        NUMERALS[40] = "XL"
        NUMERALS[10] = "X"
        NUMERALS[9] = "IX"
        NUMERALS[5] = "V"
        NUMERALS[4] = "IV"
        NUMERALS[1] = "I"

        TIME[31536000] = "y"
        TIME[86400] = "d"
        TIME[3600] = "h"
        TIME[60] = "m"
        TIME[1] = "s"
    }

    /**
     * Squares a value, i.e. multiplies it by itself.
     *
     * @param value The value to square
     * @return The squared value
     */
    @JvmStatic
    fun square(value: Int): Int {
        return value * value
    }

    /**
     * Squares a value, i.e. multiplies it by itself.
     *
     * @param value The value to square
     * @return The squared value
     */
    @JvmStatic
    fun square(value: Float): Float {
        return value * value
    }

    /**
     * Squares a value, i.e. multiplies it by itself.
     *
     * @param value The value to square
     * @return The squared value
     */
    @JvmStatic
    fun square(value: Double): Double {
        return value * value
    }

    /**
     * Squares a value, i.e. multiplies it by itself.
     *
     * @param value The value to square
     * @return The squared value
     */
    @JvmStatic
    fun square(value: Long): Long {
        return value * value
    }

    /**
     * Clamps a value between a minimum and maximum value.
     *
     * @param value The value to clamp
     * @param min The minimum value
     * @param max The maximum value
     * @return The clamped value
     * @throws IllegalArgumentException if `min` is greater than `max`
     */
    @JvmStatic
    fun clamp(value: Int, min: Int, max: Int): Int {
        require(min <= max) { "min must be less than or equal to max" }
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }

    /**
     * Clamps a value between a minimum and maximum value.
     *
     * @param value The value to clamp
     * @param min The minimum value
     * @param max The maximum value
     * @return The clamped value
     * @throws IllegalArgumentException if `min` is greater than `max`
     */
    @JvmStatic
    fun clamp(value: Float, min: Float, max: Float): Float {
        require(min <= max) { "min must be less than or equal to max" }
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }

    /**
     * Clamps a value between a minimum and maximum value.
     *
     * @param value The value to clamp
     * @param min The minimum value
     * @param max The maximum value
     * @return The clamped value
     * @throws IllegalArgumentException if `min` is greater than `max`
     */
    @JvmStatic
    fun clamp(value: Double, min: Double, max: Double): Double {
        require(min <= max) { "min must be less than or equal to max" }
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }

    /**
     * Clamps a value between a minimum and maximum value.
     *
     * @param value The value to clamp
     * @param min The minimum value
     * @param max The maximum value
     * @return The clamped value
     * @throws IllegalArgumentException if `min` is greater than `max`
     */
    @JvmStatic
    fun clamp(value: Long, min: Long, max: Long): Long {
        require(min <= max) { "min must be less than or equal to max" }
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }

    /**
     * Clamps a value between 0 and 1.
     *
     * @param value The value to clamp
     * @return The clamped value
     */
    @JvmStatic
    fun clamp01(value: Float): Float {
        return clamp(value, 0.0f, 1.0f)
    }

    /**
     * Clamps a value between 0 and 1.
     *
     * @param value The value to clamp
     * @return The clamped value
     */
    @JvmStatic
    fun clamp01(value: Double): Double {
        return clamp(value, 0.0, 1.0)
    }

    /**
     * Checks if two floating point numbers are approximately equal.
     *
     * @param a The first number
     * @param b The second number
     * @return True if the numbers are approximately equal
     */
    @JvmStatic
    @JvmOverloads
    fun approximately(a: Float, b: Float, epsilon: Float = EPSILON): Boolean {
        return abs(a - b) < epsilon
    }

    /**
     * Checks if two floating point numbers are approximately equal.
     *
     * @param a The first number
     * @param b The second number
     * @return True if the numbers are approximately equal
     */
    @JvmStatic
    @JvmOverloads
    fun approximately(a: Double, b: Double, epsilon: Double = EPSILON_DOUBLE): Boolean {
        return abs(a - b) < epsilon
    }

    /**
     * Linearly interpolates between two values.
     *
     * @param a The first value
     * @param b The second value
     * @param t The interpolation value, clamped between 0 and 1
     * @return The interpolated value
     */
    @JvmStatic
    fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * clamp01(t)
    }

    /**
     * Linearly interpolates between two values.
     *
     * @param a The first value
     * @param b The second value
     * @param t The interpolation value, clamped between 0 and 1
     * @return The interpolated value
     */
    @JvmStatic
    fun lerp(a: Double, b: Double, t: Double): Double {
        return a + (b - a) * clamp01(t)
    }

    /**
     * Linearly interpolates between two values.
     *
     * If the interpolation value is outside the range [0, 1], the
     * returned value will be outside the range [a, b]. Typically,
     * you'll want to use [lerp] instead.
     *
     * @param a The first value
     * @param b The second value
     * @param t The interpolation value.
     * @return The interpolated value
     */
    @JvmStatic
    fun lerpUnclamped(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }

    /**
     * Linearly interpolates between two values.
     *
     * If the interpolation value is outside the range [0, 1], the
     * returned value will be outside the range [a, b]. Typically,
     * you'll want to use [lerp] instead.
     *
     * @param a The first value
     * @param b The second value
     * @param t The interpolation value.
     * @return The interpolated value
     */
    @JvmStatic
    fun lerpUnclamped(a: Double, b: Double, t: Double): Double {
        return a + (b - a) * t
    }

    /**
     * Linearly interpolates between two angles, in degrees.
     *
     * This is the same as [lerp], but makes sure the values interpolate
     * correctly when they wrap around 360 degrees. This method returns
     * the shortest path between the specified angles. This method wraps
     * around values that are outside the range [-180, 180]. For example,
     * ```kotlin
     * lerpDegrees(1.0f, 190f, 1.0f) // returns -170.0f
     * ```
     */
    @JvmStatic
    fun lerpDegrees(a: Float, b: Float, t: Float): Float {
        val delta = (b - a) % 360.0f
        return (a + delta * clamp01(t)) % 360.0f
    }

    /**
     * Linearly interpolates between two angles.
     *
     * This is the same as [lerp], but makes sure the values interpolate
     * correctly when they wrap around 360 degrees. This method returns
     * the shortest path between the specified angles. This method wraps
     * around values that are outside the range [-180, 180]. For example,
     * ```kotlin
     * lerpDegrees(1.0, 190.0, 1.0) // returns -170.0
     * ```
     */
    @JvmStatic
    fun lerpDegrees(a: Double, b: Double, t: Double): Double {
        val delta = (b - a) % 360.0
        return (a + delta * clamp01(t)) % 360.0
    }

    /**
     * Linearly interpolates between two angles, in radians.
     *
     * This is the same as [lerp], but makes sure the values interpolate
     * correctly when they wrap around 2 PI. This method returns
     * the shortest path between the specified angles. This method wraps
     * around values that are outside the range [-PI, PI].
     */
    @JvmStatic
    fun lerpRadians(a: Float, b: Float, t: Float): Float {
        val delta = (b - a) % TAU
        return (a + delta * clamp01(t)) % TAU
    }

    /**
     * Linearly interpolates between two angles, in radians.
     *
     * This is the same as [lerp], but makes sure the values interpolate
     * correctly when they wrap around 2 PI. This method returns
     * the shortest path between the specified angles. This method wraps
     * around values that are outside the range [-PI, PI].
     */
    @JvmStatic
    fun lerpRadians(a: Double, b: Double, t: Double): Double {
        val delta = (b - a) % TAU_DOUBLE
        return (a + delta * clamp01(t)) % TAU_DOUBLE
    }

    /**
     * The inverse of [lerp]. Given a value between `a` and `b`, this method
     * returns the interpolation value `t` between 0 and 1.
     *
     * @param a The first value
     * @param b The second value
     * @param value The interpolated value
     * @return The interpolation value `t`
     * @throws IllegalArgumentException if `a` and `b` are equal
     */
    @JvmStatic
    fun inverseLerp(a: Float, b: Float, value: Float): Float {
        require(a != b) { "a and b cannot be equal" }
        return clamp01((value - a) / (b - a))
    }

    /**
     * The inverse of [lerp]. Given a value between `a` and `b`, this method
     * returns the interpolation value `t` between 0 and 1.
     *
     * @param a The first value
     * @param b The second value
     * @param value The interpolated value
     * @return The interpolation value `t`
     * @throws IllegalArgumentException if `a` and `b` are equal
     */
    @JvmStatic
    fun inverseLerp(a: Double, b: Double, value: Double): Double {
        require(a != b) { "a and b cannot be equal" }
        return clamp01((value - a) / (b - a))
    }

    /**
     * The inverse of [lerpUnclamped]. Given a value between `a` and `b`, this method
     * returns the interpolation value `t`.
     *
     * @param a The first value
     * @param b The second value
     * @param value The interpolated value
     * @return The interpolation value `t`
     * @throws IllegalArgumentException if `a` and `b` are equal
     */
    @JvmStatic
    fun inverseLerpUnclamped(a: Float, b: Float, value: Float): Float {
        require(a != b) { "a and b cannot be equal" }
        return (value - a) / (b - a)
    }

    /**
     * The inverse of [lerpUnclamped]. Given a value between `a` and `b`, this method
     * returns the interpolation value `t`.
     *
     * @param a The first value
     * @param b The second value
     * @param value The interpolated value
     * @return The interpolation value `t`
     * @throws IllegalArgumentException if `a` and `b` are equal
     */
    @JvmStatic
    fun inverseLerpUnclamped(a: Double, b: Double, value: Double): Double {
        require(a != b) { "a and b cannot be equal" }
        return (value - a) / (b - a)
    }

    /**
     * Remaps the value from one range to another.
     *
     * For example, if you have a number, say 0.5, in the range [0.0, 1.0], and
     * you want to remap it to the range [0, 100], you can use this method:
     * ```kotlin
     * NumberUtil.remap(0.5f, 0.0f, 1.0f, 0.0f, 100.0f) // returns 50.0f
     * ```
     *
     * @param value The value to remap
     * @param from1 The lower bound of the first range
     * @param to1 The upper bound of the first range
     * @param from2 The lower bound of the second range
     * @param to2 The upper bound of the second range
     * @return The remapped value
     */
    @JvmStatic
    fun remap(value: Float, from1: Float, to1: Float, from2: Float, to2: Float): Float {
        return lerpUnclamped(from2, to2, inverseLerpUnclamped(from1, to1, value))
    }

    /**
     * Remaps the value from one range to another.
     *
     * For example, if you have a number, say 0.5, in the range [0.0, 1.0], and
     * you want to remap it to the range [0, 100], you can use this method:
     * ```kotlin
     * NumberUtil.remap(0.5, 0.0, 1.0, 0.0, 100.0) // returns 50.0
     * ```
     *
     * @param value The value to remap
     * @param from1 The lower bound of the first range
     * @param to1 The upper bound of the first range
     * @param from2 The lower bound of the second range
     * @param to2 The upper bound of the second range
     * @return The remapped value
     */
    @JvmStatic
    fun remap(value: Double, from1: Double, to1: Double, from2: Double, to2: Double): Double {
        return lerpUnclamped(from2, to2, inverseLerpUnclamped(from1, to1, value))
    }

    /**
     * Normalizes an angle in degrees to the range (-180, 180].
     *
     * @param angle The angle to normalize
     * @return The normalized angle
     */
    @JvmStatic
    fun normalizeDegrees(angle: Float): Float {
        val normalized = angle - 360f * floor((angle + 180f) / 360f)

        // The equation above is very fast, but might output -180, when we want 180.
        return if (normalized == -180f) 180f else normalized
    }

    /**
     * Normalizes an angle in degrees to the range (-180, 180].
     *
     * @param angle The angle to normalize
     * @return The normalized angle
     */
    @JvmStatic
    fun normalizeDegrees(angle: Double): Double {
        val normalized = angle - 360.0 * floor((angle + 180.0) / 360.0)

        // The equation above is very fast, but might output -180, when we want 180.
        return if (normalized == -180.0) 180.0 else normalized
    }

    /**
     * Normalizes an angle in radians to the range (-PI, PI].
     *
     * @param angle The angle to normalize
     * @return The normalized angle
     */
    @JvmStatic
    fun normalizeRadians(angle: Float): Float {
        val normalized = angle - TAU * floor((angle + PI) / TAU)

        // The equation above is very fast, but might output -PI, when we want PI.
        return if (normalized == -PI) PI else normalized
    }

    /**
     * Normalizes an angle in radians to the range (-PI, PI].
     *
     * @param angle The angle to normalize
     * @return The normalized angle
     */
    @JvmStatic
    fun normalizeRadians(angle: Double): Double {
        val normalized = angle - TAU_DOUBLE * floor((angle + PI_DOUBLE) / TAU_DOUBLE)

        // The equation above is very fast, but might output -PI, when we want PI.
        return if (normalized == -PI_DOUBLE) PI_DOUBLE else normalized
    }

    /**
     * Returns the smallest angle between two angles, in degrees.
     *
     * @param a The first angle
     * @param b The second angle
     * @return The smallest angle between the two angles
     */
    @JvmStatic
    fun deltaDegrees(a: Float, b: Float): Float {
        return normalizeDegrees(b - a)
    }

    /**
     * Returns the smallest angle between two angles, in degrees.
     *
     * @param a The first angle
     * @param b The second angle
     * @return The smallest angle between the two angles
     */
    @JvmStatic
    fun deltaDegrees(a: Double, b: Double): Double {
        return normalizeDegrees(b - a)
    }

    /**
     * Returns the smallest angle between two angles, in radians.
     *
     * @param a The first angle
     * @param b The second angle
     * @return The smallest angle between the two angles
     */
    @JvmStatic
    fun deltaRadians(a: Float, b: Float): Float {
        return normalizeRadians(b - a)
    }

    /**
     * Returns the smallest angle between two angles, in radians.
     *
     * @param a The first angle
     * @param b The second angle
     * @return The smallest angle between the two angles
     */
    @JvmStatic
    fun deltaRadians(a: Double, b: Double): Double {
        return normalizeRadians(b - a)
    }

    /**
     * Moves a value `current` towards `target`.
     *
     * @param current The current value
     * @param target The target value
     * @param maxDelta The maximum change that should be applied
     * @return The new value
     * @throws IllegalArgumentException if `maxDelta` is negative
     */
    @JvmStatic
    fun moveTowards(current: Float, target: Float, maxDelta: Float): Float {
        require(maxDelta > 0.0f) { "maxDelta must be positive" }

        return if (abs(target - current) <= maxDelta) {
            target
        } else {
            current + signum(target - current) * maxDelta
        }
    }

    /**
     * Moves a value `current` towards `target`.
     *
     * @param current The current value
     * @param target The target value
     * @param maxDelta The maximum change that should be applied
     * @return The new value
     * @throws IllegalArgumentException if `maxDelta` is negative
     */
    @JvmStatic
    fun moveTowards(current: Double, target: Double, maxDelta: Double): Double {
        require(maxDelta > 0.0) { "maxDelta must be positive" }

        return if (abs(target - current) <= maxDelta) {
            target
        } else {
            current + signum(target - current) * maxDelta
        }
    }

    /**
     * Moves a value `current` towards `target`, but make sure the values
     * interpolate correctly when they wrap around 360 degrees.
     *
     * @param current The current value
     * @param target The target value
     * @param maxDelta The maximum change that should be applied
     * @return The new value
     * @throws IllegalArgumentException if `maxDelta` is negative
     */
    @JvmStatic
    fun moveTowardsDegrees(current: Float, target: Float, maxDelta: Float): Float {
        require(maxDelta > 0.0) { "maxDelta must be positive" }

        val delta = deltaDegrees(current, target)
        return if (abs(delta) <= maxDelta) {
            target
        } else {
            current + signum(delta) * maxDelta
        }
    }

    /**
     * Moves a value `current` towards `target`, but make sure the values
     * interpolate correctly when they wrap around 360 degrees.
     *
     * @param current The current value
     * @param target The target value
     * @param maxDelta The maximum change that should be applied
     * @return The new value
     * @throws IllegalArgumentException if `maxDelta` is negative
     */
    @JvmStatic
    fun moveTowardsDegrees(current: Double, target: Double, maxDelta: Double): Double {
        require(maxDelta > 0.0) { "maxDelta must be positive" }

        val delta = deltaDegrees(current, target)
        return if (abs(delta) <= maxDelta) {
            target
        } else {
            current + signum(delta) * maxDelta
        }
    }

    /**
     * Moves a value `current` towards `target`, but make sure the values
     * interpolate correctly when they wrap around 2 PI.
     *
     * @param current The current value
     * @param target The target value
     * @param maxDelta The maximum change that should be applied
     * @return The new value
     * @throws IllegalArgumentException if `maxDelta` is negative
     */
    @JvmStatic
    fun moveTowardsRadians(current: Float, target: Float, maxDelta: Float): Float {
        require(maxDelta > 0.0) { "maxDelta must be positive" }

        val delta = deltaRadians(current, target)
        return if (abs(delta) <= maxDelta) {
            target
        } else {
            current + signum(delta) * maxDelta
        }
    }

    /**
     * Moves a value `current` towards `target`, but make sure the values
     * interpolate correctly when they wrap around 2 PI.
     *
     * @param current The current value
     * @param target The target value
     * @param maxDelta The maximum change that should be applied
     * @return The new value
     * @throws IllegalArgumentException if `maxDelta` is negative
     */
    @JvmStatic
    fun moveTowardsRadians(current: Double, target: Double, maxDelta: Double): Double {
        require(maxDelta > 0.0) { "maxDelta must be positive" }

        val delta = deltaRadians(current, target)
        return if (abs(delta) <= maxDelta) {
            target
        } else {
            current + signum(delta) * maxDelta
        }
    }

    /**
     * Interpolates between the current and target values while easing in and
     * out at the limits.
     *
     * @param current The current value
     * @param target The target value
     * @param currentVelocity The current velocity (which is updated before the function returns)
     * @param smoothTime The time it takes to reach the target
     * @param deltaTime The time since the last call to this method
     * @param maxSpeed The maximum speed
     * @return The new value and velocity
     */
    @JvmStatic
    @JvmOverloads
    fun smoothDamp(
        current: Float,
        target: Float,
        currentVelocity: FloatRef,
        smoothTime: Float,
        deltaTime: Float = 1.0f / 20.0f,
        maxSpeed: Float = Float.POSITIVE_INFINITY,
    ): Float {
        val omega = 2f / smoothTime
        val x = omega * deltaTime
        val exp = 1f / (1f + x + 0.48f * x * x + 0.235f * x * x * x)

        val change = current - target

        // Clamp maximum speed
        val maxChange = maxSpeed * smoothTime
        val clampedChange = clamp(change, -maxChange, maxChange)
        val newTarget = current - clampedChange

        val temp = (currentVelocity.value + omega * clampedChange) * deltaTime
        var newVelocity = (currentVelocity.value - omega * temp) * exp
        var newPosition = newTarget + (clampedChange + temp) * exp

        // Prevent overshooting
        if ((target - current > 0f) == (newPosition > target)) {
            newPosition = target
            newVelocity = 0f
        }

        currentVelocity.value = newVelocity
        return newPosition
    }

    /**
     * Interpolates between the current and target values while easing in and
     * out at the limits.
     *
     * @param current The current value
     * @param target The target value
     * @param currentVelocity The current velocity (which is updated before the function returns)
     * @param smoothTime The time it takes to reach the target
     * @param deltaTime The time since the last call to this method
     * @param maxSpeed The maximum speed
     * @return The new value and velocity
     */
    @JvmStatic
    @JvmOverloads
    fun smoothDamp(
        current: Double,
        target: Double,
        currentVelocity: DoubleRef,
        smoothTime: Double,
        deltaTime: Double = 1.0 / 20.0,
        maxSpeed: Double = Double.POSITIVE_INFINITY,
    ): Double {
        val omega = 2 / smoothTime
        val x = omega * deltaTime
        val exp = 1 / (1 + x + 0.48 * x * x + 0.235 * x * x * x)

        val change = current - target

        // Clamp maximum speed
        val maxChange = maxSpeed * smoothTime
        val clampedChange = clamp(change, -maxChange, maxChange)
        val newTarget = current - clampedChange

        val temp = (currentVelocity.value + omega * clampedChange) * deltaTime
        var newVelocity = (currentVelocity.value - omega * temp) * exp
        var newPosition = newTarget + (clampedChange + temp) * exp

        // Prevent overshooting
        if ((target - current > 0) == (newPosition > target)) {
            newPosition = target
            newVelocity = 0.0
        }

        currentVelocity.value = newVelocity
        return newPosition
    }

    /**
     * Interpolates between the current and target values while easing in and
     * out at the limits.
     *
     * @param current The current value
     * @param target The target value
     * @param currentVelocity The current velocity (which is updated before the function returns)
     * @param smoothTime The time it takes to reach the target
     * @param deltaTime The time since the last call to this method
     * @param maxSpeed The maximum speed
     * @return The new value and velocity
     */
    @JvmStatic
    fun smoothDampDegrees(
        current: Float,
        target: Float,
        currentVelocity: FloatRef,
        smoothTime: Float,
        deltaTime: Float = 1.0f / 20.0f,
        maxSpeed: Float = Float.POSITIVE_INFINITY,
    ): Float {
        val delta = deltaDegrees(current, target)
        return target - smoothDamp(delta, 0f, currentVelocity, smoothTime, deltaTime, maxSpeed)
    }

    /**
     * Interpolates between the current and target values while easing in and
     * out at the limits.
     *
     * @param current The current value
     * @param target The target value
     * @param currentVelocity The current velocity (which is updated before the function returns)
     * @param smoothTime The time it takes to reach the target
     * @param deltaTime The time since the last call to this method
     * @param maxSpeed The maximum speed
     * @return The new value and velocity
     */
    @JvmStatic
    fun smoothDampDegrees(
        current: Double,
        target: Double,
        currentVelocity: DoubleRef,
        smoothTime: Double,
        deltaTime: Double = 1.0 / 20.0,
        maxSpeed: Double = Double.POSITIVE_INFINITY,
    ): Double {
        val delta = deltaDegrees(current, target)
        return target - smoothDamp(delta, 0.0, currentVelocity, smoothTime, deltaTime, maxSpeed)
    }

    /**
     * Interpolates between the current and target values while easing in and
     * out at the limits.
     *
     * @param current The current value
     * @param target The target value
     * @param currentVelocity The current velocity (which is updated before the function returns)
     * @param smoothTime The time it takes to reach the target
     * @param deltaTime The time since the last call to this method
     * @param maxSpeed The maximum speed
     * @return The new value and velocity
     */
    @JvmStatic
    fun smoothDampRadians(
        current: Float,
        target: Float,
        currentVelocity: FloatRef,
        smoothTime: Float,
        deltaTime: Float = 1.0f / 20.0f,
        maxSpeed: Float = Float.POSITIVE_INFINITY,
    ): Float {
        val delta = deltaRadians(current, target)
        return target - smoothDamp(delta, 0f, currentVelocity, smoothTime, deltaTime, maxSpeed)
    }

    /**
     * Interpolates between the current and target values while easing in and
     * out at the limits.
     *
     * @param current The current value
     * @param target The target value
     * @param currentVelocity The current velocity (which is updated before the function returns)
     * @param smoothTime The time it takes to reach the target
     * @param deltaTime The time since the last call to this method
     * @param maxSpeed The maximum speed
     * @return The new value and velocity
     */
    @JvmStatic
    fun smoothDampRadians(
        current: Double,
        target: Double,
        currentVelocity: DoubleRef,
        smoothTime: Double,
        deltaTime: Double = 1.0 / 20.0,
        maxSpeed: Double = Double.POSITIVE_INFINITY,
    ): Double {
        val delta = deltaRadians(current, target)
        return target - smoothDamp(delta, 0.0, currentVelocity, smoothTime, deltaTime, maxSpeed)
    }

    /**
     * Returns the sign of a number, or 0 if the number is 0.
     *
     * @param a The number
     * @return The sign of the number
     */
    @JvmStatic
    fun signum(a: Float): Int {
        return if (a == 0f) {
            0
        } else if (a > 0) {
            1
        } else {
            -1
        }
    }

    /**
     * Returns the sign of a number, or 0 if the number is 0.
     *
     * @param a The number
     * @return The sign of the number
     */
    @JvmStatic
    fun signum(a: Double): Int {
        return if (a == 0.0) {
            0
        } else if (a > 0) {
            1
        } else {
            -1
        }
    }

    /**
     * Returns the sign of a number, or 0 if the number is 0.
     *
     * @param a The number
     * @return The sign of the number
     */
    @JvmStatic
    fun signum(a: Int): Int {
        return if (a == 0) {
            0
        } else if (a > 0) {
            1
        } else {
            -1
        }
    }

    /**
     * Returns the sign of a number, or 0 if the number is 0.
     *
     * @param a The number
     * @return The sign of the number
     */
    @JvmStatic
    fun signum(a: Long): Long {
        return if (a == 0L) {
            0L
        } else if (a > 0L) {
            1L
        } else {
            -1L
        }
    }

    /**
     * Returns the floor of a floating point number as an integer.
     *
     * @param a The number
     * @return The floor of the number
     */
    @JvmStatic
    fun floorToInt(a: Float): Int {
        val floor = a.toInt()
        return if (a < floor) floor - 1 else floor
    }

    /**
     * Returns the floor of a floating point number as an integer.
     *
     * @param a The number
     * @return The floor of the number
     */
    @JvmStatic
    fun floorToInt(a: Double): Int {
        val floor = a.toInt()
        return if (a < floor) floor - 1 else floor
    }

    /**
     * Returns the floor of a floating point number as a long integer.
     *
     * @param a The number
     * @return The floor of the number
     */
    @JvmStatic
    fun floorToLong(a: Float): Long {
        val floor = a.toLong()
        return if (a < floor) floor - 1L else floor
    }

    /**
     * Returns the floor of a floating point number as a long integer.
     *
     * @param a The number
     * @return The floor of the number
     */
    @JvmStatic
    fun floorToLong(a: Double): Long {
        val floor = a.toLong()
        return if (a < floor) floor - 1L else floor
    }

    /**
     * Returns the fraction component of a floating point number.
     *
     * @param a The number
     * @return The fraction component of the number
     */
    @JvmStatic
    fun fraction(a: Float): Float {
        return a - floorToLong(a)
    }

    /**
     * Returns the fraction component of a floating point number.
     *
     * @param a The number
     * @return The fraction component of the number
     */
    @JvmStatic
    fun fraction(a: Double): Double {
        return a - floorToLong(a)
    }

    /**
     * Converts a number to a roman numeral. This should only be used for numbers < 5000,
     * (the Romans probably didn't even believe numbers went that high).
     *
     * Using a negative number or 0 will return "nulla".
     *
     * @param a The number to convert
     * @return The roman numeral string
     */
    @JvmStatic
    fun toRomanNumeral(a: Int): String {
        if (a <= 0) return "nulla" // nulla, sometimes used for 0

        val numeral = NUMERALS.floorKey(a)
        return if (a == numeral) {
            NUMERALS[a]!!
        } else NUMERALS[numeral] + toRomanNumeral(a - numeral)
    }

    /**
     * Converts a number of seconds to a human-readable time string.
     *
     * For example, 3661 seconds would be converted to "1h 1m 1s".
     *
     * @param seconds The number of seconds in your time
     * @return The human-readable string value
     */
    @JvmStatic
    fun toTime(seconds: Int): String {
        if (seconds <= 0)
            return "0s"

        val unit = TIME.floorKey(seconds)
        val amount: Int = seconds / unit
        return if (seconds % unit == 0) {
            amount.toString() + TIME[unit]
        } else amount.toString() + TIME[unit] + " " + toTime(seconds - amount * unit)
    }

    /**
     * Returns `true` if an `amount` of time has passed.
     *
     * @param lastMillis The time when the countdown started, in milliseconds.
     * @param amount The delay/cooldown time, in milliseconds.
     * @return `true` if enough time has passed.
     */
    @JvmStatic
    fun hasMillisPassed(lastMillis: Long, amount: Long): Boolean {
        return System.currentTimeMillis() - lastMillis > amount
    }
}
