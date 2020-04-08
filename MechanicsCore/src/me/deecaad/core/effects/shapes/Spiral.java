package me.deecaad.core.effects.shapes;

import org.bukkit.util.Vector;

public class Spiral extends Circle {

    private Vector around;
    private int loops;
    private int counter;

    public Spiral(int points, double amplitude, Vector around, int loops) {
        super(points, amplitude);

        this.loops = loops;
        setAxis(around);
    }

    @Override
    public void setAxis(Vector dir) {
        super.setAxis(dir.clone());

        around = dir.multiply(1.0 / (loops * getPoints()));
    }

    @Override
    public boolean hasNext() {
        return nextPoint + counter * getPoints() + 1 < getPoints() * loops;
    }

    @Override
    public Vector next() {
        if (nextPoint == getPoints()) {
            counter++;
            nextPoint = 0;
        }

        return super.next().add(around.clone().multiply(nextPoint + counter * getPoints()));
    }

    /**
     * This is bad. Fix this. Stop being bad
     */
    @Override
    public void reset() {
        nextPoint = 0;
        counter = 0;
    }
}
