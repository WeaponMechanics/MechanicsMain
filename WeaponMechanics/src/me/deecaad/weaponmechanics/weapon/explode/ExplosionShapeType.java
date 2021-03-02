package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.weaponmechanics.weapon.explode.shapes.CuboidExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.DefaultExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ParabolicExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.SphericalExplosion;

public enum ExplosionShapeType {

    /**
     * Represents a parabolic explosion
     * @see ParabolicExplosion
     */
    PARABOLA,

    /**
     * Represents a spherical explosion
     * @see SphericalExplosion
     */
    SPHERE,

    /**
     * Represents a cuboid explosion
     * @see CuboidExplosion
     */
    CUBE,

    /**
     * Represents a default minecraft generated explosion
     * @see DefaultExplosion
     */
    DEFAULT
}
