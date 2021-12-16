package me.deecaad.weaponmechanics.weapon.explode.shapes;

import me.deecaad.weaponmechanics.weapon.explode.Factory;

public final class ShapeFactory extends Factory<ExplosionShape> {

    private static final ShapeFactory INSTANCE = new ShapeFactory();

    static {
        INSTANCE.set("DEFAULT", INSTANCE.new Arguments(
                DefaultExplosion.class,
                new String[]{ "Yield", "Rays" },
                new Class[]{ double.class, int.class }
        ));

        INSTANCE.set("CUBOID", INSTANCE.new Arguments(
                CuboidExplosion.class,
                new String[]{ "Width", "Height" },
                new Class[]{ double.class, double.class }
        ));

        INSTANCE.set("PARABOLIC", INSTANCE.new Arguments(
                ParabolicExplosion.class,
                new String[]{ "Depth", "Angle" },
                new Class[]{ double.class }
        ));

        INSTANCE.set("SPHERICAL", INSTANCE.new Arguments(
                SphericalExplosion.class,
                new String[]{ "Radius" },
                new Class[]{ double.class }
        ));
    }

    private ShapeFactory() {
        super(8);
    }

    public static ShapeFactory getInstance() {
        return INSTANCE;
    }
}
