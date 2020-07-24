package me.deecaad.core.effects.shapes;

import org.bukkit.util.Vector;

import java.util.Iterator;

public class Spiral extends Shape {

    private final Circle circle;
    private Vector step;
    private final int totalPoints;

    public Spiral(int points, double amplitude, double offset, Vector around, int loops) {
        this(new Circle(points, amplitude, offset), around, loops);
    }

    public Spiral(Circle circle, Vector around, int loops) {
        this.circle = circle;
        this.totalPoints = loops * circle.getPoints();

        if (around != null) setAxis(around);
    }

    /**
     * Gets the circle that made this spiral
     *
     * @return This spiral's circle
     */
    public Circle getCircle() {
        return circle;
    }

    public Vector getStep() {
        return step.clone();
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    @Override
    public void setAxis(Vector dir) {
        circle.setAxis(dir);

        step = dir.clone().multiply(1.0 / (totalPoints));
        axis = dir;
    }

    @Override
    public Iterator<Vector> iterator() {
        return new SpiralIterator(circle, step, totalPoints);
    }

    private static class SpiralIterator implements Iterator<Vector> {

        private int index;
        private Circle circle;
        private Iterator<Vector> circleIterator;
        private Vector step;
        private int totalPoints;

        public SpiralIterator(Circle circle, Vector step, int totalPoints) {
            this.circle = circle;
            this.circleIterator = circle.iterator();
            this.step = step;
            this.totalPoints = totalPoints;
        }

        @Override
        public boolean hasNext() {
            return index + 1 < totalPoints;
        }

        @Override
        public Vector next() {

            // Remember that a spiral can loop multiple times,
            // or, in other words, draw multiple circles
            if (!circleIterator.hasNext()) {
                circleIterator = circle.iterator();
            }

            return circleIterator.next().add(step.clone().multiply(index++));
        }
    }

}
