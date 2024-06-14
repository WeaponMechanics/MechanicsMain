package me.deecaad.weaponmechanics.wrappers;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.Nullable;

public class MessageHelper {

    private BossBar bossBar;
    private WrappedTask bossBarTask;
    private WrappedTask expTask;

    public @Nullable BossBar getBossBar() {
        return bossBar;
    }

    public void setBossBar(@Nullable BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public @Nullable WrappedTask getBossBarTask() {
        return bossBarTask;
    }

    public void setBossBarTask(@Nullable WrappedTask bossBarTask) {
        this.bossBarTask = bossBarTask;
    }

    public @Nullable WrappedTask getExpTask() {
        return expTask;
    }

    public void setExpTask(@Nullable WrappedTask expTask) {
        this.expTask = expTask;
    }
}