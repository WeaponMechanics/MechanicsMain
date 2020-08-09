package me.deecaad.core.effects.shapes;

import com.google.common.collect.Iterators;
import me.deecaad.core.mechanics.serialization.Argument;
import me.deecaad.core.mechanics.serialization.SerializerData;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.core.utils.VectorUtils;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SerializerData(name = "lightning", args = {"distanceBetweenPoints~DOUBLE~space", "generations~INTEGER", "branchChance~DOUBLE"})
public class Lightning extends Shape {

    private static final double DISTANCE_BETWEEN_POINTS = 0.3;

    private List<Line> lines;
    private int generations = 5;

    public Lightning() {
        lines = new ArrayList<>();
    }

    @Override
    public void setAxis(Vector vector) {
        axis = vector;
        lines.clear();

        // Setup the main lightning bolt map
        Map<Vector, Vector> vectors = new HashMap<>();
        vectors.put(new Vector(), vector);

        for (int i = 0; i < generations; i++) {
            vectors = nextGeneration(vectors);
        }

        for (Map.Entry<Vector, Vector> entry: vectors.entrySet()) {
            Vector offset = entry.getKey();
            Vector path = entry.getValue();

            double length = path.length();
            Line line = new Line(Math.max(2, (int) Math.round(length / DISTANCE_BETWEEN_POINTS)));
            line.setAxis(path);
            line.setOffset(offset);
            lines.add(line);
        }
    }

    public Map<Vector, Vector> nextGeneration(Map<Vector, Vector> vectors) {
        Map<Vector, Vector> temp = new HashMap<>();

        // Avoiding lambda expressions for debugging
        for (Map.Entry<Vector, Vector> entry: vectors.entrySet()) {
            Vector offset = entry.getKey();
            Vector vector = entry.getValue();

            double length = vector.length();
            Vector midpoint = vector.clone().multiply(0.5);
            Vector noise = VectorUtils.random(length / 5);

            // Get the "split" Vectors
            midpoint.add(noise);
            Vector second = vector.clone().subtract(midpoint);
            Vector midPointOffset = offset.clone().add(midpoint);

            // 33% chance to branch out
            if (NumberUtils.chance(0.333333)) {

                // A bit of randomization
                double x = vector.getX() * NumberUtils.random(0.5, 1.0);
                double y = vector.getY() * NumberUtils.random(0.5, 1.0);
                double z = vector.getZ() * NumberUtils.random(0.5, 1.0);
                temp.put(offset, VectorUtils.setLength(new Vector(x, y, z), length / 2));
            }

            temp.put(offset, midpoint);
            temp.put(midPointOffset, second);
        }

        return temp;
    }


//        double length = vector.length();
//        int points = (int) (length / (DISTANCE_BETWEEN_POINTS / 2.0));
//
//        // Figure out which points to "zig-zag" to
//        TreeSet<Integer> set = new TreeSet<>();
//
//        // todo add infinite loop checker
//        while (set.size() < length - 1) { // WE WANT 1 LESS POINT SO WE CAN RECONNECT TO THE END OF THE VECTOR
//            int rand = NumberUtils.random(minZigZagDifference, points - minZigZagDifference);
//            Integer floor = set.floor(rand); // Unboxing may cause NPE
//            Integer ceiling = set.ceiling(rand);
//            if ((floor == null || rand - floor > minZigZagDifference) && (ceiling == null || ceiling - rand > minZigZagDifference)) {
//                set.add(rand);
//            }
//        }
//
//        Vector offset = new Vector();
//        int lastPoint = 0;
//        for (Integer i : set) {
//            Vector branch = VectorUtils.setLength(vector.clone(), (i - lastPoint) * DISTANCE_BETWEEN_POINTS);
//            Vector diagonal = getRandomRelativeTo(branch, branch.length());
//            Line line = new Line(i - lastPoint);
//            line.setAxis(diagonal);
//            line.setOffset(offset.clone());
//            lines.add(line);
//
//            // Set the new offset and point
//            offset.add(diagonal);
//            lastPoint = i;
//
//            int newBranches = NumberUtils.random(0, 2);
//            if (newBranches != 0) newBranches = NumberUtils.random(0, 2);
//            branch(diagonal, newBranches, 0, (int) Math.round(length - diagonal.length() - offset.length()), offset);
//        }
//
//        // Reconnect to the end of the axis
//        Line line = new Line(points - lastPoint);
//        line.setAxis(vector.clone().subtract(offset));
//        line.setOffset(offset);
//        lines.add(line);
//    }
//
//    public void branch(Vector parent, int branches, int depth, int maxDepth, Vector offset) {
//        if (depth == maxDepth) {
//            return;
//        }
//        double length = parent.length();
//
//        for (int i = 0; i < branches; i++) {
//            Vector random = Vector.getRandom();
//            Vector perpendicular = random.crossProduct(parent);
//
//            // Make the size of the vector relative to the parent, and
//            // make vectors smaller the further from the origin they get
//            VectorUtils.setLength(perpendicular, NumberUtils.random(-length, length) / (depth + 1));
//
//            Vector branch = perpendicular.add(parent);
//            int points = (int) (branch.length() / DISTANCE_BETWEEN_POINTS);
//            Line line = new Line(points);
//            line.setAxis(branch);
//            line.setOffset(offset);
//            lines.add(line);
//
//            int newBranches = NumberUtils.random(0, 2);
//            if (newBranches != 0) newBranches = NumberUtils.random(0, 2);
//            branch(branch, newBranches, depth + 1, maxDepth, offset.clone().add(branch));
//        }
//    }
//
//    private static Vector getRandomRelativeTo(Vector relative, double length) {
//
//        // Get a random vector, then make it perpendicular
//        // to the original Vector
//        Vector random = Vector.getRandom();
//        random.crossProduct(relative);
//
//        VectorUtils.setLength(random, NumberUtils.random(-length, length));
//        return random.add(relative);
//    }

    @Override
    public Iterator<Vector> iterator() {
        return Iterators.concat(lines.stream().map(Shape::iterator).iterator());
    }

    @Override
    public Shape serialize(Map<String, Object> data) {
        return null;
    }
}
