package me.deecaad.core.file.serializers;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerTypeException;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.Location;

import javax.annotation.Nonnull;

public class LocationAdjuster implements Serializer<LocationAdjuster> {

    private double x, y, z;

    /**
     * Default constructor for serializer
     */
    public LocationAdjuster() {
    }

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
    public boolean canUsePathTo() {
        return false;
    }

    @Override
    @Nonnull
    public LocationAdjuster serialize(SerializeData data) throws SerializerException {
        String input = data.of().assertExists().assertType(String.class).get();
        String[] split = StringUtil.split(input);

        if (split.length < 3) {
            throw data.exception(null, "Expected x~y~z format for location adjuster",
                    SerializerException.forValue(input));
        }

        double x = Double.NaN;
        double y = Double.NaN;
        double z = Double.NaN;
        try {
            x = Double.parseDouble(split[0]);
            y = Double.parseDouble(split[1]);
            z = Double.parseDouble(split[2]);
        } catch (NumberFormatException e) {
            throw new SerializerTypeException(this, Number.class, null, e.getMessage(), data.of().getLocation())
                    .addMessage(String.format("X: %s, Y: %s, Z: %s", x, y, z));
        }

        return new LocationAdjuster(x, y, z);
    }
}
