package me.deecaad.weaponmechanics.weapon;

import me.deecaad.core.file.Serializer;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class DamageDropoff implements Serializer<DamageDropoff> {

    private TreeMap<Double, Double> distances;
    boolean average;

    /**
     * Default constructor for serializer
     */
    public DamageDropoff() {
    }

    public DamageDropoff(TreeMap<Double, Double> distances, boolean average) {
        this.distances = distances;
        this.average = average;
    }

    /**
     * Gets the damage modifier for given distance. If no damage
     * modifier exists, the damage modifier is 0.0
     *
     * @param distance How far away
     * @return Damage modifier
     */
    public double getDamage(double distance) {
        Map.Entry<Double, Double> floor = distances.floorEntry(distance);

        return floor == null ? 0.0 : floor.getValue();
    }

    @Override
    public String getKeyword() {
        return null;
    }

    @Override
    public DamageDropoff serialize(File file, ConfigurationSection configurationSection, String path) {
        return null;
    }
}
