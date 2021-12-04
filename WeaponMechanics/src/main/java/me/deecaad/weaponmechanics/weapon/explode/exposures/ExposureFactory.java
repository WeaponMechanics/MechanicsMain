package me.deecaad.weaponmechanics.weapon.explode.exposures;

import me.deecaad.weaponmechanics.weapon.explode.Factory;

/**
 * Factory class to provide explosion exposures to the
 * {@link me.deecaad.weaponmechanics.weapon.explode.Explosion}'s serializer.
 * In order to add your own exposure, use {@link #set(String, Class, String...)}.
 */
public final class ExposureFactory extends Factory<ExplosionExposure> {

    private static final ExposureFactory INSTANCE = new ExposureFactory();

    static {
        INSTANCE.set("default", DefaultExposure.class);
        INSTANCE.set("distance", DistanceExposure.class);
        INSTANCE.set("optimized", OptimizedExposure.class);
        INSTANCE.set("void", VoidExposure.class);
    }

    private ExposureFactory() {
        super(4);
    }

    public static ExposureFactory getInstance() {
        return INSTANCE;
    }
}