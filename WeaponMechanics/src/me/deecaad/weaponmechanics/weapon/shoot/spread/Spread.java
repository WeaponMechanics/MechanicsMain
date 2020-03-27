package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.core.file.Serializer;
import me.deecaad.weaponmechanics.utils.NumberUtils;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.io.File;

public class Spread implements Serializer<Spread> {

    private SpreadImage spreadImage;

    private double baseSpread;
    private ModifySpreadWhen modifySpreadWhen;
    private ChangingSpread changingSpread;

    /**
     * Empty constructor to be used as serializer
     */
    public Spread() { }

    public Spread(SpreadImage spreadImage) {
        this.spreadImage = spreadImage;
    }

    public Spread(double baseSpread, ModifySpreadWhen modifySpreadWhen, ChangingSpread changingSpread) {
        this.baseSpread = baseSpread;
        this.modifySpreadWhen = modifySpreadWhen;
        this.changingSpread = changingSpread;
    }

    public Vector getNormalizedSpreadDirection(IEntityWrapper entityWrapper) {
        Location location = entityWrapper.getEntity().getLocation();
        double yaw = location.getYaw(), pitch = location.getPitch();
        if (spreadImage != null) {
            Point point = spreadImage.getLocation(3);
            return getNormalizedSpreadImageDirection(yaw, pitch, point.getX(), point.getY());
        }

        double spread = baseSpread;

        if (modifySpreadWhen != null) spread = modifySpreadWhen.applyChanges(entityWrapper, spread);
        if (changingSpread != null) spread = changingSpread.applyChanges(entityWrapper, spread);

        return getNormalizedSpreadDirection(yaw, pitch, spread);
    }

    public Vector getNormalizedSpreadImageDirection(double yaw, double pitch, double x, double y) {

        // todo

        return null;
    }

    /**
     * Used to get random normalized spread direction
     *
     * @param yaw the yaw of direction
     * @param pitch the pitch of direction
     * @param spread the spread
     * @return the randomized direction based on given params as normalized vector
     */
    public Vector getNormalizedSpreadDirection(double yaw, double pitch, double spread) {

        // Create random numbers for horizontal and vertical spread
        double randomX = NumberUtils.random(-spread, spread),
                randomY = NumberUtils.random(-spread, spread),
                randomZ = NumberUtils.random(-spread, spread);

        // Change yaw and pitch to radians
        double yawToRad = Math.toRadians(yaw);
        double pitchToRad = Math.toRadians(pitch);

        double xz = Math.cos(pitchToRad);

        // Last calculate the direction and add randomness to it
        // Then normalize it.
        return new Vector(-xz * Math.sin(yawToRad) + randomX,
                -Math.sin(pitchToRad) + randomY,
                xz * Math.cos(yawToRad) + randomZ).normalize();
    }

    @Override
    public String getKeyword() {
        return "Spread";
    }

    @Override
    public Spread serialize(File file, ConfigurationSection configurationSection, String path) {
        SpreadImage spreadImage = new SpreadImage().serialize(file, configurationSection, path + ".Spread_Image");
        if (spreadImage != null) {
            return new Spread(spreadImage);
        }
        double baseSpread = configurationSection.getDouble(path + ".Base_Spread");
        if (baseSpread == 0.0) {
            return null;
        }

        baseSpread *= 0.1;

        ModifySpreadWhen modifySpreadWhen = new ModifySpreadWhen().serialize(file, configurationSection, path + ".Modify_Spread_When");
        ChangingSpread changingSpread = new ChangingSpread().serialize(file, configurationSection, path + ".Changing_Spread");
        return new Spread(baseSpread, modifySpreadWhen, changingSpread);
    }
}
