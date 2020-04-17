package me.deecaad.core.utils;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import static org.bukkit.block.BlockFace.*;
import static org.bukkit.block.BlockFace.NORTH_NORTH_WEST;

public class VectorUtils {

    // All horizontal block faces
    private static final BlockFace[] AXIS = new BlockFace[]{NORTH, NORTH_NORTH_EAST, NORTH_EAST, EAST_NORTH_EAST, EAST, EAST_SOUTH_EAST, SOUTH_EAST, SOUTH_SOUTH_EAST, SOUTH, SOUTH_SOUTH_WEST, SOUTH_WEST, WEST_SOUTH_WEST, WEST, WEST_NORTH_WEST, NORTH_WEST, NORTH_NORTH_WEST};
    public static final double PI_2 = Math.PI * 2;

    /**
     * Don't let anyone instantiate this class
     */
    private VectorUtils() {
    }

    public static Vector random() {
        return new Vector(Math.random(), Math.random(), Math.random());
    }

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

    public static BlockFace getHorizontalFace(Location loc) {
        return getHorizontalFace(loc.getYaw());
    }

    public static BlockFace getHorizontalFace(float yaw) {
        double angle = normalize(yaw);

        int index = (int) Math.round(angle / (360f / AXIS.length));
        try {
            return AXIS[index];
        } catch (ArrayIndexOutOfBoundsException ex) {
            DebugUtil.log(LogLevel.ERROR, "angle(" + angle + ") got index " + index + " (" + VectorUtils.class.getName() + ")");
            throw new IllegalArgumentException("Index should be in bounds");
        }
    }

    /**
     * Effectively gets a vector parallel to the given
     * vector.
     *
     * Examples:
     * (0, 10, 0) -> ()
     * (10, 0, 0) -> ()
     *
     * @param vector
     * @return
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

    //public static Vector getPerpendicular(Vector vector) {
    //    double x = vector.getX();
    //    double y = vector.getY();
    //    double z = vector.getZ();
    //    double scale = Math.abs(x) + Math.abs(y) + Math.abs(z);
    //
    //    // There is nothing parallel to to a Vector with
    //    // a length of 0
    //    if (scale == 0.0) {
    //        return new Vector(0, 0, 0);
    //    }
    //
    //    x /= scale;
    //    y /= scale;
    //    z /= scale;
    //
    //    if (Math.abs(x) > Math.abs(y)) {
    //        return new Vector(z, 0 )
    //    }
    //
    //}
}
