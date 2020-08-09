package me.deecaad.core.effects.shapes;

import me.deecaad.core.mechanics.serialization.SerializerData;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.Map;

@SerializerData(name = "spiral", args = {"points~INTEGER", "radius~DOUBLE~r", "offset~DOUBLE~angle", "loops~INTEGER"})
public class Spiral extends Shape {

    private Circle circle;
    private Vector step;
    private int totalPoints;

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

    @Override
    public Shape serialize(Map<String, Object> data) {
        return null;
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
