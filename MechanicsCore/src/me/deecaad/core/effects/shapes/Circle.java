package me.deecaad.core.effects.shapes;

import me.deecaad.core.utils.NumberUtils;
import org.bukkit.util.Vector;

import java.util.Iterator;

/**
 * TODO shape interface extends Iterator/Iterable of type vector
 */
public class Circle implements Iterator<Vector> {
    
    private Point[] points;
    protected int nextPoint;
    private Vector b;
    private Vector c;
    
    public Circle() {
        this(16, 1);
    }

    /**
     * Constructs a circle with the given number of points and
     * the given amplitude (which is basically radius)
     *
     * @param points The number of points to track on a circle
     * @param amplitude How big of a circle to draw
     */
    public Circle(int points, double amplitude) {
        this.points = new Point[points];
        double period = (2 * Math.PI);
        
        for (int i = 0; i < points; i++) {
            double radian = period / points * i;
            double cos = amplitude * Math.cos(radian);
            double sin = amplitude * Math.sin(radian);
            this.points[i] = new Point(sin, cos);
        }
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
     * Sets the axis with yaw and pitch instead of
     * with a Vector. (Basically just creates the
     * Vector for you)
     *
     * @param yaw Horizontal direction
     * @param pitch Vertical direction
     */
    public void setAxis(double yaw, double pitch) {
        double x = Math.sin(pitch) * Math.cos(yaw);
        double y = Math.sin(pitch) * Math.sin(yaw);
        double z = Math.cos(pitch);
        
        setAxis(new Vector(x, y, z));
    }
    
    /**
     * Sets the axis to draw the circle on. Vectors
     * The circle is formed on the plane formed by
     * the b and c vectors. Since b and c are
     * perpendicular to a (and), the circle is formed
     * around vector a.
     *
     * If what you are drawing a circle around is changing
     * it's yaw and pitch,
     *
     * @param dir Vector to draw circle around
     */
    public void setAxis(Vector dir) {
        Vector a = dir.normalize();

        // This double checks to make sure we do not
        // produce a vector of length 0, which causes
        // issues with NaN during normalization.
        b = new Vector(0, 0, 0);
        while (NumberUtils.equals(b.length(), 0.0)) {
            Vector vector = Vector.getRandom();
            b = a.clone().crossProduct(vector);
        }
        c = a.clone().crossProduct(b.normalize());

        //DebugUtils.assertTrue(a.dot(b) == 0, "A is not perpendicular to B");
        //DebugUtils.assertTrue(b.dot(c) == 0, "B is not perpendicular to C");
        //DebugUtils.assertTrue(a.dot(c) == 0, "A is not perpendicular to C");
    }

    public void reset() {
        nextPoint = 0;
    }

    @Override
    public boolean hasNext() {
        return nextPoint + 1 < points.length;
    }

    @Override
    public Vector next() {
        Point current = points[nextPoint++];

        double x = current.cos * b.getX() + current.sin * c.getX();
        double y = current.cos * b.getY() + current.sin * c.getY();
        double z = current.cos * b.getZ() + current.sin * c.getZ();

        return new Vector(x, y, z);
    }

    private static class Point {
        
        private double sin;
        private double cos;
        
        private Point(double sin, double cos) {
            this.sin = sin;
            this.cos = cos;
        }
    }
}
