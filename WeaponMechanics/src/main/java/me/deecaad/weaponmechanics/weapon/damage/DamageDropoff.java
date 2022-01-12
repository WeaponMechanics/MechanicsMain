package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
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
    @Nonnull
    public DamageDropoff serialize(File file, ConfigurationSection configurationSection, String path) throws SerializerException {
        List<String> strings = configurationSection.getStringList(path);
        TreeMap<Double, Double> distances = new TreeMap<>();

        for (String str : strings) {
            try {
                String[] split = StringUtil.split(str);
                Double blocks = Double.valueOf(split[0]);
                Double damage = Double.valueOf(split[1]);
                distances.put(blocks, damage);

            } catch (NumberFormatException ex) {
                debug.error("Unknown decimal " + ex.getMessage(), StringUtil.foundAt(file, path));
                debug.log(LogLevel.DEBUG, ex);
            } catch (ArrayIndexOutOfBoundsException ex) {
                debug.error("You must specify both blocks and damage. For input string: " + str,
                        StringUtil.foundAt(file, path));
                debug.log(LogLevel.DEBUG, ex);
            }
        }

        return new DamageDropoff(distances);
    }
}