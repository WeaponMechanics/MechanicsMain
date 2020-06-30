package me.deecaad.core.effects.serializers;

import me.deecaad.core.effects.types.ParticleEffect;
import me.deecaad.core.file.Serializer;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;

public class ParticleEffectSerializer implements Serializer<List<ParticleEffect>> {

    /**
     * Empty constructor for serializer
     */
    public ParticleEffectSerializer() {
    }

    @Override
    public String getKeyword() {
        return "Particle";
    }

    @Override
    public List<ParticleEffect> serialize(File file, ConfigurationSection configurationSection, String path) {


        return null;
    }
}
