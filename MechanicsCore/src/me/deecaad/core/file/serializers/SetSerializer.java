package me.deecaad.core.file.serializers;

import me.deecaad.core.file.Serializer;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SetSerializer<T> implements Serializer<Set<T>> {

    /**
     * Default constructor for serializer
     */
    public SetSerializer() {
    }

    abstract T serialize(@Nonnull String str);

    @Override
    public Set<T> serialize(File file, ConfigurationSection config, String path) {
        return config.getStringList(path).stream()
                .map(this::serialize)
                .collect(Collectors.toSet());
    }
}
