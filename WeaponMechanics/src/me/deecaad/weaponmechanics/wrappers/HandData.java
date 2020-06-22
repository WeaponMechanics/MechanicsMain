package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.weapon.shoot.ShootHandler;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.RecoilTask;
import org.bukkit.Bukkit;

public class HandData {

    private int fullAutoTask;
    private int burstTask;
    private long lastShotTime;
    private String lastShotWeaponTitle;
    private double spreadChange;
    private RecoilTask recoilTask;

    /**
     * Cancels full auto and burst tasks and resets them.
     *
     * Does not cancel recoil task.
     */
    public void cancelTasks() {
        if (fullAutoTask != 0) {
            Bukkit.getScheduler().cancelTask(fullAutoTask);
            fullAutoTask = 0;
        }
        if (burstTask != 0) {
            Bukkit.getScheduler().cancelTask(burstTask);
            burstTask = 0;
        }
    }

    public boolean isUsingFullAuto() {
        return fullAutoTask != 0;
    }

    public void setFullAutoTask(int fullAutoTask) {
        this.fullAutoTask = fullAutoTask;
    }

    public boolean isUsingBurst() {
        return burstTask != 0;
    }

    public void setBurstTask(int burstTask) {
        this.burstTask = burstTask;
    }

    public void setLastShotTime(long lastShotTime) {
        this.lastShotTime = lastShotTime;
    }

    public long getLastShotTime() {
        return lastShotTime;
    }

    public boolean shouldReset() {
        return NumberUtils.hasMillisPassed(lastShotTime, ShootHandler.RESET_MILLIS);
    }

    public String getLastShotWeaponTitle() {
        return lastShotWeaponTitle;
    }

    public void setLastShotWeaponTitle(String lastShotWeaponTitle) {
        this.lastShotWeaponTitle = lastShotWeaponTitle;
    }

    public double getSpreadChange() {
        return spreadChange;
    }

    public void setSpreadChange(double spreadChange) {
        this.spreadChange = spreadChange;
    }

    public RecoilTask getRecoilTask() {
        return recoilTask;
    }

    public void setRecoilTask(RecoilTask recoilTask) {
        this.recoilTask = recoilTask;
    }
}