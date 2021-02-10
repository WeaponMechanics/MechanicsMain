package me.deecaad.core.utils;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static me.deecaad.core.MechanicsCore.debug;
import static org.bukkit.block.BlockFace.*;

public class VectorUtils {

    // All horizontal block faces
    private static final BlockFace[] AXIS = new BlockFace[]{NORTH, NORTH_NORTH_EAST, NORTH_EAST, EAST_NORTH_EAST, EAST, EAST_SOUTH_EAST, SOUTH_EAST, SOUTH_SOUTH_EAST, SOUTH, SOUTH_SOUTH_WEST, SOUTH_WEST, WEST_SOUTH_WEST, WEST, WEST_NORTH_WEST, NORTH_WEST, NORTH_NORTH_WEST};
    public static final double PI_2 = Math.PI * 2;
    public static final double GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5));

    /**
     * Don't let anyone instantiate this class
     */
    private VectorUtils() {
    }

    /**
     * Gets a random <code>Vector</code> with
     * a given length
     *
     * @return Randomized vector
     */
    public static Vector random(double length) {
        double x = ThreadLocalRandom.current().nextDouble() - ThreadLocalRandom.current().nextDouble();
        double y = ThreadLocalRandom.current().nextDouble() - ThreadLocalRandom.current().nextDouble();
        double z = ThreadLocalRandom.current().nextDouble() - ThreadLocalRandom.current().nextDouble();

        return setLength(new Vector(x, y, z), length);
    }

    /**
     * Set's the length of a given vector
     *
     * @param vector The vector to set the length of
     * @param length The length to set
     * @return The resulting vector
     */
    public static Vector setLength(Vector vector, double length) {
        double m = length / vector.length();
        return vector.multiply(m);
    }

    /**
     * Normalizes an angle given in degrees to an
     * absolute angle. The normalized angle will
     * be [0, 360)
     *
     * @param angle The angle in degrees to normalize
     * @return Normalized angle
     */
    public static double normalize(double angle) {
        return (angle %= 360) >= 0 ? angle : angle + 360;
    }

    /**
     * Normalizes an angle given in radians to an
     * absolute angle. The normalized angle will
     * be [0, 2pi)
     *
     * @param radians The angle in radians to normalize
     * @return Normalized angle
     */
    public static double normalizeRadians(double radians) {
        return (radians %= PI_2) >= 0 ? radians : radians + PI_2;
    }

    /**
     * Shorthand for getting the horizontal face from yaw
     *
     * @param loc The location to pull the yaw from
     * @return The BlockFace associated with the given yaw
     */
    public static BlockFace getHorizontalFace(Location loc) {
        return getHorizontalFace(loc.getYaw());
    }

    /**
     * Gets the <code>BlockFace</code> value of an angle. So
     * if given yaw is facing <code>BlockFace.NORTH</code>, then
     * this will give you that blockface.
     *
     * @param yaw The yaw to get the direction from
     * @return The BlockFace associated with the given yaw
     */
    @Nonnull
    public static BlockFace getHorizontalFace(float yaw) {
        double angle = normalize(yaw);

        int index = (int) Math.round(angle / (360f / AXIS.length));
        try {
            index = Math.min(index, 15);
            return AXIS[index];
        } catch (ArrayIndexOutOfBoundsException ex) {

            // This should never happen. Since the angle is normalized at
            // the beginning of the function, this simply cannot happen.
            // That being said, I make mistakes, so might as well keep this
            debug.log(LogLevel.ERROR, "angle(" + angle + ") got index " + index, ex);
            return null;
        }
    }

    /**
     * Translates the given <code>yaw</code> and <code>pitch</code> into
     * (x, y, z) components (a vector)
     *
     * @param yaw Yaw
     * @param pitch Pitch
     * @return Vector
     */
    public static Vector getVector(double yaw, double pitch) {
        double cosPitch = Math.cos(pitch);

        double x = sin(yaw) * -cosPitch;
        double y = -sin(pitch);
        double z = cos(yaw) * cosPitch;

        return new Vector(x, y, z);
    }

    public static Vector lerp(Vector min, Vector max, double factor) {
        double x = NumberUtils.lerp(min.getX(), max.getX(), factor);
        double y = NumberUtils.lerp(min.getY(), max.getY(), factor);
        double z = NumberUtils.lerp(min.getZ(), max.getZ(), factor);

        return new Vector(x, y, z);
    }

    public static Vector lerp(Vector min, Vector max, double xFactor, double yFactor, double zFactor) {
        double x = NumberUtils.lerp(min.getX(), max.getX(), xFactor);
        double y = NumberUtils.lerp(min.getY(), max.getY(), yFactor);
        double z = NumberUtils.lerp(min.getZ(), max.getZ(), zFactor);

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
    public static Vector min(Vector a, Vector b) {
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
    public static Vector max(@Nonnull Vector a, @Nonnull Vector b) {
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
    public static Vector add(@Nonnull Vector a, double x, double y, double z) {
        a.setX(a.getX() + x);
        a.setY(a.getY() + y);
        a.setZ(a.getZ() + z);
        return a;
    }

    /**
     * Effectively gets a vector perpendicular to the
     * given vector.
     *
     * Examples:
     * (0, 10, 0) -> (0, -0, 10)
     * (10, 0, 10) -> (0, -10, 0)
     *
     * @throws IllegalArgumentException If the given vector's length is 0
     *
     * @param vector The vector to use to get a perpendicular
     * @return The perpendicular method
     */
    public static Vector getPerpendicular(Vector vector) {
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();

        if (NumberUtils.equals(x + y + z, 0.0)) {
            throw new IllegalArgumentException("Vector length cannot be 0");
        }

        if (z != 0.0) {
            return new Vector(y, -x, 0);
        } else if (x != 0.0) {
            return new Vector(0, -z, y);
        } else {
            return new Vector(z, 0, -x);
        }
    }

    /**
     * Gets the angle, in radians, between the 2 given bukkit vectors.
     *
     * @see Math#toDegrees(double)
     *
     * @param a The first vector
     * @param b The second vector
     * @return The angle between the vectors
     */
    public static double getAngleBetween(@Nonnull Vector a, @Nonnull Vector b) {

        double magnitudeA = a.length();
        double magnitudeB = b.length();

        double dot = a.dot(b);
        return Math.acos(dot / (magnitudeA * magnitudeB));
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
        return NumberUtils.equals(vector.getX() + vector.getY() + vector.getZ(), 0.0);
    }
}
