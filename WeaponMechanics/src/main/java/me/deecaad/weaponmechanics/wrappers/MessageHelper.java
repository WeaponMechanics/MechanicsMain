package me.deecaad.weaponmechanics.wrappers;

import net.kyori.adventure.bossbar.BossBar;

public class MessageHelper {

    private BossBar bossBar;
    private int bossBarTask;
    private int expTask;

    public BossBar getBossBar() {
        return bossBar;
    }

    public void setBossBar(BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public int getBossBarTask() {
        return bossBarTask;
    }

    public void setBossBarTask(int bossBarTask) {
        this.bossBarTask = bossBarTask;
    }

    public int getExpTask() {
        return expTask;
    }

    public void setExpTask(int expTask) {
        this.expTask = expTask;
    }
}