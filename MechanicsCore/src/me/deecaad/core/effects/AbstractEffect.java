package me.deecaad.core.effects;

import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEffect implements Effect {

    private int repeatAmount;
    private int repeatInterval;
    private int delay;
    private Vector offset;

    public AbstractEffect() {
        repeatAmount = 1;
        offset = new Vector();
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public int getRepeatAmount() {
        return repeatAmount;
    }

    @Override
    public void setRepeatAmount(int repeatAmount) {
        this.repeatAmount = repeatAmount;
    }

    @Override
    public int getRepeatInterval() {
        return repeatInterval;
    }

    @Override
    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    @Override
    public Vector getOffset() {
        return offset;
    }

    @Override
    public void setOffset(Vector offset) {
        this.offset = offset;
    }
}
