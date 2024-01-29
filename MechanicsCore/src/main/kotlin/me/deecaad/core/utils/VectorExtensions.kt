package me.deecaad.core.utils

import org.bukkit.util.Vector


/**
 * Sets the length of the given vector to the given length.
 *
 * If the given vector is zero-length, an [IllegalArgumentException] is
 * thrown.
 *
 * @receiver The vector
 * @param length The new length
 * @return The reference to the given vector
 */
fun Vector.setLength(length: Double): Vector {
    val currentLength = this.length()
    if (currentLength.approximately(0.0))
        throw IllegalArgumentException("Cannot set the length of a zero-length vector")

    return this.multiply(length / currentLength)
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
 * @receiver The base vector
 * @param up The up vector
 * @param back The back vector
 * @return The new vector
 */
fun Vector.getPerpendicular(
    up: Vector = VectorUtil.UP,
    back: Vector = VectorUtil.SOUTH
): Vector {
    if (VectorUtil.isZero(this))
        throw IllegalArgumentException("Cannot get a perpendicular vector of a zero-length vector")

    val cross = this.getCrossProduct(up)

    // If the cross product is zero, the vectors are parallel. Use the back
    // vector instead. If the back vector happens to be parallel as well,
    // this will return the zero vector!
    if (cross.lengthSquared() < NumberUtil.EPSILON_DOUBLE) {
        return this.getCrossProduct(back)
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
 * @receiver The first vector
 * @param other The second vector
 * @return The angle between the vectors, in radians
 * @see Math.toDegrees
 */
fun Vector.getAngleBetween(other: Vector): Double {
    return VectorUtil.getAngleBetween(this, other)
}