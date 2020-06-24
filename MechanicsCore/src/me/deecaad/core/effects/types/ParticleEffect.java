package me.deecaad.core.effects.types;

import me.deecaad.core.effects.Effect;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParticleEffect extends Effect {

    protected Particle particle;
    protected int amount;
    protected double horizontal;
    protected double vertical;
    protected double speed;
    protected Object particleData;

    public ParticleEffect(Particle particle) {
        this.particle = particle;
        this.amount = 1;
    }

    public ParticleEffect(Particle particle, int amount, double horizontal, double vertical, double speed, @Nullable Object particleData) {
        this.particle = particle;
        this.amount = amount;
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.speed = speed;
        this.particleData = particleData;
    }

    @Override
    public void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data) {
        world.spawnParticle(particle, x, y, z, amount, horizontal, vertical, horizontal, speed, particleData, true);
    }

    @Override
    protected void spawnOnceFor(@Nonnull Plugin source, @Nonnull Player player, @Nonnull World world, double x, double y, double z, @Nullable Object data) {
        player.spawnParticle(particle, x, y, z, amount, horizontal, vertical, horizontal, speed, particleData);
    }
}
