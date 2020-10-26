package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.core.utils.NumberUtils;
import org.bukkit.boss.BossBar;

import java.util.List;

public class MessageHelper {

    private List<Integer> messageTasks;

    private long denyActionBarStart;
    private long denyActionBarTime;
    private long denyTitleStart;
    private long denyTitleTime;

    private BossBar currentInfoBossBar;
    private int currentInfoBossBarTask;

    private int expTask;

    private long itemUpdateTime;

    public void updateActionBarTime(int forTicks) {
        denyActionBarStart = System.currentTimeMillis();
        denyActionBarTime = forTicks * 50;
    }

    public boolean denyInfoActionBar() {
        return !NumberUtils.hasMillisPassed(denyActionBarStart, denyActionBarTime);
    }

    public void updateTitleTime(int forTicks) {
        denyTitleStart = System.currentTimeMillis();
        denyTitleTime = forTicks * 50;
    }

    public boolean denyInfoTitle() {
        return !NumberUtils.hasMillisPassed(denyTitleStart, denyTitleTime);
    }

    public BossBar getCurrentInfoBossBar() {
        return currentInfoBossBar;
    }

    public void setCurrentInfoBossBar(BossBar currentInfoBossBar) {
        this.currentInfoBossBar = currentInfoBossBar;
    }

    public int getCurrentInfoBossBarTask() {
        return currentInfoBossBarTask;
    }

    public void setCurrentInfoBossBarTask(int currentInfoBossBarTask) {
        this.currentInfoBossBarTask = currentInfoBossBarTask;
    }

    public int getExpTask() {
        return expTask;
    }

    public void setExpTask(int expTask) {
        this.expTask = expTask;
    }

    public boolean allowItemUpdate() {
        return NumberUtils.hasMillisPassed(itemUpdateTime, 5000);
    }

    public void updateItemTime() {
        itemUpdateTime = System.currentTimeMillis();
    }
}