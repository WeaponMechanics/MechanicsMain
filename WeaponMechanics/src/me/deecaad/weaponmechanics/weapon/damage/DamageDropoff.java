package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class DamageDropoff implements Serializer<DamageDropoff> {

    private TreeMap<Double, Double> distances;

    /**
     * Default constructor for serializer
     */
    public DamageDropoff() {
    }

    public DamageDropoff(TreeMap<Double, Double> distances) {
        this.distances = distances;
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
        return "Dropoff";
    }

    @Override
    public DamageDropoff serialize(File file, ConfigurationSection configurationSection, String path) {
        List<String> strings = configurationSection.getStringList(path);
        TreeMap<Double, Double> distances = new TreeMap<>();

        for (String str : strings) {
            try {
                String[] split = StringUtils.split(str);
                Double blocks = Double.valueOf(split[0]);
                Double damage = Double.valueOf(split[1]);
                distances.put(blocks, damage);

            } catch (NumberFormatException ex) {
                debug.error("Unknown decimal " + ex.getMessage(), StringUtils.foundAt(file, path));
                return null;
            } catch (ArrayIndexOutOfBoundsException ex) {
                debug.error("You must specify both blocks and damage. For input string: " + str,
                        StringUtils.foundAt(file, path));
                return null;
            }
        }

        return new DamageDropoff(distances);
    }
}
