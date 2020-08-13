package me.deecaad.core.__mechanics.features;

import me.deecaad.core.__mechanics.IMechanic;
import me.deecaad.core.file.FileReader;
import me.deecaad.core.file.Serializer;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.io.File;

public class AnotherFeature implements Serializer<AnotherFeature>, IMechanic {

    @Override
    public int use(Entity entity) {
        return use(entity.getLocation());
    }

    @Override
    public int useFor(Entity entity) {
        return 0;
    }

    @Override
    public int use(Location location) {
        location.getWorld().generateTree(location, TreeType.BIG_TREE);
        return 0;
    }

    @Override
    public String getKeyword() {
        return "AnotherFeature";
    }

    @Override
    public AnotherFeature serialize(File file, ConfigurationSection configurationSection, String path) {

        // Serialize normally AnotherFeature (yml)

        return null;
    }

    @Override
    public IMechanic stringSerialize(File file, ConfigurationSection configurationSection, String path, FileReader fileReader, String line) {

        // Serialize using param line

        return null;
    }
}