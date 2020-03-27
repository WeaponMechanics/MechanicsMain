package me.deecaad.weaponmechanics.particles;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Probably better to make a complicated inheritance to easily support a lot of particles. Later
 */
public class Circle {
    
    private Point[] points;
    private int nextPoint;
    private Vector b;
    private Vector c;
    
    public Circle() {
        this(16, 1, 1, 0, 0);
    }
    
    public Circle(int points) {
        this(points, 1, 1, 0, 0);
    }
    
    public Circle(int points, double amplitude, double frequency) {
        this(points, amplitude, frequency, 0, 0);
    }
    
    /**
     * Constructs a circle with high customization. It is easiest
     * to understand each parameter with an understanding of the
     * graphs of sin and cos.
     *
     * @param points The number of points to track on a circle
     * @param amplitude How big of a circle to draw
     * @param frequency How often to repeat the circle
     * @param axis y offset
     * @param phaseShift x offset
     */
    public Circle(int points, double amplitude, double frequency, double axis, double phaseShift) {
        this.points = new Point[points];
        double period = (2 * Math.PI) / frequency;
        
        for (int i = 0; i < points; i++) {
            double radian = period / points * i;
            double cos = amplitude * Math.cos(frequency * (radian - phaseShift)) + axis;
            double sin = amplitude * Math.sin(frequency * (radian - phaseShift)) + axis;
            this.points[i] = new Point(sin, cos);
        }
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
        b = new Vector(a.getY(), -a.getX(), 0).normalize();
        c = a.crossProduct(b);
    }
    
    /**
     * Gets the next point on the circle, and adds
     * that to the given Location. This allows the circle
     * to be drawn in relation to a location, which can change.
     * This makes it easier to draw spirals.
     *
     * If the given location does not change, this will just draw
     * a circle around the location.
     *
     * @param loc Location to draw at
     */
    public void getNext(Location loc) {
        Point current = points[nextPoint];
        
        double x = current.cos * b.getX() + current.sin * c.getX();
        double y = current.cos * b.getY() + current.sin * c.getY();
        double z = current.cos * b.getZ() + current.sin * c.getZ();
        
        nextPoint = (nextPoint + 1) % points.length;
        
        loc.add(x, y, z);
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
