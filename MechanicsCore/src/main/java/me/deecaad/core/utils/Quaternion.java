package me.deecaad.core.utils;

import org.bukkit.util.Vector;

import java.util.Objects;

public class Quaternion implements Cloneable {

    public static final double EPSILON = 1.11e-16;
    static final Vector UP = new Vector(0, 1, 0);
    static final Vector DOWN = new Vector(0, -1, 0);
    static final Vector LEFT = new Vector(1, 0, 0);
    static final Vector RIGHT = new Vector(-1, 0, 0);
    static final Vector FORWARD = new Vector(0, 0, 1);
    static final Vector BACKWARD = new Vector(0, 0, -1);

    private double x;
    private double y;
    private double z;
    private double w;

    private Quaternion() {
    }

    private Quaternion(Quaternion quaternion) {
        this.x = quaternion.x;
        this.y = quaternion.y;
        this.z = quaternion.z;
        this.w = quaternion.w;
    }

    private Quaternion(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Get the Euler Angles (in radians). You can get the yaw, pitch, and roll by accessing the returned
     * vector's x, y, and z components respectively. In general, converting to euler angles from
     * quaternions (and vice versa) is a slow operation, and should be avoided if possible.
     *
     * @return The yaw, pitch, and roll, respectively.
     * @see <a href=
     *      "http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm">Euler
     *      Angles</a>
     */
    public Vector getEulerAngles() {
        Vector temp = new Vector();

        double sqw = w * w;
        double sqx = x * x;
        double sqy = y * y;
        double sqz = z * z;
        double unit = sqx + sqy + sqz + sqw;
        double test = x * y + z * w;
        if (test > 0.499 * unit) { // singularity at north pole
            temp.setY(2 * Math.atan2(x, w));
            temp.setZ(NumberUtil.HALF_PI);
            temp.setX(0);
        } else if (test < -0.499 * unit) { // singularity at south pole
            temp.setY(-2 * Math.atan2(x, w));
            temp.setZ(-NumberUtil.HALF_PI);
            temp.setX(0);
        } else {
            temp.setY(Math.atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw));
            temp.setZ(Math.asin(2 * test / unit));
            temp.setX(Math.atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw));
        }
        return temp;
    }

    /**
     * Set the Euler Angles (in radians). angles.x is yaw, angles.y is pitch, and angles.z is roll. In
     * general, converting from euler angles to quaternions (and vice versa) is a slow operation, and
     * should be avoided if possible.
     *
     * @param angles The yaw, pitch, and roll, respectively.
     * @return A non-null reference to this (builder pattern)
     * @see <a href=
     *      "http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm">Euler
     *      Angles</a>
     */
    public Quaternion setEulerAngles(Vector angles) {
        double yaw = angles.getX();
        double pitch = angles.getY();
        double roll = angles.getZ();

        double angle;
        double sinRoll, sinPitch, sinYaw, cosRoll, cosPitch, cosYaw;
        angle = pitch / 2.0;
        sinPitch = Math.sin(angle);
        cosPitch = Math.cos(angle);
        angle = roll / 2.0;
        sinRoll = Math.sin(angle);
        cosRoll = Math.cos(angle);
        angle = yaw / 2.0;
        sinYaw = Math.sin(angle);
        cosYaw = Math.cos(angle);

        // variables used to reduce multiplication calls.
        double cosRollXcosPitch = cosRoll * cosPitch;
        double sinRollXsinPitch = sinRoll * sinPitch;
        double cosRollXsinPitch = cosRoll * sinPitch;
        double sinRollXcosPitch = sinRoll * cosPitch;

        w = (cosRollXcosPitch * cosYaw - sinRollXsinPitch * sinYaw);
        x = (cosRollXcosPitch * sinYaw + sinRollXsinPitch * cosYaw);
        y = (sinRollXcosPitch * cosYaw + cosRollXsinPitch * sinYaw);
        z = (cosRollXsinPitch * cosYaw - sinRollXcosPitch * sinYaw);

        normalize();
        return this;
    }

    /**
     * Interpolates this quaternion with the given When t=0, this quaternion is returned. When t is 0,
     * the given quaternion is returned. See {@link NumberUtil#lerp(double, double, double)} for more
     * information on interpolation.
     *
     * @param other The other
     * @param t A number, will be clamped 0..1
     * @return A non-null reference to this (builder pattern).
     */
    public Quaternion lerp(Quaternion other, double t) {
        t = NumberUtil.clamp01(t);

        // If the 2 quaternions are equal, then don't do any math.
        if (equals(other))
            return this;

        double dot = dot(other);

        if (dot < 0.0) {
            // todo math
        }
        return this;
    }

    public Quaternion multiply(Quaternion other) {
        double a = x * other.w + y * other.z - z * other.y + w * other.x;
        double b = -x * other.z + y * other.w + z * other.x + w * other.y;
        double c = x * other.y - y * other.x + z * other.w + w * other.z;
        double d = -x * other.x - y * other.y - z * other.z + w * other.w;

        x = a;
        y = b;
        z = c;
        w = d;

        return this;
    }

    public Vector multiply(Vector vector) {
        double tempX = w * w * vector.getX() + 2 * y * w * vector.getZ() - 2 * z * w * vector.getY() + x * x * vector.getX()
            + 2 * y * x * vector.getY() + 2 * z * x * vector.getZ() - z * z * vector.getX() - y * y * vector.getX();
        double tempY = 2 * x * y * vector.getX() + y * y * vector.getY() + 2 * z * y * vector.getZ() + 2 * w * z
            * vector.getX() - z * z * vector.getY() + w * w * vector.getY() - 2 * x * w * vector.getZ() - x * x
                * vector.getY();
        double tempZ = 2 * x * z * vector.getX() + 2 * y * z * vector.getY() + z * z * vector.getZ() - 2 * w * y * vector.getX()
            - y * y * vector.getZ() + 2 * w * x * vector.getY() - x * x * vector.getZ() + w * w * vector.getZ();
        return new Vector(tempX, tempY, tempZ);
    }

    public double dot(Quaternion other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    public Quaternion inverse() {
        double length = dot(this);
        double inverse = 1.0 / length;
        x *= -inverse;
        y *= -inverse;
        z *= -inverse;
        w *= inverse;

        return this;
    }

    public Quaternion normalize() {
        double factor = Math.sqrt(dot(this));

        if (factor < EPSILON)
            throw new IllegalArgumentException("Divide by 0");

        x /= factor;
        y /= factor;
        z /= factor;
        w /= factor;
        return this;
    }

    @Override
    protected Quaternion clone() {
        return new Quaternion(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Quaternion that = (Quaternion) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0
            && Double.compare(that.z, z) == 0 && Double.compare(that.w, w) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, w);
    }

    /**
     * Returns <code>true</code> when the "length" of the quaternion is zero. Generally, you need a unit
     * quaternion in order to do math. Use {@link #normalize()} to make this quaternion a unit
     *
     * @return true if this is a unit
     */
    public boolean isUnit() {
        return dot(this) < EPSILON;
    }

    /**
     * Returns <code>true</code> when the scalar component (<code>w</code>) is zero. Pure quaternions
     * usually hold a vector.
     *
     * @return true if this is a pure
     */
    public boolean isPure() {
        return w < EPSILON;
    }

    /**
     * Returns the identity quaternion, which is a quaternion with 0 rotation.
     *
     * @return A copy of the identity
     */
    public static Quaternion identity() {
        return new Quaternion(0, 0, 0, 1);
    }

    /**
     * Creates a new quaternion looking in the given direction.
     *
     * @param direction Normalized direction vector.
     * @param up The local up {@link Transform#getUp()}
     * @return A new quaternion looking in the direction.
     */
    public static Quaternion lookAt(Vector direction, Vector up) {
        Vector x = up.getCrossProduct(direction).normalize();
        Vector y = direction.getCrossProduct(x).normalize();

        return fromAxis(x, y, direction);
    }

    public static Quaternion fromAxis(Vector right, Vector up, Vector forward) {
        double m00 = right.getX();
        double m01 = right.getY();
        double m02 = right.getZ();
        double m10 = up.getX();
        double m11 = up.getY();
        double m12 = up.getZ();
        double m20 = forward.getX();
        double m21 = forward.getY();
        double m22 = forward.getZ();

        // New quaternion values
        double x, y, z, w;

        double num8 = (m00 + m11) + m22;
        if (num8 > 0f) {
            double num = Math.sqrt(num8 + 1f);
            w = num * 0.5f;
            num = 0.5f / num;
            x = (m12 - m21) * num;
            y = (m20 - m02) * num;
            z = (m01 - m10) * num;
        } else if ((m00 >= m11) && (m00 >= m22)) {
            double num7 = Math.sqrt(((1f + m00) - m11) - m22);
            double num4 = 0.5f / num7;
            x = 0.5f * num7;
            y = (m01 + m10) * num4;
            z = (m02 + m20) * num4;
            w = (m12 - m21) * num4;
        } else if (m11 > m22) {
            double num6 = Math.sqrt(((1f + m11) - m00) - m22);
            double num3 = 0.5f / num6;
            x = (m10 + m01) * num3;
            y = 0.5f * num6;
            z = (m21 + m12) * num3;
            w = (m20 - m02) * num3;
        } else {
            double num5 = Math.sqrt(((1f + m22) - m00) - m11);
            double num2 = 0.5f / num5;
            x = (m20 + m02) * num2;
            y = (m21 + m12) * num2;
            z = 0.5f * num5;
            w = (m01 - m10) * num2;
        }

        return new Quaternion(x, y, z, w);
    }

    /**
     * Creates a quaternion that rotates between the 2 given vectors.
     *
     * @param from From direction.
     * @param to To direction.
     * @return A new quaternion rotation from -> to.
     */
    public static Quaternion fromTo(Vector from, Vector to) {
        from = from.clone();
        Vector axis = from.crossProduct(to);
        double angle = VectorUtil.getAngleBetween(from, to);

        // When the angle is almost 180 degrees, axis becomes 0, 0, 0... and
        // we cannot rotate about that axis. So we try to find an arbitrary
        // perpendicular vector. In the off chance that it's still zero, we try
        // again (The second time, it is impossible for it to still be zero)
        if (VectorUtil.isZero(axis) || angle > 3.1401) {
            Vector arbitrary = from.crossProduct(RIGHT);
            axis = arbitrary.crossProduct(from);
            if (VectorUtil.isZero(axis))
                axis = UP;
        }

        return angleAxis(angle, axis);
    }

    /**
     * Creates a new Quaternion from the given Euler angles.
     *
     * @param angles The yaw, pitch, and roll stored in x, y, and z respectively.
     * @return A new
     * @see #setEulerAngles(Vector)
     */
    public static Quaternion fromEuler(Vector angles) {
        return new Quaternion().setEulerAngles(angles);
    }

    /**
     * Creates a new Quaternion from the given axis and rotation.
     *
     * @param axis The normalized axis.
     * @param angle The angle, in radians, to rotate.
     * @return A non-null reference to this (builder pattern).
     */
    public static Quaternion angleAxis(double angle, Vector axis) {
        if (VectorUtil.isZero(axis))
            throw new IllegalArgumentException("Divide by zero");

        double s = Math.sin(angle / 2.0);
        double w = Math.cos(angle / 2.0);
        double x = axis.getX() * s;
        double y = axis.getY() * s;
        double z = axis.getZ() * s;
        return new Quaternion(x, y, z, w);
    }
}