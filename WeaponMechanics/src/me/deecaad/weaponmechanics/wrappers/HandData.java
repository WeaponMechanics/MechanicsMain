package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.shoot.ShootHandler;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.RecoilTask;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class HandData {

    private final IEntityWrapper entityWrapper;

    private int fullAutoTask;
    private int burstTask;
    private long lastShotTime;
    private String lastShotWeaponTitle;
    private double spreadChange;
    private RecoilTask recoilTask;
    private final List<Integer> reloadTasks = new ArrayList<>();
    private ZoomData zoomData;
    private int shootFirearmActionTask;

    public HandData(IEntityWrapper entityWrapper) {
        this.entityWrapper = entityWrapper;
    }

    /**
     * Cancels following things
     * - Full auto
     * - Burst
     * - Reload tasks
     * - Shoot firearm action task
     * - Zooming
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
        if (!reloadTasks.isEmpty()) {
            reloadTasks.forEach(task -> Bukkit.getScheduler().cancelTask(task));
            reloadTasks.clear();
        }
        if (shootFirearmActionTask != 0) {
            Bukkit.getScheduler().cancelTask(shootFirearmActionTask);
            shootFirearmActionTask = 0;
        }
        if (getZoomData().isZooming()) {
            WeaponMechanics.getWeaponHandler().getScopeHandler().forceZoomOut(entityWrapper, zoomData);
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

    public void addReloadTask(int reloadTask) {
        this.reloadTasks.add(reloadTask);
    }

    public boolean isReloading() {
        return !reloadTasks.isEmpty();
    }

    public void stopReloadingTasks() {
        if (!reloadTasks.isEmpty()) {
            reloadTasks.forEach(task -> Bukkit.getScheduler().cancelTask(task));
            reloadTasks.clear();
        }
    }

    public ZoomData getZoomData() {
        return zoomData == null ? zoomData = new ZoomData() : zoomData;
    }

    public int getShootFirearmActionTask() {
        return shootFirearmActionTask;
    }

    public void setShootFirearmActionTask(int shootFirearmActionTask) {
        this.shootFirearmActionTask = shootFirearmActionTask;
    }
}