package me.deecaad.core.effects;

public abstract class AbstractEffect implements Effect, StringSerializable<AbstractEffect> {

    private int repeatAmount;
    private int repeatInterval;
    private int delay;

    public AbstractEffect() {
        repeatAmount = 1;
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
}
