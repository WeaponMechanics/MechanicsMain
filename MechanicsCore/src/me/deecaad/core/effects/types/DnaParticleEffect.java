package me.deecaad.core.effects.types;

import me.deecaad.core.effects.ShapedEffect;
import me.deecaad.core.effects.shapes.Circle;
import me.deecaad.core.effects.shapes.Line;
import me.deecaad.core.effects.shapes.Spiral;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class DnaParticleEffect extends ParticleEffect implements ShapedEffect {

    private Vector axis;
    private ArrayList<Vector> vectors;
    private Map<Vector, Vector> linePoints;

    public DnaParticleEffect(Spiral a, int rungs, Particle particle) {
        super(particle);

        // Setup the opposite spiral to create the helix
        Circle temp = a.getCircle();
        Circle opposite = new Circle(temp.getPoints(), temp.getAmplitude(), temp.getOffset() + Math.PI);
        opposite.setAxis(a.getStep());

        int points = temp.getPoints();
        Spiral b = new Spiral(opposite, a.getStep().multiply(a.getTotalPoints()), a.getTotalPoints() / points);

        // Get how often to create a particle effect
        int rungSeparation = points / rungs;
        if (rungSeparation == 0) {
            debug.log(LogLevel.ERROR, "Invalid spiral for DNA creation");
        }

        vectors = new ArrayList<>();
        Iterator<Vector> aIterator = a.iterator();
        Iterator<Vector> bIterator = b.iterator();

        linePoints = new HashMap<>();
        for (int i = 0; i < a.getTotalPoints(); i++) {
            if (!aIterator.hasNext() || !bIterator.hasNext()) {
                debug.log(LogLevel.ERROR, "Failed to create DNA effect");
            }

            // Get the next points and add them to
            // the list of points
            Vector _a = aIterator.next();
            Vector _b = bIterator.next();
            vectors.add(_a);
            vectors.add(_b);

            // Check to see if I should create a rung this time
            if (i % rungSeparation == 0) {
                rungs--;

                // Create the rung between the 2 vectors
                Line line = Line.between(_a, _b, 12); // todo hardcoded
                line.forEach(vector -> linePoints.put(vector, _b)); // These have to be inverted because duplicate keys
            }
        }

        // Set the default axis
        axis = new Vector();
    }

    @Override
    public void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data) {
        for (Vector vector : vectors) {
            final double xPos = x + vector.getX() + axis.getX();
            final double yPos = y + vector.getY() + axis.getY();
            final double zPos = z + vector.getZ() + axis.getZ();

            super.spawnOnce(source, world, xPos, yPos, zPos, data);
        }

        linePoints.forEach((vector, offset) -> {
            final double xPos = x + offset.getX() + vector.getX() + axis.getX();
            final double yPos = y + offset.getY() + vector.getY() + axis.getY();
            final double zPos = z + offset.getZ() + vector.getZ() + axis.getZ();

            super.spawnOnce(source, world, xPos, yPos, zPos, data);
        });
    }

    @Override
    public void setAxis(Vector vector) {
        this.axis = vector;
    }
}
