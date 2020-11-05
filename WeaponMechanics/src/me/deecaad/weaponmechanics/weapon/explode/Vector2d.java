package me.deecaad.weaponmechanics.weapon.explode;

import org.bukkit.util.NumberConversions;

class Vector2d {

    private final double x;
    private final double z;

    Vector2d(double x, double z) {
        this.x = x;
        this.z = z;
    }

    double distanceSquared(Vector2d vector) {
        return NumberConversions.square(this.x - vector.x) + NumberConversions.square(this.z - vector.z);
    }
}
