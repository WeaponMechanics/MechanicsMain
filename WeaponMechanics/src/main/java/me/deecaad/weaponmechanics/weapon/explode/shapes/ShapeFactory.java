package me.deecaad.weaponmechanics.weapon.explode.shapes;

import me.deecaad.weaponmechanics.weapon.explode.Factory;

public final class ShapeFactory extends Factory<ExplosionShape> {

    private static final ShapeFactory INSTANCE = new ShapeFactory();

    static {
        INSTANCE.set("default", DefaultExplosion.class, "Yield");
        INSTANCE.set("cuboid", CuboidExplosion.class, "Width", "Height");
        INSTANCE.set("parabolic", ParabolicExplosion.class, "Depth", "Angle");
        INSTANCE.set("spherical", SphericalExplosion.class, "Radius");
    }

    private ShapeFactory() {
        super(8);
    }

    public static ShapeFactory getInstance() {
        return INSTANCE;
    }
}
