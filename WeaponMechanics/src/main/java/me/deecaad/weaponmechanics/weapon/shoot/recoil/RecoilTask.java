package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponcompatibility.shoot.IShootCompatibility;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.TimerTask;

public class RecoilTask extends TimerTask {

    private static final IShootCompatibility shootCompatibility = WeaponCompatibilityAPI.getShootCompatibility();
    private final IPlayerWrapper playerWrapper;
    private final HandData handData;

    /**
     * True = rotating
     * False = recovering
     */
    private boolean isRotating;

    /**
     * Gets the index where recoil is currently in the recoil pattern
     */
    private int currentIndexAtRecoilPattern;

    private int counter;
    private Recoil tempRecoil;
    private int rotations;
    private float yawPerIteration;
    private float pitchPerIteration;
    private int waitRotations;
    private long recoverTime;
    private final float recoverToYaw;
    private final float recoverToPitch;
    private float shouldBeLastYaw = -361;
    private float shouldBeLastPitch = -361;

    public RecoilTask(IPlayerWrapper playerWrapper, HandData handData, Recoil recoil) {
        this.playerWrapper = playerWrapper;
        this.handData = handData;

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

        Player player = playerWrapper.getPlayer();
        if (player == null || player.isDead() || !player.isOnline()) {
            handData.setRecoilTask(null);
            cancel();
            return;
        }

        // If this returns true, that means task is terminated
        if (handleNewRecoil()) return;

        if (isRotating) {
            Location location = playerWrapper.getPlayer().getLocation();
            float pitch = location.getPitch() < -80 ? 0 : pitchPerIteration;
            shouldBeLastPitch -= pitch;
            shouldBeLastYaw += yawPerIteration;
            shootCompatibility.modifyCameraRotation(playerWrapper.getPlayer(), yawPerIteration, pitch, false);
        } else if (counter >= waitRotations) {
            // Let recovering happen normally without checking any maximum pitch changes
            shootCompatibility.modifyCameraRotation(playerWrapper.getPlayer(), yawPerIteration, pitchPerIteration, false);
        }

        if (++counter >= rotations) {
            if (!isRotating || recoverTime == 0) {
                // Recovery finished
                // OR
                // Recovery is not used
                handData.setRecoilTask(null);
                cancel();
                return;
            }

            // Rotation finished, start recovering

            // Wait for 60 millis before starting recovery
            waitRotations = (int) (60 / Recoil.MILLIS_BETWEEN_ROTATIONS) - 1;

            rotations = (int) (recoverTime / Recoil.MILLIS_BETWEEN_ROTATIONS);

            Location location = playerWrapper.getPlayer().getLocation();
            float yaw = location.getYaw();
            float pitch = location.getPitch();

            // If user input changed yaw more than 45 deg
            // -> don't recover yaw
            yawPerIteration = Math.abs(calculateYawUserInput(yaw)) > 45 ? 0 : calculateYawDifference(yaw) / rotations;

            pitchPerIteration = calculatePitchUserInput(pitch) > 45 ? 0 : calculatePitchDifference(pitch) / rotations * -1;

            // Last add that wait time for rotations
            rotations += waitRotations;

            counter = 0;
            isRotating = false;
        }
    }

    /**
     * Simply handles new recoil if there is one
     *
     * @return true ONLY if task is terminated
     */
    private boolean handleNewRecoil() {
        if (tempRecoil == null) return false;

        float rotateYaw = 0;
        float rotatePitch = 0;

        RecoilPattern pattern = tempRecoil.getRecoilPattern();
        if (pattern != null) {
            RecoilPattern.ExtraRecoilPatternData nextData = getNext(pattern);
            if (nextData != null) {
                rotateYaw = nextData.getHorizontalRecoil();
                rotatePitch = nextData.getVerticalRecoil();
            }
        }

        List<Float> horizontal = tempRecoil.getRandomHorizontal();
        if (rotateYaw == 0 && horizontal != null) {
            rotateYaw = horizontal.get(NumberUtil.random(horizontal.size()));
        }
        List<Float> vertical = tempRecoil.getRandomVertical();
        if (rotatePitch == 0 && vertical != null) {
            rotatePitch = vertical.get(NumberUtil.random(vertical.size()));
        }

        if (rotateYaw == 0 && rotatePitch == 0) {
            // Neither one wasn't used?
            // Terminate this task...
            handData.setRecoilTask(null);
            cancel();
            return true;
        }

        long pushTime = tempRecoil.getPushTime();

        if (pushTime == 0) {
            // Meaning rotation should be instant
            rotations = 1;
            yawPerIteration = rotateYaw;
            pitchPerIteration = rotatePitch;
        } else {
            rotations = (int) (pushTime / Recoil.MILLIS_BETWEEN_ROTATIONS);
            yawPerIteration = rotateYaw / rotations;
            pitchPerIteration = rotatePitch / rotations;
        }

        recoverTime = tempRecoil.getRecoverTime();

        Location playerLocation = playerWrapper.getPlayer().getLocation();

        // Calculate where last yaw spot should be
        //shouldBeLastYaw = shouldBeLastYaw == -361 ? playerLocation.getYaw() + rotateYaw : shouldBeLastYaw + rotateYaw;
        if (shouldBeLastYaw == -361) {
            shouldBeLastYaw = playerLocation.getYaw();
        }

        // Calculate where last pitch spot should be
        if (shouldBeLastPitch == -361) {
            shouldBeLastPitch = playerLocation.getPitch();
        }

        tempRecoil = null;
        counter = 0;
        isRotating = true;
        waitRotations = 0;
        return false;
    }

    private RecoilPattern.ExtraRecoilPatternData getNext(RecoilPattern pattern) {
        List<RecoilPattern.ExtraRecoilPatternData> list = pattern.getRecoilPatternList();

        if (currentIndexAtRecoilPattern >= list.size()) {
            // Basically means that this recoil pattern has reached its end
            // AND its not using repeat pattern
            return null;
        }

        RecoilPattern.ExtraRecoilPatternData nextData = null;
        while (nextData == null) {
            nextData = list.get(currentIndexAtRecoilPattern);

            if (nextData.shouldSkip()) nextData = null;

            ++currentIndexAtRecoilPattern;

            if (currentIndexAtRecoilPattern >= list.size()) {

                // Non-repeating pattern -> break
                if (!pattern.isRepeatPattern()) break;

                // Repeating pattern, start again at 0
                currentIndexAtRecoilPattern = 0;
            }
        }
        return nextData;
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

    private float calculatePitchUserInput(float currentPitch) {
        return Math.abs(currentPitch - shouldBeLastPitch);
    }

    private float calculatePitchDifference(float currentPitch) {
        float pitchDifference = Math.abs(currentPitch - recoverToPitch);

        if (currentPitch > recoverToPitch) {
            pitchDifference *= -1;
        }
        return pitchDifference;
    }
}