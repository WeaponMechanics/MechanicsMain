package me.deecaad.core.effects.serializers;

import me.deecaad.core.effects.Effect;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

public class EffectListSerializer implements Serializer<List<Effect>> {

    private static final Map<String, Serializer<? extends List<? extends Effect>>> SERIALIZERS = new HashMap<>();

    /**
     * Default constructor for serializer
     */
    public EffectListSerializer() {
    }

    @Override
    public String getKeyword() {
        return "Effects";
    }

    @Override
    public List<Effect> serialize(File file, ConfigurationSection configurationSection, String path) {

        List<Effect> temp = new ArrayList<>();

        // Effect format follows the list format
        // Effects:
        //   - Sound()
        //   - Particle()
        if (configurationSection.getStringList(path) != null) {
            // todo
        } else {
            ConfigurationSection config = configurationSection.getConfigurationSection(path);

            for (String key : config.getKeys(false)) {
                Serializer<? extends List<? extends Effect>> serializer = SERIALIZERS.get(key);

                if (serializer == null) {
                    debug.error("Unknown effect type \"" + key + "\"... Did you misspell it?",
                            "Valid Effects: " + SERIALIZERS.keySet(),
                            StringUtils.foundAt(file, path));
                    continue;
                }

                List<? extends Effect> effects = serializer.serialize(file, configurationSection, path + "." + key);

                if (effects == null) {
                    debug.warn("Serialization of effects \"" + key + "\" failed...");
                } else {
                    temp.addAll(effects);
                }
            }
        }

        return temp;
    }

    public static void addSerializer(Serializer<? extends List<? extends Effect>> serializer) {
        String key = serializer.getKeyword();

        if (SERIALIZERS.containsKey(key)) {
            throw new IllegalArgumentException("A serializer with key \"" + key + "\" already exists");
        }

        SERIALIZERS.put(key, serializer);
    }

    static {
        addSerializer(new ParticleEffectSerializer());
        addSerializer(new SoundEffectSerializer());
    }
}
