package me.deecaad.core.effects.shapes;

import org.bukkit.util.Vector;

import java.util.Iterator;

public class Line implements Shape {

    private Vector vector;
    private int points; // Number of points to draw

    public Line(int points) {
        this.points = points;
        this.vector = new Vector();
    }


    @Override
    public void setAxis(Vector vector) {
        this.vector = vector;
    }

    @Override
    public boolean isGuessVector() {
        return false;
    }

    @Override
    public Iterator<Vector> iterator() {
        return new LineIterator(vector, points);
    }

    public static Line between(Vector a, Vector b, int points) {
        Vector vector = a.clone().subtract(b);

        Line line = new Line(points);
        line.setAxis(vector);
        return line;
    }

    private static class LineIterator implements Iterator<Vector> {

        private int index;
        private Vector vector;
        private int points;

        private LineIterator(Vector vector, int points) {
            this.vector = vector.clone().multiply(1.0 / points);
            this.points = points;
        }

        @Override
        public boolean hasNext() {
            return index + 1 < points;
        }

        @Override
        public Vector next() {
            return vector.clone().multiply(index++);
        }
    }
}
