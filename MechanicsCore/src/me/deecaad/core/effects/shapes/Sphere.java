package me.deecaad.core.effects.shapes;

import me.deecaad.core.mechanics.serialization.SerializerData;
import me.deecaad.core.utils.VectorUtils;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

@SerializerData(name = "sphere", args = {"radius~DOUBLE~r", "points~INTEGER"})
public class Sphere extends Shape {

    private double radius;
    private ArrayList<Vector> points;

    public Sphere(double radius, int amountPoints) {
        this.radius = radius;
        this.points = new ArrayList<>();

        double phi = VectorUtils.GOLDEN_ANGLE;

        for (int i = 0; i < amountPoints; i++) {
            double y = 1 - (i / ((double) amountPoints - 1)) * 2;
            double r = Math.sqrt(1 - y * y);

            // y *= (radius / r); // Creates a cool diamond like shape

            double theta = phi * i;

            double x = r * Math.cos(theta);
            double z = r * Math.sin(theta);
            points.add(new Vector(x * radius, y * radius, z * radius)); // todo make this an array when finished debugging
        }
    }

    @Override
    public Iterator<Vector> iterator() {
        return points.iterator();
    }

    @Override
    public Shape serialize(Map<String, Object> data) {
        return null;
    }
}
