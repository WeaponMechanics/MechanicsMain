package me.deecaad.core.utils

/**
 * Helper function to test a random chance. The chance should be a floating point between
 * [0.0f, 1.0f]. If the chance is 1.0f, this method will always return true. If the chance
 * is 0.0f, this method will always return false.
 *
 * @receiver The chance to test
 * @return True if the chance was successful
 */
fun Float.chance(): Boolean {
    return RandomUtil.chance(this)
}

/**
 * Helper function to test a random chance. The chance should be a floating point between
 * [0.0, 1.0]. If the chance is 1.0, this method will always return true. If the chance
 * is 0.0, this method will always return false.
 *
 * @receiver The chance to test
 * @return True if the chance was successful
 */
fun Double.chance(): Boolean {
    return RandomUtil.chance(this)
}

/**
 * Returns a random element from the given array, where the probabilities of
 * each element being selected are equal.
 *
 * @return The random element
 */
fun <T> Array<T>.random(): T {
    return RandomUtil.element(this)
}

/**
 * Returns a random element from the given list, where the probabilities of
 * each element being selected are equal.
 *
 * @return The random element
 */
fun <T> List<T>.random(): T {
    return RandomUtil.element(this)
}