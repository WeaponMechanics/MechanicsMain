package me.deecaad.core.effects;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEffect implements Effect, StringSerializable<Effect> {

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

    @Override
    public AbstractEffect serialize(Map<String, Object> args) {
        delay = (Integer) args.get("delay");
        repeatAmount = (Integer) args.get("repeat");
        repeatInterval = (Integer) args.get("repeatInterval");
        return this;
    }

    @Override
    public Map<String, Object> getDefaults() {
        Map<String, Object> temp = new HashMap<>();
        temp.put("delay", 0);
        temp.put("repeat", 1);
        temp.put("repeatInterval", 20);
        return temp;
    }
}
