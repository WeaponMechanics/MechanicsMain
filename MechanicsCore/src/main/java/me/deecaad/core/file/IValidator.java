package me.deecaad.core.file;

import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;

public interface IValidator {

    default boolean denyKeys() {
        return false;
    }

    /**
     * @return keyword of this validator used in configurations
     */
    String getKeyword();

    /**
     * This validator is only used if the path {weapon title}.{getAllowedPaths()} matches fully.
     *
     * @return The nullable allowed paths
     */
    default List<String> getAllowedPaths() {
        return null;
    }

    /**
     * This is used to validate configurations which can't be used as serializers.
     * This validation process should be done after the serialization.
     *
     * @param configuration the global configuration object
     * @param file the file being filled
     * @param configurationSection the configuration section object
     * @param path the path to this validator's path (path to keyword like path.keyword)
     */
    void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path) throws SerializerException;
}