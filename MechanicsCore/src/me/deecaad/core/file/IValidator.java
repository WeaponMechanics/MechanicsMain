package me.deecaad.core.file;

import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

public interface IValidator {

    /**
     * @return keyword of this validator used in configurations
     */
    String getKeyword();

    /**
     * This is used to validate configurations which can't be used as serializers.
     * This validation process should be done after the serialization.
     *
     * @param configuration the global configuration object
     * @param file the file being filled
     * @param configurationSection the configuration section object
     * @param path the path to this validator's path (path to keyword like path.keyword)
     */
    void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path);
}