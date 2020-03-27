package me.deecaad.weaponmechanics.weapon.shoot.spread;

/**
 * This class encapsulates an (x, y) point
 * with a value.
 */
public class Point {
    
    private double x;
    private double y;
    private int value;
    
    public Point() {}
    
    public Point(double x, double y) {
        this(x, y, 0);
    }
    
    public Point(double x, double y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public int getValue() {
        return value;
    }
}
