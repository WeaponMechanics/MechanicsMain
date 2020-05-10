package me.deecaad.core.utils;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;

import static org.bukkit.block.BlockFace.*;
import static org.bukkit.block.BlockFace.NORTH_NORTH_WEST;

import static me.deecaad.core.MechanicsCore.debug;

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
     * a length of 1
     *
     * @return Randomized vector
     */
    public static Vector random(double length) {
        double x = NumberUtils.random().nextDouble() - NumberUtils.random().nextDouble();
        double y = NumberUtils.random().nextDouble() - NumberUtils.random().nextDouble();
        double z = NumberUtils.random().nextDouble() - NumberUtils.random().nextDouble();

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
        } else {
            return new Vector(0, -z, y);
        }
    }

    /**
     * Gets a vector half the length of the given
     * "main" Vector
     *
     * @param vector Vector to get the midpoint of
     * @return Midpoint of the vector
     */
    public static Vector[] splitMidpoint(Vector vector) {
        Vector start = vector.clone().multiply(1 / 2.0);
        Vector stop = vector.clone().subtract(start);
        return new Vector[]{start, stop};
    }

    /**
     * Gets the midpoint of a vector with randomness,
     * based on how much <code>noise</code> is given
     *
     * @param vector Vector to get the midpoint of
     * @param noise How much randomness to add
     * @return Midpoint of the vector
     */
    public static Vector[] splitMidPoint(Vector vector, double noise) {
        if (noise < 0) throw new IllegalArgumentException("Noise must be positive!");
        else if (noise != 0.0) noise = NumberUtils.random(-noise, noise);

        Vector start = vector.clone().multiply(1.0 / (2.0 + noise));
        Vector stop = vector.clone().subtract(start);
        return new Vector[]{start, stop};
    }
}
