package me.deecaad.weaponmechanics.wrappers;

import com.cjcrafter.foliascheduler.TaskImplementation;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.Nullable;

public class MessageHelper {

    private BossBar bossBar;
    private TaskImplementation bossBarTask;
    private TaskImplementation expTask;

    public @Nullable BossBar getBossBar() {
        return bossBar;
    }

    public void setBossBar(@Nullable BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public @Nullable TaskImplementation getBossBarTask() {
        return bossBarTask;
    }

    public void setBossBarTask(@Nullable TaskImplementation bossBarTask) {
        this.bossBarTask = bossBarTask;
    }

    public @Nullable TaskImplementation getExpTask() {
        return expTask;
    }

    public void setExpTask(@Nullable TaskImplementation expTask) {
        this.expTask = expTask;
    }
}