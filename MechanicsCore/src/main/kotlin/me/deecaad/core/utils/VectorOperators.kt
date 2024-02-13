package me.deecaad.core.utils

import org.bukkit.util.Vector

// override the + operator
operator fun Vector.plus(other: Vector): Vector {
    return this.clone().add(other)
}

// override the += operator
operator fun Vector.plusAssign(other: Vector) {
    this.add(other)
}

// override the - operator
operator fun Vector.minus(other: Vector): Vector {
    return this.clone().subtract(other)
}

// override the -= operator
operator fun Vector.minusAssign(other: Vector) {
    this.subtract(other)
}

// override the + (sign) operator
operator fun Vector.unaryPlus(): Vector {
    return this.clone()
}

// override the - (sign) operator
operator fun Vector.unaryMinus(): Vector {
    return this.clone().multiply(-1)
}

// override the * operator for scalar multiplication
operator fun Vector.times(other: Int): Vector {
    return this.clone().multiply(other)
}

// override the * operator for scalar multiplication
operator fun Vector.times(other: Double): Vector {
    return this.clone().multiply(other)
}

// override the * operator for piecewise multiplication
operator fun Vector.times(other: Vector): Vector {
    return this.clone().multiply(other)
}

// override the *= operator
operator fun Vector.timesAssign(other: Vector) {
    this.multiply(other)
}

// override the / operator
operator fun Vector.div(other: Vector): Vector {
    return this.clone().divide(other)
}

// override the /= operator
operator fun Vector.divAssign(other: Vector) {
    this.divide(other)
}
