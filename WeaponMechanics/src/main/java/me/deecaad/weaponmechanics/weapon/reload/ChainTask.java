package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class ChainTask extends BukkitRunnable {

    private final int delay;
    private ChainTask nextTask;

    public ChainTask(int delay) {
        this.delay = delay;
    }

    /**
     * @param nextTask the next task in the chain
     * @return the next task that was set
     */
    public ChainTask setNextTask(ChainTask nextTask) {
        this.nextTask = nextTask;
        return nextTask;
    }

    /**
     * @return the delay after this task should be executed
     */
    public int getDelay() {
        return this.delay;
    }

    /**
     * Starts the chain of tasks
     */
    public void startChain() {
        runTaskLater(WeaponMechanics.getPlugin(), getDelay());
        setup();
    }

    @Override
    public void run() {
        task();

        if (nextTask == null)
            return;
        this.nextTask.runTaskLater(WeaponMechanics.getPlugin(), this.nextTask.getDelay());
        this.nextTask.setup();
    }

    public boolean hasNext() {
        return this.nextTask != null;
    }

    /**
     * Does stuff when chain task is run
     */
    public abstract void task();

    /**
     * Setup this task right after its delay counter has started (e.g. store task id somewhere)
     */
    public abstract void setup();
}