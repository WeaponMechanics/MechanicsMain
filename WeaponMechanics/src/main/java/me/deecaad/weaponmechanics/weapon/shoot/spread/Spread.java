package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;

public class Spread implements Serializer<Spread> {

    private SpreadImage spreadImage;

    private double baseSpread;
    private ModifySpreadWhen modifySpreadWhen;
    private ChangingSpread changingSpread;

    /**
     * Default constructor for serializer
     */
    public Spread() {
    }

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
     * @param shootLocation the shoot location
     * @param mainHand whether or not main hand was used
     * @param updateSpreadChange whether or not to allow updating current spread change
     * @return the normalized spread direction
     */
    public Vector getNormalizedSpreadDirection(EntityWrapper entityWrapper, Location shootLocation, boolean mainHand, boolean updateSpreadChange) {
        double yaw = shootLocation.getYaw(), pitch = shootLocation.getPitch();
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
    @Nonnull
    public Spread serialize(SerializeData data) throws SerializerException {
        SpreadImage spreadImage = data.of("Spread_Image").serialize(SpreadImage.class);
        if (spreadImage != null)
            return new Spread(spreadImage);

        double baseSpread = data.of("Base_Spread").assertExists().assertPositive().getDouble();
        baseSpread /= 100.0;

        ModifySpreadWhen modifySpreadWhen = (ModifySpreadWhen) data.of("Modify_Spread_When").serialize(new ModifySpreadWhen());
        ChangingSpread changingSpread = data.of("Changing_Spread").serialize(ChangingSpread.class);
        return new Spread(baseSpread, modifySpreadWhen, changingSpread);
    }
}
