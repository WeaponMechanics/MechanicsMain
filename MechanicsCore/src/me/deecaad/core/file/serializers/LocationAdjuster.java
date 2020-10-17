package me.deecaad.core.file.serializers;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

public class LocationAdjuster implements Serializer<LocationAdjuster> {

    private double x, y, z;

    /**
     * Empty constructor to be used as serializer
     */
    public LocationAdjuster() {}

    /**
     * This can be used to find new location based on x, y, z given from certain location.
     *
     * <pre>
     * X represents the moving sideways. Positive value means right and negative left.
     * Y represents the moving upwards/downwards. Positive value means up and negative down.
     * Z represents the moving forwards/backwards. Positive value means forward and negative backward.
     * </pre>
     *
     * @param x the value moved sideways
     * @param y the value moved upward/downward
     * @param z the value moved forward/backward
     */
    public LocationAdjuster(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @param location the location from which the starting location will be taken
     * @return the new location based on this location finder's x, y and z and location given for this method.
     */
    public Location getNewLocation(Location location) {
        if (this.x == 0.0 && this.z == 0.0) {
            return new Location(location.getWorld(), location.getX(), (location.getY() + this.y), location.getZ());
        }
        double yawToRadians = Math.toRadians(location.getYaw());
        double sinYaw = Math.sin(yawToRadians);
        double cosYaw = Math.cos(yawToRadians);
        return new Location(location.getWorld(),
                (-1.0 * sinYaw * this.z) + location.getX() + (-this.x * cosYaw),
                (location.getY() + this.y),
                (cosYaw * this.z) + location.getZ() + (-this.x * sinYaw));
    }

    @Override
    public String getKeyword() {
        return "Location_Adjuster";
    }

    @Override
    public LocationAdjuster serialize(File file, ConfigurationSection configurationSection, String path) {
        String[] adjusterData = StringUtils.split(configurationSection.getString(path));
        if (adjusterData == null) return null;

        if (adjusterData.length < 3) {
            MechanicsCore.debug.log(LogLevel.ERROR,
                    "Found an invalid location adjuster format in configurations!",
                    "Located at file " + file + " in " + path + " in configurations",
                    "Make sure there is 3 args: <x>~<y>~<z>.");
            return null;
        }

        double x, y, z;
        try {
            x = Integer.parseInt(adjusterData[0]);
            y = Integer.parseInt(adjusterData[1]);
            z = Integer.parseInt(adjusterData[2]);
        } catch (NumberFormatException e) {
            MechanicsCore.debug.log(LogLevel.ERROR,
                    "Found an invalid number format in configurations!",
                    "Located at file " + file + " in " + path + " in configurations",
                    "Make sure you only use numbers.");
            return null;
        }

        return new LocationAdjuster(x, y, z);
    }
}
