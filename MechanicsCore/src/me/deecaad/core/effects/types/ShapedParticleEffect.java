package me.deecaad.core.effects.types;

import me.deecaad.core.effects.ShapedEffect;
import me.deecaad.core.effects.shapes.Shape;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShapedParticleEffect extends ParticleEffect implements ShapedEffect {

    private Shape shape;
    private int interval;

    public ShapedParticleEffect(Particle particle, int amount, double horizontal, double vertical, double speed,
                                @Nullable Object particleData, @Nonnull Shape shape, int interval) {
        super(particle, amount, horizontal, vertical, speed, particleData);

        this.shape = shape;
        this.interval = interval;
    }

    @Override
    public void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data) {
        int counter = 0;

        if (data instanceof Vector) {
            shape.setAxis((Vector) data);
        }

        for (Vector vector: shape) {
            Bukkit.getScheduler().runTaskLater(source, () -> {
                super.spawnOnce(source, world, x + vector.getX(), y + vector.getY(), z + vector.getZ(), data);
            }, counter++ * interval);
        }
    }

    @Override
    public void setAxis(Vector vector) {
        shape.setAxis(vector);
    }
}
