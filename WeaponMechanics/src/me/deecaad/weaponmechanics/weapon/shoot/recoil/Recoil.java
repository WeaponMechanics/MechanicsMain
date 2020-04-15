package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.entity.Player;

import java.util.*;
import static me.deecaad.weaponmechanics.WeaponMechanics.getPlayerWrapper;

public class Recoil {

    public static final long MILLIS_BETWEEN_ROTATIONS = 5;
    private static final Timer TIMER = new Timer();

    private long rotationTime;
    private List<Float> yaws;
    private List<Float> pitches;
    private long recoverTime;

    public Recoil(long rotationTime, List<Float> yaws, List<Float> pitches, long recoverTime) {
        this.rotationTime = rotationTime;
        this.yaws = yaws;
        this.pitches = pitches;
        this.recoverTime = recoverTime;
    }

    public void start(Player player) {
        IPlayerWrapper playerWrapper = getPlayerWrapper(player);
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