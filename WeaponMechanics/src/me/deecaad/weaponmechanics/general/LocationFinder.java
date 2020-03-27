package me.deecaad.weaponmechanics.general;

import me.deecaad.core.file.Serializer;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import java.io.File;

public class LocationFinder implements Serializer<LocationFinder> {

    private double x;
    private double y;
    private double z;
    private boolean useEyeLocation;

    /**
     * Empty constructor to be used as serializer
     */
    public LocationFinder() {}

    /**
     * This can be used to find new location based on x, y, z given from certain location.
     *
     * <pre>
     * X represents the moving sideways. Positive value means right and negative left.
     * Y represents the moving upwards/downwards. Positive value means upward and negative downward.
     * Z represents the moving forwards/backwards. Positive value means forward and negative backward.
     * useEyeLocation means that should this finder use living entity's eye location rather than feet location in method getNewLocation(LivingEntity).
     * </pre>
     *
     * @param x the value moved sideways
     * @param y the value moved upward/downward
     * @param z the value moved forward/backward
     * @param useEyeLocation if living entity eye location should be used
     */
    public LocationFinder(double x, double y, double z, boolean useEyeLocation) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.useEyeLocation = useEyeLocation;
    }

    /**
     * @param livingEntity the living entity from which the starting location will be taken
     * @return the new location based on this location finder's x, y and z and living entity's location and direction.
     */
    public Location getNewLocation(LivingEntity livingEntity) {
        return getNewLocation(this.useEyeLocation ? livingEntity.getEyeLocation() : livingEntity.getLocation());
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
        return "Location_Finder";
    }

    @Override
    public LocationFinder serialize(File file, ConfigurationSection configurationSection, String path) {
        double x = configurationSection.getDouble(path + ".X", 9999.0);
        double y = configurationSection.getDouble(path + ".Y", 9999.0);
        double z = configurationSection.getDouble(path + ".Z", 9999.0);
        if (x == 9999.0 && y == 9999.0 && z == 9999.0) {
            return null;
        }
        if (x == 9999.0) {
            x = 0.0;
        }
        if (y == 9999.0) {
            y = 0.0;
        }
        if (z == 9999.0) {
            z = 0.0;
        }
        boolean useEyeLocation = configurationSection.getBoolean(path + ".Use_Eye_location", false);
        return new LocationFinder(x, y, z, useEyeLocation);
    }
}