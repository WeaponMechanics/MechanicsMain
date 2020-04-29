package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.weapon.shoot.ShootHandler;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.RecoilTask;

public class HandData {

    private boolean usingFullAuto;
    private long lastShotTime;
    private String lastShotWeaponTitle;
    private double spreadChange;
    private RecoilTask recoilTask;

    public boolean isUsingFullAuto() {
        return usingFullAuto;
    }

    public void setUsingFullAuto(boolean usingFullAuto) {
        this.usingFullAuto = usingFullAuto;
    }

    public long getLastShotTime() {
        return lastShotTime;
    }

    public void updateLastShotTime() {
        this.lastShotTime = System.currentTimeMillis();
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