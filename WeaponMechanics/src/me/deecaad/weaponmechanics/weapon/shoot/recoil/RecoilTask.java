package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.shoot.IShootCompatibility;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.Location;

import java.util.List;
import java.util.TimerTask;

public class RecoilTask extends TimerTask {

    private static final IShootCompatibility shootCompatibility = CompatibilityAPI.getCompatibility().getShootCompatibility();
    private final IPlayerWrapper playerWrapper;

    /**
     * True = rotating
     * False = recovering
     */
    private boolean isRotating;

    private int counter;
    private Recoil tempRecoil;
    private int rotations;
    private float yawPerIteration;
    private float pitchPerIteration;
    private int waitRotations;
    private long recoverTime;
    private final float recoverToYaw;
    private final float recoverToPitch;
    private float maximumYawChange;
    private float maximumPitchChange;

    private float shouldBeLastYaw;

    public RecoilTask(IPlayerWrapper playerWrapper, Recoil recoil) {
        this.playerWrapper = playerWrapper;

        Location location = playerWrapper.getPlayer().getLocation();
        recoverToYaw = location.getYaw();
        recoverToPitch = location.getPitch();

        this.tempRecoil = recoil;
    }

    public void setRecoil(Recoil recoil) {
        tempRecoil = recoil;
    }

    @Override
    public void run() {
        handleNewRecoil();

        if (isRotating) {
            Location location = playerWrapper.getPlayer().getLocation();
            float pitch = location.getPitch();

            // todo: add randomness for bounds
            //boolean hasMaximumPitchChange = Math.abs(calculatePitchDifference(pitch)) > maximumPitchChange || pitch < -80;
            //boolean hasMaximumYawChange = Math.abs(calculateYawDifference(location.getYaw())) > maximumYawChange;
            //shootCompatibility.modifyCameraRotation(playerWrapper.getPlayer(), hasMaximumYawChange ? 0 : yawPerIteration, hasMaximumPitchChange ? 0 : pitchPerIteration, false);
            shootCompatibility.modifyCameraRotation(playerWrapper.getPlayer(), yawPerIteration, pitchPerIteration, false);
        } else if (counter >= waitRotations) {
            // Let recovering happen normally without checking any maximum pitch changes
            shootCompatibility.modifyCameraRotation(playerWrapper.getPlayer(), yawPerIteration, pitchPerIteration, false);
        }

        if (++counter >= rotations) {
            if (!isRotating || recoverTime == 0) {
                // Recovery finished
                // OR
                // Recovery is not used
                playerWrapper.setRecoilTask(null);
                cancel();
                return;
            }

            // Rotation finished, start recovering

            // Wait for 55 millis before starting recovery
            waitRotations = (int) (55 / Recoil.MILLIS_BETWEEN_ROTATIONS) - 1;

            rotations = (int) (recoverTime / Recoil.MILLIS_BETWEEN_ROTATIONS);

            Location location = playerWrapper.getPlayer().getLocation();
            float yaw = location.getYaw();

            // If user input changed yaw more than 45 deg
            // -> don't recover yaw
            yawPerIteration = calculateYawUserInput(yaw) > 45 ? 0 : calculateYawDifference(yaw) / rotations;

            pitchPerIteration = calculatePitchDifference(location.getPitch()) / rotations * -1;

            // Last add that wait time for rotations
            rotations += waitRotations;

            counter = 0;
            isRotating = false;
        }
    }

    /**
     * Simply handles new recoil if set
     */
    private void handleNewRecoil() {
        if (tempRecoil != null) {
            List<Float> yaws = tempRecoil.getYaws();
            List<Float> pitches = tempRecoil.getPitches();
            float rotateYaw = yaws.get(NumberUtils.random(yaws.size()));
            float rotatePitch = pitches.get(NumberUtils.random(pitches.size()));

            long rotationTime = tempRecoil.getRotationTime();

            if (rotationTime == 0) {
                // Meaning rotation should be instant
                rotations = 1;
                yawPerIteration = rotateYaw;
                pitchPerIteration = rotatePitch;
            } else {
                rotations = (int) (tempRecoil.getRotationTime() / Recoil.MILLIS_BETWEEN_ROTATIONS);
                yawPerIteration = rotateYaw / rotations;
                pitchPerIteration = rotatePitch / rotations;
            }

            recoverTime = tempRecoil.getRecoverTime();
            maximumYawChange = tempRecoil.getMaximumYawChange();
            maximumPitchChange = tempRecoil.getMaximumPitchChange();

            // Calculate where last yaw spot should be
            shouldBeLastYaw = shouldBeLastYaw == 0 ? playerWrapper.getPlayer().getLocation().getYaw() + rotateYaw : shouldBeLastYaw + rotateYaw;

            tempRecoil = null;
            counter = 0;
            isRotating = true;
            waitRotations = 0;
        }
    }

    private float calculateYawUserInput(float currentYaw) {
        float userInputYawCheck = Math.abs(currentYaw - shouldBeLastYaw);
        userInputYawCheck -= (userInputYawCheck > 180 ? 360 : 0);
        return userInputYawCheck;
    }

    private float calculateYawDifference(float currentYaw) {
        // Recover normally as user input didn't change yaw too much
        float yawDifference = Math.abs(currentYaw - recoverToYaw);
        yawDifference -= (yawDifference > 180 ? 360 : 0);

        if (currentYaw > recoverToYaw) {
            yawDifference *= -1;
        }
        return yawDifference;
    }

    private float calculatePitchDifference(float currentPitch) {
        float pitchDifference = Math.abs(currentPitch - recoverToPitch);

        if (currentPitch > recoverToPitch) {
            pitchDifference *= -1;
        }
        return pitchDifference;
    }
}