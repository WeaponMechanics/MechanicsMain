package me.deecaad.core.effects.particles;

import me.deecaad.core.effects.AbstractEffect;
import me.deecaad.core.effects.data.EffectData;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ParticleEffect extends AbstractEffect {

    protected Particle particle;
    protected int amount;
    protected double horizontal;
    protected double vertical;
    protected double speed;
    protected Object particleData;

    public ParticleEffect() {
    }

    public ParticleEffect(Particle particle, int amount, double horizontal, double vertical, double speed) {
        this.particle = particle;
        this.amount = amount;
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.speed = speed;
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
    public void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable EffectData data) {
        world.spawnParticle(particle, x, y, z, amount, horizontal, vertical, horizontal, speed, particleData);
    }

    @Override
    public AbstractEffect serialize(Map<String, SerializerData<?>> args) {
        Particle particle = Particle.valueOf((String) args.get("particle").getData());

        return null;
    }

    @Override
    public Map<String, SerializerData<?>> getDefaults() {
        final Map<String, SerializerData<?>> defaults = new HashMap<>();

        defaults.put("particle", new SerializerData<>(Particle.VILLAGER_HAPPY.name()));
        defaults.put("hspread", new SerializerData<>(1.0));
        defaults.put("vspread", new SerializerData<>(1.0));

        return defaults;
    }
}
