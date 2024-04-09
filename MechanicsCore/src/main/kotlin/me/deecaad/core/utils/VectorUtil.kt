package me.deecaad.core.utils

import org.bukkit.util.Vector
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A collection of utility functions for [Vector]s.
 *
 * @see NumberUtil
 */
object VectorUtil {
    /**
     * An immutable vector with all components set to 0.
     */
    val ZERO = ImmutableVector(0, 0, 0)

    /**
     * An immutable vector pointing in the positive Y direction.
     */
    val UP = ImmutableVector(0, 1, 0)

    /**
     * An immutable vector pointing in the negative Y direction.
     */
    val DOWN = ImmutableVector(0, -1, 0)

    /**
     * An immutable vector pointing in the negative Z direction.
     */
    val NORTH = ImmutableVector(0, 0, -1)

    /**
     * An immutable vector pointing in the positive Z direction.
     */
    val SOUTH = ImmutableVector(0, 0, 1)

    /**
     * An immutable vector pointing in the positive X direction.
     */
    val EAST = ImmutableVector(1, 0, 0)

    /**
     * An immutable vector pointing in the negative X direction.
     */
    val WEST = ImmutableVector(-1, 0, 0)

    /**
     * Sets the length of the given vector to the given length.
     *
     * If the given vector is zero-length, an [IllegalArgumentException] is
     * thrown.
     *
     * @param vector The vector
     * @param length The new length
     * @return The reference to the given vector
     */
    @JvmStatic
    fun setLength(
        vector: Vector,
        length: Double,
    ): Vector {
        val currentLength = vector.length()
        if (currentLength.approximately(0.0)) {
            throw IllegalArgumentException("Cannot set the length of a zero-length vector")
        }

        return vector.multiply(length / currentLength)
    }

    /**
     * Adds the given vector to the current vector, scaled by the given factor.
     *
     * @param vector The current vector
     * @param other The vector to add
     * @param scale The factor to scale the other vector by
     * @return The reference to the current vector
     */
    @JvmStatic
    fun addScaledVector(
        vector: Vector,
        other: Vector,
        scale: Double,
    ): Vector {
        vector.x += other.x * scale
        vector.y += other.y * scale
        vector.z += other.z * scale
        return vector
    }

    /**
     * Returns a vector pointing in the same direction as the given yaw and
     * pitch.
     *
     * @param yaw The yaw in degrees.
     * @param pitch The pitch in degrees.
     * @return the vector
     */
    @JvmStatic
    fun fromDegrees(
        yaw: Double,
        pitch: Double,
    ): Vector {
        val yawRad = Math.toRadians(yaw)
        val pitchRad = Math.toRadians(pitch)

        return fromRadians(yawRad, pitchRad)
    }

    /**
     * Returns a vector pointing in the same direction as the given yaw and
     * pitch.
     *
     * @param yaw The yaw in radians.
     * @param pitch The pitch in radians.
     * @return the new vector
     */
    @JvmStatic
    fun fromRadians(
        yaw: Double,
        pitch: Double,
    ): Vector {
        val cosPitch = cos(pitch)

        val x = sin(yaw) * -cosPitch
        val y = -sin(pitch)
        val z = cos(yaw) * cosPitch

        return Vector(x, y, z)
    }

    /**
     * Linearly interpolates between two vectors.
     *
     * @param a The first vector
     * @param b The second vector
     * @param t The interpolation parameter, clamped between 0 and 1
     * @return The new interpolated vector
     */
    @JvmStatic
    fun lerp(
        a: Vector,
        b: Vector,
        t: Double,
    ): Vector {
        val x = NumberUtil.lerp(a.x, b.x, t)
        val y = NumberUtil.lerp(a.y, b.y, t)
        val z = NumberUtil.lerp(a.z, b.z, t)

        return Vector(x, y, z)
    }

    /**
     * Linearly interpolates between two vectors.
     *
     * @param a The first vector
     * @param b The second vector
     * @param t The interpolation parameter
     * @return The new interpolated vector
     */
    @JvmStatic
    fun lerpUnclamped(
        a: Vector,
        b: Vector,
        t: Double,
    ): Vector {
        val x = NumberUtil.lerpUnclamped(a.x, b.x, t)
        val y = NumberUtil.lerpUnclamped(a.y, b.y, t)
        val z = NumberUtil.lerpUnclamped(a.z, b.z, t)

        return Vector(x, y, z)
    }

    /**
     * Returns the component-wise minimum of two vectors.
     *
     * @param a The first vector
     * @param b The second vector
     * @return The new vector
     */
    @JvmStatic
    fun min(
        a: Vector,
        b: Vector,
    ): Vector {
        val x = Math.min(a.x, b.x)
        val y = Math.min(a.y, b.y)
        val z = Math.min(a.z, b.z)

        return Vector(x, y, z)
    }

    /**
     * Returns the component-wise maximum of two vectors.
     *
     * @param a The first vector
     * @param b The second vector
     * @return The new vector
     */
    @JvmStatic
    fun max(
        a: Vector,
        b: Vector,
    ): Vector {
        val x = Math.max(a.x, b.x)
        val y = Math.max(a.y, b.y)
        val z = Math.max(a.z, b.z)

        return Vector(x, y, z)
    }

    /**
     * Returns a vector perpendicular to the given vector. If the given vector
     * is parallel to the up vector, the back vector is used instead.
     *
     * You can optionally specify the up and back vectors to use. If you choose
     * to change them, make sure both vectors are not:
     * 1. similar
     * 2. parallel
     *
     * @param base The base vector
     * @param up The up vector
     * @param back The back vector
     * @return The new vector
     */
    @JvmStatic
    @JvmOverloads
    fun getPerpendicular(
        base: Vector,
        up: Vector = UP,
        back: Vector = SOUTH,
    ): Vector {
        if (isZero(base)) {
            throw IllegalArgumentException("Cannot get a perpendicular vector of a zero-length vector")
        }

        val cross = base.getCrossProduct(up)

        // If the cross product is zero, the vectors are parallel. Use the back
        // vector instead. If the back vector happens to be parallel as well,
        // this will return the zero vector!
        if (cross.lengthSquared() < NumberUtil.EPSILON_DOUBLE) {
            return base.getCrossProduct(back)
        }

        return cross
    }

    /**
     * Returns the angle, in radians, between two vectors. The returned angle
     * will be in the same range as [Math.acos], 0 to pi.
     *
     * If either vector is zero-length, this will return 0.
     *
     * This method is faster than Spigot's [Vector.angle] method because it
     * calls [Math.sqrt] 1 time instead of 2 times.
     *
     * @param a The first vector
     * @param b The second vector
     * @return The angle between the vectors, in radians
     * @see Math.toDegrees
     */
    @JvmStatic
    fun getAngleBetween(
        a: Vector,
        b: Vector,
    ): Double {
        // This uses the formula for the dot product of two vectors:
        // a · b = |a| * |b| * cos(θ)
        // θ = acos(a · b / (|a| * |b|))

        // Check for zero-length vectors to avoid division by zero
        val lengthsProduct = sqrt(a.lengthSquared() * b.lengthSquared())
        if (lengthsProduct.approximately(0.0)) return 0.0

        // Calculate the cosine of the angle using the dot product
        var cosTheta = a.dot(b) / lengthsProduct

        // Clamp the cosine value to the range [-1, 1] to handle floating-point inaccuracies
        cosTheta = cosTheta.clamp(-1.0, 1.0)

        // Return the angle in radians
        return acos(cosTheta)
    }

    /**
     * Returns true if the given vector is approximately zero-length.
     *
     * @param a The vector
     * @param epsilon The maximum allowed difference to 0.0
     * @return
     */
    @JvmStatic
    @JvmOverloads
    fun isZero(
        a: Vector,
        epsilon: Double = NumberUtil.EPSILON_DOUBLE,
    ): Boolean {
        return a.x.approximately(0.0, epsilon) &&
            a.y.approximately(0.0, epsilon) &&
            a.z.approximately(0.0, epsilon)
    }
}
