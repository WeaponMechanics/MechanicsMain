package me.deecaad.core.effects.particles;

import me.deecaad.core.effects.data.EffectData;
import me.deecaad.core.effects.shapes.Spiral;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpiralParticleEffect extends ParticleEffect {

    private Spiral spiral;
    private int interval;

    /**
     * Empty constructor for serializers
     */
    public SpiralParticleEffect() {
    }

    public SpiralParticleEffect(Particle particle, int amount, double horizontal, double vertical, double speed,
                                @Nullable Object particleData, Spiral spiral, int interval) {
        super(particle, amount, horizontal, vertical, speed, particleData);

        this.spiral = spiral;
    }

    @Override
    public void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable EffectData data) {
        spiral.reset();
        for (int i = 0; spiral.hasNext(); i++) {
            Vector next = spiral.next();

            Bukkit.getScheduler().runTaskLater(source, () -> {
                super.spawnOnce(source, world, x + next.getX(), y + next.getZ(), z + next.getY(), data);
            }, i/* * interval*/);
        }

        //for (int i = 0; i < circle.getPoints(); i++) {
        //    x += around.getX();
        //    y += around.getZ();
        //    z += around.getY();
//
        //    Location loc = circle.getNext();
        //    final double finalX = x + loc.getX();
        //    final double finalY = y + loc.getY();
        //    final double finalZ = z + loc.getZ();
        //    Bukkit.getScheduler().runTaskLater(source, () -> {
        //        super.spawnOnce(source, world, finalX, finalY, finalZ, data);
        //    }, i * interval);
        //}
    }
}
