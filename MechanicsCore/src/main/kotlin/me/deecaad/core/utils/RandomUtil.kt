package me.deecaad.core.utils

import org.bukkit.util.Vector
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.sqrt

/**
 * A collection of utility functions for random generation.
 */
object RandomUtil {
    /**
     * Helper function to test a random chance. The chance should be a floating point between
     * [0.0f, 1.0f]. If the chance is 1.0f, this method will always return true. If the chance
     * is 0.0f, this method will always return false.
     *
     * @param chance The chance to test
     * @return True if the chance was successful
     */
    @JvmStatic
    fun chance(chance: Float): Boolean {
        return chance >= 1.0f || chance > 0.0f && ThreadLocalRandom.current().nextFloat() < chance
    }

    /**
     * Helper function to test a random chance. The chance should be a floating point between
     * [0.0, 1.0]. If the chance is 1.0, this method will always return true. If the chance
     * is 0.0, this method will always return false.
     *
     * @param chance The chance to test
     * @return True if the chance was successful
     */
    @JvmStatic
    fun chance(chance: Double): Boolean {
        return chance >= 1.0 || chance > 0.0 && ThreadLocalRandom.current().nextDouble() < chance
    }

    /**
     * Returns a random number between 0 (inclusive) and 1 (inclusive).
     *
     * @param min The minimum value (inclusive)
     * @param max The maximum value (inclusive)
     * @return The random number
     */
    @JvmStatic
    fun range(
        min: Int,
        max: Int,
    ): Int {
        return ThreadLocalRandom.current().nextInt(min, max + 1)
    }

    /**
     * Returns a random number between 0 (inclusive) and 1 (exclusive).
     *
     * @param min The minimum value (inclusive)
     * @param max The maximum value (exclusive)
     * @return The random number
     */
    @JvmStatic
    fun range(
        min: Float,
        max: Float,
    ): Float {
        return if (min == max) min else ThreadLocalRandom.current().nextFloat() * (max - min) + min
    }

    /**
     * Returns a random number between 0 (inclusive) and 1 (exclusive).
     *
     * @param min The minimum value (inclusive)
     * @param max The maximum value (exclusive)
     * @return The random number
     */
    @JvmStatic
    fun range(
        min: Double,
        max: Double,
    ): Double {
        return if (min == max) min else ThreadLocalRandom.current().nextDouble(min, max)
    }

    /**
     * Returns a random element from the given array, where the probabilities of
     * each element being selected are equal.
     *
     * @param array The array
     * @return The random element
     * @throws IllegalArgumentException if the array is empty
     */
    @JvmStatic
    fun <T> element(array: Array<T>): T {
        require(array.isNotEmpty()) { "array must have at least 1 element" }
        return array[ThreadLocalRandom.current().nextInt(array.size)]
    }

    /**
     * Returns a random element from the given list, where the probabilities of
     * each element being selected are equal.
     *
     * @param list The list
     * @return The random element
     * @throws IllegalArgumentException if the list is empty
     */
    @JvmStatic
    fun <T> element(list: List<T>): T {
        require(list.isNotEmpty()) { "list must have at least 1 element" }
        return list[ThreadLocalRandom.current().nextInt(list.size)]
    }

    /**
     * Generates a random normalized vector.
     *
     * @return A random normalized vector.
     */
    @JvmStatic
    fun onUnitSphere(): Vector {
        var x: Double
        var y: Double
        var z: Double
        var length: Double

        // Using a loop here is overkill, but avoids the possibility of a divide
        // by zero error, or a vector with a small length causing instability.
        do {
            // Multiply by 2 and subtract 1 to remap from [0, 1] to [-1, 1]
            x = ThreadLocalRandom.current().nextGaussian()
            y = ThreadLocalRandom.current().nextGaussian()
            z = ThreadLocalRandom.current().nextGaussian()

            length = sqrt(x * x + y * y + z * z)
        } while (length < NumberUtil.EPSILON_DOUBLE)

        return Vector(x / length, y / length, z / length)
    }
}
