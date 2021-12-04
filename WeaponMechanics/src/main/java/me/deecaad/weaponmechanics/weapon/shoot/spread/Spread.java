package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

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

    /**
     * Returns normalized spread direction
     *
     * @param entityWrapper the entity involved
     * @param mainHand whether or not main hand was used
     * @param updateSpreadChange whether or not to allow updating current spread change
     * @return the normalized spread direction
     */
    public Vector getNormalizedSpreadDirection(IEntityWrapper entityWrapper, boolean mainHand, boolean updateSpreadChange) {
        Location location = entityWrapper.getEntity().getLocation();
        double yaw = location.getYaw(), pitch = location.getPitch();
        if (spreadImage != null) {
            Point point = spreadImage.getLocation();
            return getNormalizedSpreadImageDirection(Math.toRadians(yaw), Math.toRadians(pitch), point.getYaw(), point.getPitch());
        }

        double spread = baseSpread;

        if (modifySpreadWhen != null) spread = modifySpreadWhen.applyChanges(entityWrapper, spread);
        if (changingSpread != null) spread = changingSpread.applyChanges(entityWrapper, spread, mainHand, updateSpreadChange);

        return getNormalizedSpreadDirection(yaw, pitch, spread);
    }

    private Vector getNormalizedSpreadImageDirection(double startYaw, double startPitch, double yaw, double pitch) {
        return VectorUtil.getVector(startYaw + yaw, startPitch + pitch).normalize();
    }

    /**
     * Used to get random normalized spread direction
     *
     * @param yaw the yaw of direction
     * @param pitch the pitch of direction
     * @param spread the spread
     * @return the randomized direction based on given params as normalized vector
     */
    private Vector getNormalizedSpreadDirection(double yaw, double pitch, double spread) {

        // Create random numbers for horizontal and vertical spread
        double randomX = NumberUtil.random(-spread, spread),
                randomY = NumberUtil.random(-spread, spread),
                randomZ = NumberUtil.random(-spread, spread);

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
        if (baseSpread <= 0.0) {
            debug.error("Base_Spread must be greater than 0!", StringUtil.foundAt(file, path + ".Base_Spread"));
            return null;
        }

        baseSpread *= 0.1;

        ModifySpreadWhen modifySpreadWhen = new ModifySpreadWhen().serialize(file, configurationSection, path + ".Modify_Spread_When");
        ChangingSpread changingSpread = new ChangingSpread().serialize(file, configurationSection, path + ".Changing_Spread");
        return new Spread(baseSpread, modifySpreadWhen, changingSpread);
    }
}
