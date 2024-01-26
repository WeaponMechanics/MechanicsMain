package me.deecaad.core.utils

import org.bukkit.util.Vector

/**
 * An immutable version of [Vector]. When a method is called that would normally
 * change the vector, a clone of the vector is used and returned instead.
 *
 * @see Vector
 */
class ImmutableVector : Vector {

    constructor() : super()
    constructor(x: Int, y: Int, z: Int) : super(x, y, z)
    constructor(x: Double, y: Double, z: Double) : super(x, y, z)
    constructor(x: Float, y: Float, z: Float) : super(x, y, z)

    override fun add(vec: Vector): Vector {
        return clone().add(vec)
    }

    override fun subtract(vec: Vector): Vector {
        return clone().subtract(vec)
    }

    override fun multiply(vec: Vector): Vector {
        return clone().multiply(vec)
    }

    override fun multiply(m: Int): Vector {
        return clone().multiply(m)
    }

    override fun multiply(m: Double): Vector {
        return clone().multiply(m)
    }

    override fun multiply(m: Float): Vector {
        return clone().multiply(m)
    }

    override fun divide(vec: Vector): Vector {
        return clone().divide(vec)
    }

    override fun copy(vec: Vector): Vector {
        return clone().copy(vec)
    }

    override fun midpoint(other: Vector): Vector {
        return getMidpoint(other)
    }

    override fun crossProduct(o: Vector): Vector {
        return getCrossProduct(o)
    }

    override fun normalize(): Vector {
        return clone().normalize()
    }

    override fun zero(): Vector {
        return clone().zero()
    }

    override fun rotateAroundX(angle: Double): Vector {
        return clone().rotateAroundX(angle)
    }

    override fun rotateAroundY(angle: Double): Vector {
        return clone().rotateAroundY(angle)
    }

    override fun rotateAroundZ(angle: Double): Vector {
        return clone().rotateAroundZ(angle)
    }

    override fun rotateAroundAxis(axis: Vector, angle: Double): Vector {
        return clone().rotateAroundAxis(axis, angle)
    }

    override fun rotateAroundNonUnitAxis(axis: Vector, angle: Double): Vector {
        return clone().rotateAroundNonUnitAxis(axis, angle)
    }

    override fun setX(x: Int): Vector {
        return clone().setX(x)
    }

    override fun setX(x: Double): Vector {
        return clone().setX(x)
    }

    override fun setX(x: Float): Vector {
        return clone().setX(x)
    }

    override fun setY(y: Int): Vector {
        return clone().setY(y)
    }

    override fun setY(y: Double): Vector {
        return clone().setY(y)
    }

    override fun setY(y: Float): Vector {
        return clone().setY(y)
    }

    override fun setZ(z: Int): Vector {
        return clone().setZ(z)
    }

    override fun setZ(z: Double): Vector {
        return clone().setZ(z)
    }

    override fun setZ(z: Float): Vector {
        return clone().setZ(z)
    }
}