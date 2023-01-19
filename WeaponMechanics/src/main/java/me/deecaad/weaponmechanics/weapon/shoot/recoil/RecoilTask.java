package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.compatibility.IWeaponCompatibility;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.TimerTask;

public class RecoilTask extends TimerTask {

    private static final IWeaponCompatibility weaponCompatibility = WeaponCompatibilityAPI.getWeaponCompatibility();
    private final PlayerWrapper playerWrapper;
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
    private long recoverTime;

    public RecoilTask(PlayerWrapper playerWrapper, HandData handData, Recoil recoil) {
        this.playerWrapper = playerWrapper;
        this.handData = handData;
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

        // This first check in case non-repeating pattern is used, and it has reached its end -> don't send unnecessary packets
        if (!(yawPerIteration == 0 && pitchPerIteration == 0)) {
            if (isRotating) {
                Location location = playerWrapper.getPlayer().getLocation();
                float pitch = location.getPitch() < -80 ? 0 : pitchPerIteration;
                weaponCompatibility.modifyCameraRotation(playerWrapper.getPlayer(), yawPerIteration, pitch, false);
            } else {
                // Let recovering happen normally without checking any maximum pitch changes
                weaponCompatibility.modifyCameraRotation(playerWrapper.getPlayer(), yawPerIteration, pitchPerIteration, false);
            }
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


            if (yawPerIteration == 0 && pitchPerIteration == 0) {
                // Non-repeating pattern which reached its end was used, meaning we don't really want to do recovery anymore
                yawPerIteration = 0;
                pitchPerIteration = 0;
            } else {
                // Multiply back to original push time
                yawPerIteration *= -rotations;
                pitchPerIteration *= -rotations;

                // Then recalculate the yaw and pitch per iteration based on recover time
                rotations = (int) (recoverTime / Recoil.MILLIS_BETWEEN_ROTATIONS);
                yawPerIteration = yawPerIteration / rotations;
                pitchPerIteration = pitchPerIteration / rotations;
            }

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

        boolean nonRepeatingPatternReachedEnd = false;
        RecoilPattern pattern = tempRecoil.getRecoilPattern();
        if (pattern != null) {
            RecoilPattern.ExtraRecoilPatternData nextData = getNext(pattern);
            if (nextData != null) {
                rotateYaw = nextData.horizontalRecoil();
                rotatePitch = nextData.verticalRecoil();
            } else {
                nonRepeatingPatternReachedEnd = true;
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

        // If its non-repeating pattern which reached end these would be 0
        if (!nonRepeatingPatternReachedEnd && rotateYaw == 0 && rotatePitch == 0) {
            // Neither one wasn't used?
            // Terminate this task...
            handData.setRecoilTask(null);
            cancel();
            return true;
        }

        ModifyRecoilWhen modifyRecoilWhen = tempRecoil.getModifyRecoilWhen();
        if (modifyRecoilWhen != null) {
            if (rotateYaw != 0) {
                rotateYaw = (float) modifyRecoilWhen.applyChanges(playerWrapper, rotateYaw);
            }
            if (rotatePitch != 0) {
                rotatePitch = (float) modifyRecoilWhen.applyChanges(playerWrapper, rotatePitch);
            }
        }

        long pushTime = tempRecoil.getPushTime();

        if (pushTime == 0) {
            // Meaning rotation should be instant
            rotations = 1;
            yawPerIteration = rotateYaw;
            pitchPerIteration = rotatePitch;
        } else if (nonRepeatingPatternReachedEnd) {
            // We don't want to do anything when its non-repeating pattern
            rotations = (int) (pushTime / Recoil.MILLIS_BETWEEN_ROTATIONS);
            yawPerIteration = 0;
            pitchPerIteration = 0;
        } else {
            rotations = (int) (pushTime / Recoil.MILLIS_BETWEEN_ROTATIONS);
            yawPerIteration = rotateYaw / rotations;
            pitchPerIteration = rotatePitch / rotations;
        }

        recoverTime = tempRecoil.getRecoverTime();
        tempRecoil = null;
        counter = 0;
        isRotating = true;
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
}