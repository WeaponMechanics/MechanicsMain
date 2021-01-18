package temp;

import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class Line extends Shape implements Offsetable {

    private Vector offset;
    private int points;

    public Line(int points) {
        if (points < 2) throw new IllegalArgumentException("Must have at least 2 points on a line");

        this.points = points;
        this.axis = new Vector();
        this.offset = new Vector();
    }

    @Override
    public Vector getOffset() {
        return offset;
    }

    @Override
    public void setOffset(Vector offset) {
        this.offset = offset;
    }

    @Nonnull
    @Override
    public Iterator<Vector> iterator() {
        return new LineIterator(axis, offset, points);
    }

    public static Line between(Vector a, Vector b, int points) {
        Vector vector = a.clone().subtract(b);

        Line line = new Line(points);
        line.setAxis(vector);
        line.setOffset(b);
        return line;
    }

    private static class LineIterator implements Iterator<Vector> {

        private int index;
        private Vector vector;
        private Vector offset;
        private int points;

        private LineIterator(Vector vector, Vector offset, int points) {
            this.vector = vector.clone().multiply(1.0 / (points - 1));
            this.offset = offset;
            this.points = points;
        }

        @Override
        public boolean hasNext() {
            return index + 1 < points;
        }

        @Override
        public Vector next() {
            return vector.clone().multiply(index++).add(offset);
        }
    }
}
