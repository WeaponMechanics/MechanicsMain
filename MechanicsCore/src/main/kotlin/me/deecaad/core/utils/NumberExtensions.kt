package me.deecaad.core.utils

/**
 * Squares a value, i.e. multiplies it by itself.
 *
 * @receiver The value to square
 * @return The squared value
 * @see NumberUtil.square
 */
fun Int.square(): Int {
    return NumberUtil.square(this)
}

/**
 * Squares a value, i.e. multiplies it by itself.
 *
 * @receiver The value to square
 * @return The squared value
 * @see NumberUtil.square
 */
fun Float.square(): Float {
    return NumberUtil.square(this)
}

/**
 * Squares a value, i.e. multiplies it by itself.
 *
 * @receiver The value to square
 * @return The squared value
 * @see NumberUtil.square
 */
fun Double.square(): Double {
    return NumberUtil.square(this)
}

/**
 * Squares a value, i.e. multiplies it by itself.
 *
 * @receiver The value to square
 * @return The squared value
 * @see NumberUtil.square
 */
fun Long.square(): Long {
    return NumberUtil.square(this)
}

/**
 * Clamps a value between a minimum and maximum value.
 *
 * @receiver The value to clamp
 * @param min The minimum value
 * @param max The maximum value
 * @return The clamped value
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Int.clamp(
    min: Int,
    max: Int,
): Int {
    return NumberUtil.clamp(this, min, max)
}

/**
 * Clamps a value between a minimum and maximum value.
 *
 * @receiver The value to clamp
 * @param min The minimum value
 * @param max The maximum value
 * @return The clamped value
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Float.clamp(
    min: Float,
    max: Float,
): Float {
    return NumberUtil.clamp(this, min, max)
}

/**
 * Clamps a value between a minimum and maximum value.
 *
 * @receiver The value to clamp
 * @param min The minimum value
 * @param max The maximum value
 * @return The clamped value
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Double.clamp(
    min: Double,
    max: Double,
): Double {
    return NumberUtil.clamp(this, min, max)
}

/**
 * Clamps a value between a minimum and maximum value.
 *
 * @receiver The value to clamp
 * @param min The minimum value
 * @param max The maximum value
 * @return The clamped value
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Long.clamp(
    min: Long,
    max: Long,
): Long {
    return NumberUtil.clamp(this, min, max)
}

/**
 * Clamps a value between 0 and 1.
 *
 * @receiver The value to clamp
 * @return The clamped value
 */
fun Float.clamp01(): Float {
    return NumberUtil.clamp(this, 0.0f, 1.0f)
}

/**
 * Clamps a value between 0 and 1.
 *
 * @receiver The value to clamp
 * @return The clamped value
 */
fun Double.clamp01(): Double {
    return NumberUtil.clamp(this, 0.0, 1.0)
}

/**
 * Checks if two floating point numbers are approximately equal.
 *
 * @receiver The first number
 * @param other The second number
 * @return True if the numbers are approximately equal
 */
fun Float.approximately(
    other: Float,
    epsilon: Float = NumberUtil.EPSILON,
): Boolean {
    return NumberUtil.approximately(this, other, epsilon)
}

/**
 * Checks if two floating point numbers are approximately equal.
 *
 * @receiver The first number
 * @param other The second number
 * @return True if the numbers are approximately equal
 */
fun Double.approximately(
    other: Double,
    epsilon: Double = NumberUtil.EPSILON_DOUBLE,
): Boolean {
    return NumberUtil.approximately(this, other, epsilon)
}

/**
 * Linearly interpolates between two values.
 *
 * @receiver The first value
 * @param target The second value
 * @param t The interpolation value, clamped between 0 and 1
 * @return The interpolated value
 */
fun Float.lerp(
    target: Float,
    t: Float,
): Float {
    return NumberUtil.lerp(this, target, t)
}

/**
 * Linearly interpolates between two values.
 *
 * @receiver The first value
 * @param target The second value
 * @param t The interpolation value, clamped between 0 and 1
 * @return The interpolated value
 */
fun Double.lerp(
    target: Double,
    t: Double,
): Double {
    return NumberUtil.lerp(this, target, t)
}

/**
 * Linearly interpolates between two values.
 *
 * If the interpolation value is outside the range [0, 1], the
 * returned value will be outside the range [a, b]. Typically,
 * you'll want to use [lerp] instead.
 *
 * @receiver The first value
 * @param target The second value
 * @param t The interpolation value.
 * @return The interpolated value
 */
fun Float.lerpUnclamped(
    target: Float,
    t: Float,
): Float {
    return NumberUtil.lerpUnclamped(this, target, t)
}

/**
 * Linearly interpolates between two values.
 *
 * If the interpolation value is outside the range [0, 1], the
 * returned value will be outside the range [a, b]. Typically,
 * you'll want to use [lerp] instead.
 *
 * @receiver The first value
 * @param target The second value
 * @param t The interpolation value.
 * @return The interpolated value
 */
fun Double.lerpUnclamped(
    target: Double,
    t: Double,
): Double {
    return NumberUtil.lerpUnclamped(this, target, t)
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
fun Float.lerpDegrees(
    target: Float,
    t: Float,
): Float {
    return NumberUtil.lerpDegrees(this, target, t)
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
fun Double.lerpDegrees(
    target: Double,
    t: Double,
): Double {
    return NumberUtil.lerp(this, target, t)
}

/**
 * Linearly interpolates between two angles, in radians.
 *
 * This is the same as [lerp], but makes sure the values interpolate
 * correctly when they wrap around 2 PI. This method returns
 * the shortest path between the specified angles. This method wraps
 * around values that are outside the range [-PI, PI].
 */
fun Float.lerpRadians(
    target: Float,
    t: Float,
): Float {
    return NumberUtil.lerpRadians(this, target, t)
}

/**
 * Linearly interpolates between two angles, in radians.
 *
 * This is the same as [lerp], but makes sure the values interpolate
 * correctly when they wrap around 2 PI. This method returns
 * the shortest path between the specified angles. This method wraps
 * around values that are outside the range [-PI, PI].
 */
fun Double.lerpRadians(
    target: Double,
    t: Double,
): Double {
    return NumberUtil.lerpRadians(this, target, t)
}

/**
 * The inverse of [lerp]. Given a value between `a` and `b`, this method
 * returns the interpolation value `t` between 0 and 1.
 *
 * @receiver The first value
 * @param target The second value
 * @param value The interpolated value
 * @return The interpolation value `t`
 * @throws IllegalArgumentException if `a` and `b` are equal
 */
fun Float.inverseLerp(
    target: Float,
    value: Float,
): Float {
    return NumberUtil.inverseLerp(this, target, value)
}

/**
 * The inverse of [lerp]. Given a value between `a` and `b`, this method
 * returns the interpolation value `t` between 0 and 1.
 *
 * @receiver The first value
 * @param target The second value
 * @param value The interpolated value
 * @return The interpolation value `t`
 * @throws IllegalArgumentException if `a` and `b` are equal
 */
fun Double.inverseLerp(
    target: Double,
    value: Double,
): Double {
    return NumberUtil.inverseLerp(this, target, value)
}

/**
 * The inverse of [lerpUnclamped]. Given a value between `a` and `b`, this method
 * returns the interpolation value `t`.
 *
 * @receiver The first value
 * @param target The second value
 * @param value The interpolated value
 * @return The interpolation value `t`
 * @throws IllegalArgumentException if `a` and `b` are equal
 */
fun Float.inverseLerpUnclamped(
    target: Float,
    value: Float,
): Float {
    return NumberUtil.inverseLerpUnclamped(this, target, value)
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
fun Double.inverseLerpUnclamped(
    target: Double,
    value: Double,
): Double {
    return NumberUtil.inverseLerpUnclamped(this, target, value)
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
 * @receiver The value to remap
 * @param from1 The lower bound of the first range
 * @param to1 The upper bound of the first range
 * @param from2 The lower bound of the second range
 * @param to2 The upper bound of the second range
 * @return The remapped value
 */
fun Float.remap(
    from1: Float,
    to1: Float,
    from2: Float,
    to2: Float,
): Float {
    return NumberUtil.remap(this, from1, to1, from2, to2)
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
 * @receiver The value to remap
 * @param from1 The lower bound of the first range
 * @param to1 The upper bound of the first range
 * @param from2 The lower bound of the second range
 * @param to2 The upper bound of the second range
 * @return The remapped value
 */
fun Double.remap(
    from1: Double,
    to1: Double,
    from2: Double,
    to2: Double,
): Double {
    return NumberUtil.remap(this, from1, to1, from2, to2)
}

/**
 * Normalizes an angle in degrees to the range [-180, 180].
 *
 * @receiver The angle to normalize
 * @return The normalized angle
 */
fun Float.normalizeDegrees(): Float {
    return NumberUtil.normalizeDegrees(this)
}

/**
 * Normalizes an angle in degrees to the range [-180, 180].
 *
 * @receiver The angle to normalize
 * @return The normalized angle
 */
fun Double.normalizeDegrees(): Double {
    return NumberUtil.normalizeDegrees(this)
}

/**
 * Normalizes an angle in radians to the range [-PI, PI].
 *
 * @receiver The angle to normalize
 * @return The normalized angle
 */
fun Float.normalizeRadians(): Float {
    return NumberUtil.normalizeRadians(this)
}

/**
 * Normalizes an angle in radians to the range [-PI, PI].
 *
 * @receiver The angle to normalize
 * @return The normalized angle
 */
fun Double.normalizeRadians(): Double {
    return NumberUtil.normalizeRadians(this)
}

/**
 * Returns the smallest angle between two angles, in degrees.
 *
 * @receiver The first angle
 * @param other The second angle
 * @return The smallest angle between the two angles
 */
fun Float.deltaDegrees(other: Float): Float {
    return NumberUtil.deltaDegrees(this, other)
}

/**
 * Returns the smallest angle between two angles, in degrees.
 *
 * @receiver The first angle
 * @param other The second angle
 * @return The smallest angle between the two angles
 */
fun Double.deltaDegrees(other: Double): Double {
    return NumberUtil.deltaDegrees(this, other)
}

/**
 * Returns the smallest angle between two angles, in radians.
 *
 * @receiver The first angle
 * @param other The second angle
 * @return The smallest angle between the two angles
 */
fun Float.deltaRadians(other: Float): Float {
    return NumberUtil.deltaRadians(this, other)
}

/**
 * Returns the smallest angle between two angles, in radians.
 *
 * @receiver The first angle
 * @param other The second angle
 * @return The smallest angle between the two angles
 */
fun Double.deltaRadians(other: Double): Double {
    return NumberUtil.deltaRadians(this, other)
}

/**
 * Moves a value `current` towards `target`.
 *
 * @receiver The current value
 * @param target The target value
 * @param maxDelta The maximum change that should be applied
 * @return The new value
 * @throws IllegalArgumentException if `maxDelta` is negative
 */
fun Float.moveTowards(
    target: Float,
    maxDelta: Float,
): Float {
    return NumberUtil.moveTowards(this, target, maxDelta)
}

/**
 * Moves a value `current` towards `target`.
 *
 * @receiver The current value
 * @param target The target value
 * @param maxDelta The maximum change that should be applied
 * @return The new value
 * @throws IllegalArgumentException if `maxDelta` is negative
 */
fun Double.moveTowards(
    target: Double,
    maxDelta: Double,
): Double {
    return NumberUtil.moveTowards(this, target, maxDelta)
}

/**
 * Moves a value `current` towards `target`, but make sure the values
 * interpolate correctly when they wrap around 360 degrees.
 *
 * @receiver The current value
 * @param target The target value
 * @param maxDelta The maximum change that should be applied
 * @return The new value
 * @throws IllegalArgumentException if `maxDelta` is negative
 */
fun Float.moveTowardsDegrees(
    target: Float,
    maxDelta: Float,
): Float {
    return NumberUtil.moveTowardsDegrees(this, target, maxDelta)
}

/**
 * Moves a value `current` towards `target`, but make sure the values
 * interpolate correctly when they wrap around 360 degrees.
 *
 * @receiver The current value
 * @param target The target value
 * @param maxDelta The maximum change that should be applied
 * @return The new value
 * @throws IllegalArgumentException if `maxDelta` is negative
 */
fun Double.moveTowardsDegrees(
    target: Double,
    maxDelta: Double,
): Double {
    return NumberUtil.moveTowardsDegrees(this, target, maxDelta)
}

/**
 * Moves a value `current` towards `target`, but make sure the values
 * interpolate correctly when they wrap around 2 PI.
 *
 * @receiver The current value
 * @param target The target value
 * @param maxDelta The maximum change that should be applied
 * @return The new value
 * @throws IllegalArgumentException if `maxDelta` is negative
 */
fun Float.moveTowardsRadians(
    target: Float,
    maxDelta: Float,
): Float {
    return NumberUtil.moveTowardsRadians(this, target, maxDelta)
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
fun Double.moveTowardsRadians(
    target: Double,
    maxDelta: Double,
): Double {
    return NumberUtil.moveTowardsRadians(this, target, maxDelta)
}

/**
 * Interpolates between the current and target values while easing in and
 * out at the limits.
 *
 * @receiver The current value
 * @param target The target value
 * @param currentVelocity The current velocity (which is updated before the function returns)
 * @param smoothTime The time it takes to reach the target
 * @param deltaTime The time since the last call to this method
 * @param maxSpeed The maximum speed
 * @return The new value and velocity
 */
fun Float.smoothDamp(
    target: Float,
    currentVelocity: FloatRef,
    smoothTime: Float,
    deltaTime: Float = 1.0f / 20.0f,
    maxSpeed: Float = Float.POSITIVE_INFINITY,
): Float {
    return NumberUtil.smoothDamp(this, target, currentVelocity, smoothTime, deltaTime, maxSpeed)
}

/**
 * Interpolates between the current and target values while easing in and
 * out at the limits.
 *
 * @receiver The current value
 * @param target The target value
 * @param currentVelocity The current velocity (which is updated before the function returns)
 * @param smoothTime The time it takes to reach the target
 * @param deltaTime The time since the last call to this method
 * @param maxSpeed The maximum speed
 * @return The new value and velocity
 */
fun Double.smoothDamp(
    target: Double,
    currentVelocity: DoubleRef,
    smoothTime: Double,
    deltaTime: Double = 1.0 / 20.0,
    maxSpeed: Double = Double.POSITIVE_INFINITY,
): Double {
    return NumberUtil.smoothDamp(this, target, currentVelocity, smoothTime, deltaTime, maxSpeed)
}

/**
 * Interpolates between the current and target values while easing in and
 * out at the limits.
 *
 * @receiver The current value
 * @param target The target value
 * @param currentVelocity The current velocity (which is updated before the function returns)
 * @param smoothTime The time it takes to reach the target
 * @param deltaTime The time since the last call to this method
 * @param maxSpeed The maximum speed
 * @return The new value and velocity
 */
fun Float.smoothDampDegrees(
    target: Float,
    currentVelocity: FloatRef,
    smoothTime: Float,
    deltaTime: Float = 1.0f / 20.0f,
    maxSpeed: Float = Float.POSITIVE_INFINITY,
): Float {
    return NumberUtil.smoothDampDegrees(this, target, currentVelocity, smoothTime, deltaTime, maxSpeed)
}

/**
 * Interpolates between the current and target values while easing in and
 * out at the limits.
 *
 * @receiver The current value
 * @param target The target value
 * @param currentVelocity The current velocity (which is updated before the function returns)
 * @param smoothTime The time it takes to reach the target
 * @param deltaTime The time since the last call to this method
 * @param maxSpeed The maximum speed
 * @return The new value and velocity
 */
fun Double.smoothDampDegrees(
    target: Double,
    currentVelocity: DoubleRef,
    smoothTime: Double,
    deltaTime: Double = 1.0 / 20.0,
    maxSpeed: Double = Double.POSITIVE_INFINITY,
): Double {
    return NumberUtil.smoothDampDegrees(this, target, currentVelocity, smoothTime, deltaTime, maxSpeed)
}

/**
 * Interpolates between the current and target values while easing in and
 * out at the limits.
 *
 * @receiver The current value
 * @param target The target value
 * @param currentVelocity The current velocity (which is updated before the function returns)
 * @param smoothTime The time it takes to reach the target
 * @param deltaTime The time since the last call to this method
 * @param maxSpeed The maximum speed
 * @return The new value and velocity
 */
fun Float.smoothDampRadians(
    target: Float,
    currentVelocity: FloatRef,
    smoothTime: Float,
    deltaTime: Float = 1.0f / 20.0f,
    maxSpeed: Float = Float.POSITIVE_INFINITY,
): Float {
    return NumberUtil.smoothDampRadians(this, target, currentVelocity, smoothTime, deltaTime, maxSpeed)
}

/**
 * Interpolates between the current and target values while easing in and
 * out at the limits.
 *
 * @receiver The current value
 * @param target The target value
 * @param currentVelocity The current velocity (which is updated before the function returns)
 * @param smoothTime The time it takes to reach the target
 * @param deltaTime The time since the last call to this method
 * @param maxSpeed The maximum speed
 * @return The new value and velocity
 */
fun Double.smoothDampRadians(
    target: Double,
    currentVelocity: DoubleRef,
    smoothTime: Double,
    deltaTime: Double = 1.0 / 20.0,
    maxSpeed: Double = Double.POSITIVE_INFINITY,
): Double {
    return NumberUtil.smoothDampRadians(this, target, currentVelocity, smoothTime, deltaTime, maxSpeed)
}

/**
 * Returns the sign of a number, or 0 if the number is 0.
 *
 * @receiver The number
 * @return The sign of the number
 */
fun Float.signum(): Int {
    return NumberUtil.signum(this)
}

/**
 * Returns the sign of a number, or 0 if the number is 0.
 *
 * @receiver The number
 * @return The sign of the number
 */
fun Double.signum(): Int {
    return NumberUtil.signum(this)
}

/**
 * Returns the sign of a number, or 0 if the number is 0.
 *
 * @receiver The number
 * @return The sign of the number
 */
fun Int.signum(): Int {
    return NumberUtil.signum(this)
}

/**
 * Returns the sign of a number, or 0 if the number is 0.
 *
 * @receiver The number
 * @return The sign of the number
 */
fun Long.signum(a: Long): Long {
    return NumberUtil.signum(a)
}

/**
 * Returns the floor of a floating point number as an integer.
 *
 * @receiver The number
 * @return The floor of the number
 */
fun Float.floorToInt(): Int {
    return NumberUtil.floorToInt(this)
}

/**
 * Returns the floor of a floating point number as an integer.
 *
 * @receiver The number
 * @return The floor of the number
 */
fun Double.floorToInt(): Int {
    return NumberUtil.floorToInt(this)
}

/**
 * Returns the floor of a floating point number as a long integer.
 *
 * @receiver The number
 * @return The floor of the number
 */
fun Float.floorToLong(): Long {
    return NumberUtil.floorToLong(this)
}

/**
 * Returns the floor of a floating point number as a long integer.
 *
 * @receiver The number
 * @return The floor of the number
 */
fun Double.floorToLong(): Long {
    return NumberUtil.floorToLong(this)
}

/**
 * Returns the fraction component of a floating point number.
 *
 * @param a The number
 * @return The fraction component of the number
 */
fun Float.fraction(): Float {
    return NumberUtil.fraction(this)
}

/**
 * Returns the fraction component of a floating point number.
 *
 * @receiver The number
 * @return The fraction component of the number
 */
fun Double.fraction(): Double {
    return NumberUtil.fraction(this)
}

/**
 * Returns `true` if an `amount` of time has passed.
 *
 * @receiver The time when the countdown started, in milliseconds.
 * @param amount The delay/cooldown time, in milliseconds.
 * @return `true` if enough time has passed.
 */
fun Long.hasMillisPassed(amount: Long): Boolean {
    return NumberUtil.hasMillisPassed(this, amount)
}
