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
    private boolean isRotating;
    private int counter;

    private Recoil tempRecoil;

    private int rotations;
    private float yawPerIteration;
    private float pitchPerIteration;

    private long recoverTime;
    private final float recoverToYaw;
    private final float recoverToPitch;

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
        if (tempRecoil != null) {
            List<Float> yaws = tempRecoil.getYaws();
            List<Float> pitches = tempRecoil.getPitches();
            float rotateYaw = yaws.get(NumberUtils.random(yaws.size()));
            float rotatePitch = pitches.get(NumberUtils.random(pitches.size()));
            rotations = (int) (tempRecoil.getRotationTime() / Recoil.MILLIS_BETWEEN_ROTATIONS);
            recoverTime = tempRecoil.getRecoverTime();
            yawPerIteration = rotateYaw / rotations;
            pitchPerIteration = rotatePitch / rotations;

            tempRecoil = null;
            counter = 0;
            isRotating = true;
        }

        shootCompatibility.modifyCameraRotation(playerWrapper.getPlayer(), yawPerIteration, pitchPerIteration, false);

        if (++counter >= rotations) {
            if (!isRotating) {
                // Recovery finished
                playerWrapper.setRecoilTask(null);
                cancel();
                return;
            }

            // Rotation finished, start recovering

            rotations = (int) (recoverTime / Recoil.MILLIS_BETWEEN_ROTATIONS);

            Location location = playerWrapper.getPlayer().getLocation();

            yawPerIteration = calculateYawDifference(location.getYaw()) / rotations;
            pitchPerIteration = calculatePitchDifference(location.getPitch()) / rotations * -1;

            counter = 0;
            isRotating = false;
        }

    }

    private float calculateYawDifference(float currentYaw) {
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