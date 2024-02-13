package me.deecaad.weaponmechanics.weapon.explode.shapes;

import me.deecaad.weaponmechanics.utils.Factory;

public final class ShapeFactory extends Factory<ExplosionShape> {

    private static final ShapeFactory INSTANCE = new ShapeFactory();

    static {
        INSTANCE.set("DEFAULT", INSTANCE.new Arguments(
            DefaultExplosion.class,
            new String[]{"Yield", "Rays"},
            new Class[]{double.class, int.class}));

        INSTANCE.set("CUBE,CUBOID", INSTANCE.new Arguments(
            CuboidExplosion.class,
            new String[]{"Width", "Height"},
            new Class[]{double.class, double.class}));

        INSTANCE.set("PARABOLA,PARABOLIC", INSTANCE.new Arguments(
            ParabolicExplosion.class,
            new String[]{"Depth", "Angle"},
            new Class[]{double.class, double.class}));

        INSTANCE.set("SPHERE,SPHERICAL", INSTANCE.new Arguments(
            SphericalExplosion.class,
            new String[]{"Radius"},
            new Class[]{double.class}));
    }

    private ShapeFactory() {
        super(ExplosionShape.class);
    }

    public static ShapeFactory getInstance() {
        return INSTANCE;
    }
}
