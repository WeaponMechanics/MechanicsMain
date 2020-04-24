package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.shoot.IShootCompatibility;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.entity.Player;

import java.util.*;
import static me.deecaad.weaponmechanics.WeaponMechanics.getPlayerWrapper;

public class Recoil {

    public static final long MILLIS_BETWEEN_ROTATIONS = 5;
    private static final Timer TIMER = new Timer();
    private static final IShootCompatibility shootCompatibility = CompatibilityAPI.getCompatibility().getShootCompatibility();

    private final long rotationTime;
    private final List<Float> yaws;
    private final List<Float> pitches;
    private final long recoverTime;

    public Recoil(long rotationTime, List<Float> yaws, List<Float> pitches, long recoverTime) {
        this.rotationTime = rotationTime;
        this.yaws = yaws;
        this.pitches = pitches;
        this.recoverTime = recoverTime;
    }

    public void start(Player player) {
        IPlayerWrapper playerWrapper = getPlayerWrapper(player);
        if (rotationTime == 0 && recoverTime == 0) {
            // No need for task as rotation time and recover time are 0
            float rotateYaw = yaws.get(NumberUtils.random(yaws.size()));
            float rotatePitch = pitches.get(NumberUtils.random(pitches.size()));
            shootCompatibility.modifyCameraRotation(playerWrapper.getPlayer(), rotateYaw, rotatePitch, false);
            return;
        }
        RecoilTask recoilTask = playerWrapper.getRecoilTask();
        if (recoilTask == null) {
            // Normally shoot, recoil, recover
            recoilTask = new RecoilTask(playerWrapper, this);
            playerWrapper.setRecoilTask(recoilTask);
            TIMER.scheduleAtFixedRate(recoilTask, 0, MILLIS_BETWEEN_ROTATIONS);
            return;
        }
        // Shoot during recoil
        //  -> continue from current recoil with new one (cancel last recoil)
        // Shoot during recover
        //  -> continue from current recover spot with new recoil (cancel last recover)
        recoilTask.setRecoil(this);
    }

    public long getRotationTime() {
        return rotationTime;
    }

    public List<Float> getYaws() {
        return yaws;
    }

    public List<Float> getPitches() {
        return pitches;
    }

    public long getRecoverTime() {
        return recoverTime;
    }
}