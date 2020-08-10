package me.deecaad.core.__mechanics;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

public class Mechanics implements Serializer<Mechanics> {

    private List<IMechanic> mechanicList;

    /**
     * Empty constructor for serializers
     */
    public Mechanics() { }

    public Mechanics(List<IMechanic> mechanicList) {
        this.mechanicList = mechanicList;
    }

    public void use(Entity entity) {
        mechanicList.forEach(iMechanic -> iMechanic.use(entity));
    }

    public void use(Location location) {
        mechanicList.forEach(iMechanic -> iMechanic.use(location));
    }

    @Override
    public String getKeyword() {
        return "Mechanics";
    }

    @Override
    public Mechanics serialize(File file, ConfigurationSection configurationSection, String path) {

        List<IMechanic> mechanicList = new ArrayList<>();

        List<String> stringSerializers = configurationSection.getStringList(path);
        if (stringSerializers == null) {

            // Ill do way to fetch this if we decide to do this way
            Map<String, Serializer<?>> mapOfSerializers = null;

            ConfigurationSection mechanicsSection = configurationSection.getConfigurationSection(path);
            if (mechanicsSection == null) return null;

            for (String mechanicKeyword : mechanicsSection.getKeys(false)) {

                Serializer<?> serializer = mapOfSerializers.get(mechanicKeyword.toLowerCase());
                if (serializer == null) {

                    String didYouMean = StringUtils.didYouMean(mechanicKeyword.toLowerCase(), mapOfSerializers.keySet());

                    debug.log(LogLevel.ERROR,
                            "Found an invalid keyword from string serializer in configurations!",
                            "Located at file " + file + " in " + path + " in configurations",
                            "Your input was " + mechanicKeyword.toLowerCase() + ", did you mean to use " + didYouMean + "?");
                    continue;
                }

                /*IMechanic*/Object serializerStringMechanic = serializer.serialize(file, configurationSection, path);

                // Check if something went wrong in serialization (don't add null values)
                if (serializerStringMechanic == null) continue;

                mechanicList.add((IMechanic) serializerStringMechanic);

            }

            if (mechanicList.isEmpty()) return null;

            return new Mechanics(mechanicList);
        }

        // Ill do way to fetch this if we decide to do this way
        // Just as an example, list of string serializers would be made
        Map<String, /*String*/Serializer<?>> mapOfStringSerializers = null;

        // Meaning that string serializers are used
        for (int i = 0; i < stringSerializers.size(); ++i) {
            String line = stringSerializers.get(i);

            // Not sure if this splits correctly with (
            // Just want the keyword
            String[] keywordSplittedLine = line.split("\\(");

            // Not valid line
            if (keywordSplittedLine.length <= 1) continue;

            String keywordLowerCase = keywordSplittedLine[0].toLowerCase();

            /*String*/Serializer<?> stringSerializer = mapOfStringSerializers.get(keywordLowerCase);
            if (stringSerializer == null) {

                String didYouMean = StringUtils.didYouMean(keywordLowerCase, mapOfStringSerializers.keySet());

                debug.log(LogLevel.ERROR,
                        "Found an invalid keyword from string serializer in configurations!",
                        "Located at file " + file + " in " + path + " in configurations",
                        "Your input was " + keywordLowerCase + " in line " + (i + 1) + ", did you mean to use " + didYouMean + "?");
                continue;
            }

            /*IMechanic*/Object serializerStringMechanic = stringSerializer./*string*/serialize(file, configurationSection, path/*, line*/);

            // Check if something went wrong in serialization (don't add null values)
            if (serializerStringMechanic == null) continue;

            mechanicList.add((IMechanic) serializerStringMechanic);
        }

        if (mechanicList.isEmpty()) return null;

        return new Mechanics(mechanicList);
    }
}