package me.deecaad.core.utils

/**
 * A reference to a float value.
 *
 * This is used to pass a float by reference instead of by value.
 *
 * We are aware that kotlin has a built-in reference type, along with the primitive
 * types, but we cannot use that since any plugin that wants to use MechanicsCore
 * as a dependency would have to use kotlin, and might have shading issues... So:
 */
data class FloatRef(var value: Float)