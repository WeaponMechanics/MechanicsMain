package me.deecaad.core.__mechanics.features;

import me.deecaad.core.__mechanics.IMechanic;
import me.deecaad.core.file.Serializer;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.io.File;

public class Feature1 implements Serializer<Feature1>, IMechanic {

    @Override
    public int use(Entity entity) {
        return use(entity.getLocation());
    }

    @Override
    public int use(Location location) {
        location.getWorld().createExplosion(location, 1.0f);
        return 0;
    }

    @Override
    public String getKeyword() {
        return "Feature1";
    }

    @Override
    public Feature1 serialize(File file, ConfigurationSection configurationSection, String path) {

        // Serialize normally Feature1

        return null;
    }

    public Feature1 stringSerialize(File file, ConfigurationSection configurationSection, String path, String line) {

        // Serialize using param line

        return null;
    }
}