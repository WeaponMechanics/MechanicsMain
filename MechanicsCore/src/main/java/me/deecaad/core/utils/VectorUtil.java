package me.deecaad.core.utils;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.bukkit.block.BlockFace.*;

/**
 * This final utility class outlines static methods that operate on or return
 * a {@link Vector}. This class also contains methods that act upon angles.
 */
public final class VectorUtil {

    // All horizontal block faces
    private static final BlockFace[] AXIS = new BlockFace[]{NORTH, NORTH_NORTH_EAST, NORTH_EAST, EAST_NORTH_EAST, EAST, EAST_SOUTH_EAST, SOUTH_EAST, SOUTH_SOUTH_EAST, SOUTH, SOUTH_SOUTH_WEST, SOUTH_WEST, WEST_SOUTH_WEST, WEST, WEST_NORTH_WEST, NORTH_WEST, NORTH_NORTH_WEST};
    public static final double HALF_PI = Math.PI / 2.0;
    public static final double PI_2 = Math.PI * 2.0;
    public static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));

    // Don't let anyone instantiate this class
    private VectorUtil() {
    }

    /**
     * Returns a bukkit {@link Vector} with a {@link Vector#length()} equal to
     * <code>length</code>. The returned vector has randomized components.
     *
     * @param length The non-negative {@link Vector#length()} of the vector.
     * @return The non-null randomized vector.
     */
    @NotNull
    public static Vector random(double length) {
        if (length < 0)
            throw new IllegalArgumentException("length < 0");

        double x = ThreadLocalRandom.current().nextDouble(-1, 1);
        double y = ThreadLocalRandom.current().nextDouble(-1, 1);
        double z = ThreadLocalRandom.current().nextDouble(-1, 1);

        return setLength(new Vector(x, y, z), length);
    }

    /**
     * Sets the {@link Vector#length()} of a vector by normalizing it, then
     * multiplying. The returned vector is a reference to the vector passed in
     * as a parameter.
     *
     * @param vector The non-null bukkit {@link Vector} to set the length of.
     * @param length The non-negative new length for the vector.
     * @return A reference to <code>vector</code>.
     */
    @NotNull
    public static Vector setLength(@NotNull Vector vector, double length) {
        double m = length / vector.length();
        return vector.multiply(m);
    }

    /**
     * Normalizes the given <code>angle</code>, in degrees. Normalizing an
     * angle ensures that the angle is between 0 inclusively, and 360
     * exclusively.
     *
     * @param angle The angle, in degrees, to normalize.
     * @return The normalized angle, <code>[0, 360)</code>.
     */
    public static double normalize(double angle) {
        return (angle %= 360) >= 0 ? angle : angle + 360;
    }

    /**
     * Normalized the given <code>angle</code>, in radians. Normalizing an
     * angle ensures that the angle is between 0 inclusively, and 2pi
     * exclusively.
     *
     * @param radians The angle, in radians, to normalize.
     * @return The normalized angle, <code>[0, 2pi)</code>.
     */
    public static double normalizeRadians(double radians) {
        return (radians %= PI_2) >= 0 ? radians : radians + PI_2;
    }

    /**
     * Returns the horizontal {@link BlockFace} that most closely matches the
     * given <code>yaw</code>.
     *
     * @param yaw The angle to convert to a {@link BlockFace}. Make sure this
     *            angle has been normalized ({@link #normalize(double)}).
     * @return The non-null nearest horizontal block face.
     * @throws IllegalArgumentException If the angle is not normalized.
     */
    @NotNull
    public static BlockFace getHorizontalFace(float yaw) {
        int index = Math.round(yaw / (360f / AXIS.length));
        try {
            index = Math.min(index, 15);
            return AXIS[index];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("yaw needs to be normalized!", ex);
        }
    }

    /**
     * Returns a new {@link Vector} with a horizontal angle of <code>yaw</code>
     * and a vertical angle of <code>pitch</code>. The angles should be given
     * in radians, and do not need to be normalized
     *
     * @param yaw   The horizontal angle of the vector, in radians.
     * @param pitch The vertical angle of the vector, in radians.
     * @return The non-null instantiated vector.
     * @see Math#toRadians(double)
     */
    @NotNull
    public static Vector getVector(double yaw, double pitch) {
        double cosPitch = Math.cos(pitch);

        double x = sin(yaw) * -cosPitch;
        double y = -sin(pitch);
        double z = cos(yaw) * cosPitch;

        return new Vector(x, y, z);
    }

    /**
     * <a href="https://en.wikipedia.org/wiki/Linear_interpolation">Linear interpolation</a>
     * shorthand, using 3 dimensional vectors instead of numbers (1 dimensional
     * vectors). The returned point will be be between the 2 given points.
     *
     * <p>The given points should be minimum and maximum points, respectively,
     * most likely from a bounding box. Use {@link #min(Vector, Vector)} and
     * {@link #max(Vector, Vector)}.
     *
     * @param min    The non-null, minimum bound, inclusive.
     * @param max    The non-null, maximum bound, inclusive.
     * @param factor A number between 0.0 inclusively and 1.0 inclusively.
     *               Values approaching 0.0 will return a point closer to
     *               <code>min</code>, while points closer to 1.0 will return a
     *               point closer to <code>max</code>.
     * @return The non-null, new interpolated vector.
     */
    @NotNull
    public static Vector lerp(@NotNull Vector min, @NotNull Vector max, double factor) {
        double x = NumberUtil.lerp(min.getX(), max.getX(), factor);
        double y = NumberUtil.lerp(min.getY(), max.getY(), factor);
        double z = NumberUtil.lerp(min.getZ(), max.getZ(), factor);

        return new Vector(x, y, z);
    }

    /**
     * <a href="https://en.wikipedia.org/wiki/Linear_interpolation">Linear interpolation</a>
     * shorthand, using 3 dimensional vectors instead of numbers (1 dimensional
     * vectors). The returned point will be be between the 2 given points.
     *
     * <p>The given points should be minimum and maximum points, respectively,
     * most likely from a bounding box. Use {@link #min(Vector, Vector)} and
     * {@link #max(Vector, Vector)}.
     *
     * <p>The factor decides where, relative to <code>min</code> and
     * <code>max</code>, the returned point will be. The factor should be a
     * number between 0 inclusively and 1 inclusively. Values approaching 0.0
     * will return a point closer to <code>min</code>, while values approaching
     * 1.0 will return a point closer to <code>max</code>.
     *
     * <p>The most obvious application for this method is to iterate over the
     * points on an axis aligned bounding box.
     *
     * @param min     The non-null, minimum bound, inclusive.
     * @param max     The non-null, maximum bound, inclusive.
     * @param xFactor The factor to apply to the x dimension.
     * @param yFactor The factor to apply to the y dimension.
     * @param zFactor The factor to apply to the z dimension.
     * @return The non-null, new interpolated vector.
     */
    @NotNull
    public static Vector lerp(@NotNull Vector min, @NotNull Vector max, double xFactor, double yFactor, double zFactor) {
        double x = NumberUtil.lerp(min.getX(), max.getX(), xFactor);
        double y = NumberUtil.lerp(min.getY(), max.getY(), yFactor);
        double z = NumberUtil.lerp(min.getZ(), max.getZ(), zFactor);

        return new Vector(x, y, z);
    }

    /**
     * Gets the minimum vector of the bounding box with corners <code>a</code>
     * and <code>b</code>. Note that the returned vector will never be ==, and
     * is not always .equal to either of the given vectors.
     *
     * @param a The first corner of the box
     * @param b The second corner of the box
     * @return The minimum vector
     */
    @NotNull
    public static Vector min(@NotNull Vector a, @NotNull Vector b) {
        return new Vector(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
    }

    /**
     * Gets the maximum vector of the bounding box with corners <code>a</code>
     * and <code>b</code>. Note that the returned vector will never be ==, and
     * is not always .equal to either of the given vectors.
     *
     * @param a The first corner of the box
     * @param b The second corner of the box
     * @return The maximum vector
     */
    public static Vector max(@NotNull Vector a, @NotNull Vector b) {
        return new Vector(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }

    /**
     * Shorthand for adding values to a vector without instantiating a new vector
     *
     * @param a The vector to add to
     * @param x The x value to add
     * @param y The y value to add
     * @param z The z value to add
     */
    public static Vector add(@NotNull Vector a, double x, double y, double z) {
        a.setX(a.getX() + x);
        a.setY(a.getY() + y);
        a.setZ(a.getZ() + z);
        return a;
    }

    /**
     * Returns a {@link Vector} perpendicular to the given
     *
     * @param vector The vector to use to get a perpendicular
     * @return The perpendicular method
     * @throws IllegalArgumentException If the given vector's length is 0
     */
    @NotNull
    public static Vector getPerpendicular(@NotNull Vector vector) {
        if (isEmpty(vector))
            throw new IllegalArgumentException("Vector length cannot be 0");

        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();

        if (!NumberUtil.equals(y, 0.0))
            return new Vector(z, 0, -x);
        else if (!NumberUtil.equals(x, 0.0))
            return new Vector(0, -z, y);
        else
            return new Vector(y, -x, 0);
    }

    /**
     * Gets the angle, in radians, between the 2 given bukkit vectors.
     *
     * @param a The first vector
     * @param b The second vector
     * @return The angle between the vectors
     * @see Math#toDegrees(double)
     */
    public static double getAngleBetween(@NotNull Vector a, @NotNull Vector b) {

        // ALGEBRA: sqrt(a) * sqrt(b) = sqrt(a * b)
        double denominator = Math.sqrt(a.lengthSquared() * b.lengthSquared());
        if (NumberUtil.equals(denominator, 0.0))
            return 0f;

        double dot = NumberUtil.minMax(-1.0, a.dot(b) / denominator, 1.0);

        // This Math.min is requires for parallel vectors, as floating point
        // issues often cause numbers like 1.002
        return Math.acos(dot);
    }

    /**
     * Returns <code>true</code> if the given <code>vector</code> has a length of
     * 0. This method doesn't use any natively implemented method, making it faster
     * than checking the length manually.
     *
     * @param vector The vector to check
     * @return true if the vector has no magnitude
     */
    public static boolean isEmpty(Vector vector) {
        return NumberUtil.equals(vector.getX(), 0.0) &&
                NumberUtil.equals(vector.getY(), 0.0) &&
                NumberUtil.equals(vector.getZ(), 0.0);
    }
}
