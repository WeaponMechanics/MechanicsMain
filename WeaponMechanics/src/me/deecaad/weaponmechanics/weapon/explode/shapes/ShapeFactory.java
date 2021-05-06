package me.deecaad.weaponmechanics.weapon.explode.shapes;

import me.deecaad.weaponmechanics.weapon.explode.Factory;

public final class ShapeFactory extends Factory<ExplosionShape> {

    private static final ShapeFactory INSTANCE = new ShapeFactory();

    static {
        INSTANCE.set("default", DefaultExplosion.class, "yield");
        INSTANCE.set("cuboid", CuboidExplosion.class, "width", "height");
        INSTANCE.set("parabolic", ParabolicExplosion.class, "depth", "angle");
        INSTANCE.set("spherical", SphericalExplosion.class, "radius");
    }

    private ShapeFactory() {
        super(4);
    }

    public static ShapeFactory getInstance() {
        return INSTANCE;
    }
}
