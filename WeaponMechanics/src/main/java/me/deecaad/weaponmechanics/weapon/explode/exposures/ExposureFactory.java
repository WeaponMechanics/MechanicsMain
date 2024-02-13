package me.deecaad.weaponmechanics.weapon.explode.exposures;

import me.deecaad.weaponmechanics.utils.Factory;

/**
 * Factory class to provide explosion exposures to the
 * {@link me.deecaad.weaponmechanics.weapon.explode.Explosion}'s serializer. In order to add your
 * own exposure, use {@link #set(String, Factory.Arguments)}.
 */
public final class ExposureFactory extends Factory<ExplosionExposure> {

    private static final ExposureFactory INSTANCE;

    static {
        INSTANCE = new ExposureFactory();

        INSTANCE.set("DEFAULT", INSTANCE.new Arguments(
            DefaultExposure.class,
            new String[]{},
            new Class[]{}));

        INSTANCE.set("DISTANCE", INSTANCE.new Arguments(
            DistanceExposure.class,
            new String[]{},
            new Class[]{}));

        INSTANCE.set("OPTIMIZED", INSTANCE.new Arguments(
            OptimizedExposure.class,
            new String[]{},
            new Class[]{}));

        INSTANCE.set("VOID,NONE", INSTANCE.new Arguments(
            VoidExposure.class,
            new String[]{},
            new Class[]{}));
    }

    private ExposureFactory() {
        super(ExplosionExposure.class);
    }

    public static ExposureFactory getInstance() {
        return INSTANCE;
    }
}