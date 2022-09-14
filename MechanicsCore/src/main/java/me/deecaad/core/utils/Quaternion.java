package me.deecaad.core.utils;

import org.bukkit.util.Vector;

import java.util.Objects;

public class Quaternion implements Cloneable {

    public static final double EPSILON = 1.11e-16;
    static final Vector UP = new Vector(0, 1, 0);
    static final Vector DOWN = new Vector(0, -1, 0);
    static final Vector LEFT = new Vector(-1, 0, 0);
    static final Vector RIGHT = new Vector(1, 0, 0);
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
     * Get the Euler Angles (in radians). You can get the yaw, pitch, and roll
     * by accessing the returned vector's x, y, and z components respectively.
     * In general, converting to euler angles from quaternions (and vice versa)
     * is a slow operation, and should be avoided if possible.
     *
     * @return The yaw, pitch, and roll, respectively.
     * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm">Euler Angles</a>
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
            temp.setZ(VectorUtil.HALF_PI);
            temp.setX(0);
        } else if (test < -0.499 * unit) { // singularity at south pole
            temp.setY(-2 * Math.atan2(x, w));
            temp.setZ(-VectorUtil.HALF_PI);
            temp.setX(0);
        } else {
            temp.setY(Math.atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw));
            temp.setZ(Math.asin(2 * test / unit));
            temp.setX(Math.atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw));
        }
        return temp;
    }

    /**
     * Set the Euler Angles (in radians). angles.x is yaw, angles.y is pitch,
     * and angles.z is roll. In general, converting from euler angles to
     * quaternions (and vice versa) is a slow operation, and should be avoided
     * if possible.
     *
     * @param angles The yaw, pitch, and roll, respectively.
     * @return A non-null reference to this (builder pattern)
     * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm">Euler Angles</a>
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
     * Interpolates this quaternion with the given quaternion. When t=0, this
     * quaternion is returned. When t is 0, the given quaternion is returned.
     * See {@link NumberUtil#lerp(double, double, double)} for more
     * information on interpolation.
     *
     * @param other The other quaternion.
     * @param t A number, will be clamped 0..1
     * @return A non-null reference to this (builder pattern).
     */
    public Quaternion lerp(Quaternion other, double t) {
        t = NumberUtil.minMax(0.0, t, 1.0);

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
        return vector.setX(tempX).setY(tempY).setZ(tempZ);
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
    protected Quaternion clone()  {
        return new Quaternion(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quaternion that = (Quaternion) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0
                && Double.compare(that.z, z) == 0 && Double.compare(that.w, w) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, w);
    }

    /**
     * Returns <code>true</code> when the "length" of the quaternion is zero.
     * Generally, you need a unit quaternion in order to do math. Use
     * {@link #normalize()} to make this quaternion a unit quaternion.
     *
     * @return true if this is a unit quaternion.
     */
    public boolean isUnit() {
        return dot(this) < EPSILON;
    }

    /**
     * Returns <code>true</code> when the scalar component (<code>w</code>) is
     * zero. Pure quaternions usually hold a vector.
     *
     * @return true if this is a pure quaternion.
     */
    public boolean isPure() {
        return w < EPSILON;
    }

    /**
     * Returns the identity quaternion, which is a quaternion with 0 rotation.
     *
     * @return A copy of the identity quaternion.
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

    public static Quaternion fromAxis(Vector xAxis, Vector yAxis, Vector zAxis) {
        double t = xAxis.getX() + yAxis.getY() + zAxis.getZ();
        double x, y, z, w;

        // we protect the division by s by ensuring that s>=1
        if (t >= 0) { // |w| >= .5
            double s = Math.sqrt(t + 1); // |s|>=1 ...
            w = 0.5f * s;
            s = 0.5f / s;
            x = (yAxis.getZ() - zAxis.getY()) * s;
            y = (zAxis.getX() - xAxis.getZ()) * s;
            z = (xAxis.getY() - yAxis.getX()) * s;
        } else if ((xAxis.getX() > yAxis.getY()) && (xAxis.getX() > zAxis.getZ())) {
            double s = Math.sqrt(1.0f + xAxis.getX() - yAxis.getY() - zAxis.getZ()); // |s|>=1
            x = s * 0.5f; // |x| >= .5
            s = 0.5f / s;
            y = (xAxis.getY() + yAxis.getX()) * s;
            z = (zAxis.getX() + xAxis.getZ()) * s;
            w = (yAxis.getZ() - zAxis.getY()) * s;
        } else if (yAxis.getY() > zAxis.getZ()) {
            double s = Math.sqrt(1.0f + yAxis.getY() - xAxis.getX() - zAxis.getZ()); // |s|>=1
            y = s * 0.5f; // |y| >= .5
            s = 0.5f / s;
            x = (xAxis.getY() + yAxis.getX()) * s;
            z = (yAxis.getZ() + zAxis.getY()) * s;
            w = (zAxis.getX() - xAxis.getZ()) * s;
        } else {
            double s = Math.sqrt(1.0f + zAxis.getZ() - xAxis.getX() - yAxis.getY()); // |s|>=1
            z = s * 0.5f; // |z| >= .5
            s = 0.5f / s;
            x = (zAxis.getX() + xAxis.getZ()) * s;
            y = (yAxis.getZ() + zAxis.getY()) * s;
            w = (xAxis.getY() - yAxis.getX()) * s;
        }

        return new Quaternion(x, y, z, w);
    }

    /**
     * Creates a quaternion that rotates between the 2 given vectors. Make sure
     * that the given vectors are not opposites. from != -to.
     *
     * @param from From direction.
     * @param to   To direction.
     * @return A new quaternion rotation from -> to.
     */
    public static Quaternion fromTo(Vector from, Vector to) {
        Vector axis = from.crossProduct(to);
        double angle = from.angle(to);
        return angleAxis(angle, axis);
    }

    /**
     * Creates a new Quaternion from the given Euler angles.
     *
     * @param angles The yaw, pitch, and roll stored in x, y, and z respectively.
     * @return A new quaternion.
     * @see #setEulerAngles(Vector)
     */
    public static Quaternion fromEuler(Vector angles) {
        return new Quaternion().setEulerAngles(angles);
    }

    /**
     * Creates a new Quaternion from the given axis and rotation.
     *
     * @param axis  The normalized axis.
     * @param angle The angle, in radians, to rotate.
     * @return A non-null reference to this (builder pattern).
     */
    public static Quaternion angleAxis(double angle, Vector axis) {
        if (VectorUtil.isEmpty(axis))
            throw new IllegalArgumentException("Divide by zero");

        double s = Math.sin(angle / 2.0);
        double w = Math.cos(angle / 2.0);
        double x = axis.getX() * s;
        double y = axis.getY() * s;
        double z = axis.getZ() * s;
        return new Quaternion(x, y, z, w);
    }

}
