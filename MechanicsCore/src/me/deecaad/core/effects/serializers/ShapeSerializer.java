package me.deecaad.core.effects.serializers;

import me.deecaad.core.effects.shapes.Circle;
import me.deecaad.core.effects.shapes.Line;
import me.deecaad.core.effects.shapes.Shape;
import me.deecaad.core.effects.shapes.Sphere;
import me.deecaad.core.effects.shapes.Spiral;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Arrays;

import static me.deecaad.core.MechanicsCore.debug;

public class ShapeSerializer implements Serializer<Shape> {

    /**
     * Default constructor for serializer
     */
    public ShapeSerializer() {
    }

    @Override
    public String getKeyword() {
        return "Shape";
    }

    @Override
    public Shape serialize(File file, ConfigurationSection configurationSection, String path) {
        ConfigurationSection config = configurationSection.getConfigurationSection(path);

        Shape shape;
        Vector axis;
        double radius = config.getDouble("Radius", 1);
        int points = config.getInt("Points", 16);
        int loops = config.getInt("Spiral_Loops", 1);

        String direction = config.getString("Axis.Direction").toUpperCase();
        switch (direction) {
            case "GUESS":
                axis = new Vector();
                break;
            case "RANDOM":
                axis = null;
                break;
            default:
                try {
                    axis = BlockFace.valueOf(direction).getDirection();
                } catch (EnumConstantNotPresentException ex) {
                    debug.error("Unknown direction \"" + direction + "\", did you spell it correctly?",
                            StringUtils.foundAt(file, path));
                    axis = new Vector();
                }
        }

        switch (config.getString("Type").toUpperCase()) {
            case "CIRCLE":
                shape = new Circle(points, radius);
                shape.setAxis(axis);
                break;
            case "LINE":
                shape = new Line(points);
                shape.setAxis(axis);
                break;
            case "SPHERE":
                shape = new Sphere(radius, points);
                shape.setAxis(axis);
                break;
            case "SPIRAL":
                shape = new Spiral(points, radius, 0.0, axis, loops);
                break;
            default:
                debug.error("Unknown shape \"" + config.getString("Type") + "\"!",
                        "Valid Shapes: " + Arrays.asList("Circle", "Line", "Sphere", "Spiral"),
                        StringUtils.foundAt(file, path));
                return null;
        }

        return shape;
    }
}
