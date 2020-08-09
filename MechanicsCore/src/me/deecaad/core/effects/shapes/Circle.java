package me.deecaad.core.effects.shapes;

import me.deecaad.core.mechanics.serialization.SerializerData;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.core.utils.VectorUtils;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

@SerializerData(name = "circle", args = {"points~INTEGER", "radius~DOUBLE~r", "startAngle~DOUBLE~angle"})
public class Circle extends Shape {

    private Point[] points;
    private Vector b;
    private Vector c;
    private double offset;
    private double amplitude;

    /**
     * Default constructor for serializer
     */
    public Circle() {
    }

    /**
     * Constructs a circle with the given number of points and
     * the given amplitude (which is basically radius)
     *
     * @param points The number of points to track on a circle
     * @param amplitude How big of a circle to draw
     */
    public Circle(int points, double amplitude) {
        this(points, amplitude, 0);
    }

    /**
     * Constructs a circle with the given number of points and
     * the given amplitude (which is basically radius)
     *
     * @param points The number of points to track on a circle
     * @param amplitude How big of a circle to draw
     * @param startAngle The angle, in radians, to start at
     */
    public Circle(int points, double amplitude, double startAngle) {

        this.points = new Point[points];
        double period = VectorUtils.PI_2;

        for (int i = 0; i < points; i++) {
            double radian = VectorUtils.normalizeRadians(period / points * i + startAngle);
            double cos = amplitude * Math.cos(radian);
            double sin = amplitude * Math.sin(radian);
            this.points[i] = new Point(sin, cos);
        }

        this.offset = startAngle;
        this.amplitude = amplitude;
    }

    /**
     * Returns the number of points on this <code>Circle</code>
     *
     * @return Number of points
     */
    public int getPoints() {
        return points.length;
    }

    /**
     * Returns the starting angle (or offset)
     *
     * @return The angle this circle started at
     */
    public double getOffset() {
        return offset;
    }

    /**
     * Gets the amplitude used to create this
     * circle's points
     *
     * @return This circle's amplitude
     */
    public double getAmplitude() {
        return amplitude;
    }

    /**
     * Sets the axis to draw the circle on.
     * The circle is formed on the plane formed by
     * the b and c vectors. Since b and c are
     * perpendicular to a, the circle is formed
     * around vector a.
     *
     * @param dir Vector to draw circle around
     */
    @Override
    public void setAxis(Vector dir) {
        axis = dir.clone().normalize();

        // Getting 2 perpendicular Vectors
        b = VectorUtils.getPerpendicular(axis).normalize();
        c = axis.clone().crossProduct(b).normalize();

        // I do this check to avoid wasting resource to get the
        // dot product of these vectors.
        if (debug.canLog(LogLevel.DEBUG)) {
            double dot1 = axis.dot(b);
            double dot2 = b.dot(c);
            double dot3 = axis.dot(c);

            debug.validate(NumberUtils.equals(dot1, 0.0), "A is not perpendicular to B");
            debug.validate(NumberUtils.equals(dot2, 0.0), "B is not perpendicular to C");
            debug.validate(NumberUtils.equals(dot3, 0.0), "A is not perpendicular to C");
        }
    }

    @Override
    public Iterator<Vector> iterator() {
        return new CircleIterator(points, b, c);
    }

    @Override
    public Shape serialize(Map<String, Object> data) {

        int points = (int) data.getOrDefault("points", 16);
        double radius = (double) data.getOrDefault("radius", 1.0);
        double startAngle = (double) data.getOrDefault("startAngle", 0.0);

        debug.validate(points < 1, "Points must be >= 1! Found: " + points);
        debug.validate(radius < 0, "Radius must be positive! Found: " + radius);

        this.points = new Point[points];
        double period = VectorUtils.PI_2;

        for (int i = 0; i < points; i++) {
            double radian = VectorUtils.normalizeRadians(period / points * i + startAngle);
            double cos = amplitude * Math.cos(radian);
            double sin = amplitude * Math.sin(radian);
            this.points[i] = new Point(sin, cos);
        }

        this.offset = startAngle;
        this.amplitude = radius;
        return this;
    }

    private static class Point {
        
        private double sin;
        private double cos;
        
        private Point(double sin, double cos) {
            this.sin = sin;
            this.cos = cos;
        }
    }

    private static class CircleIterator implements Iterator<Vector> {

        private int index;
        private Point[] points;
        private Vector b;
        private Vector c;

        private CircleIterator(Point[] points, Vector b, Vector c) {
            this.points = points;
            this.b = b;
            this.c = c;
        }

        @Override
        public boolean hasNext() {
            return index + 1 < points.length;
        }

        @Override
        public Vector next() {
            Point current = points[index++];

            double x = current.cos * b.getX() + current.sin * c.getX();
            double y = current.cos * b.getY() + current.sin * c.getY();
            double z = current.cos * b.getZ() + current.sin * c.getZ();

            return new Vector(x, y, z);
        }
    }
}
