package me.deecaad.core.mechanics;

import me.deecaad.core.effects.Delayable;
import me.deecaad.core.effects.Repeatable;
import me.deecaad.core.mechanics.serialization.Argument;
import me.deecaad.core.mechanics.serialization.StringSerializable;
import me.deecaad.core.mechanics.targeters.Targetable;
import me.deecaad.core.mechanics.targeters.Targeter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Mechanic extends StringSerializable<Mechanic> implements Targetable, Delayable, Repeatable {

    private Targeter<?> targeter;

    private int delay;
    private int repeatAmount;
    private int repeatInterval;

    public Mechanic(String name, Argument... args) {
        super(name, args);

        this.targeter = targeter;
        this.delay = delay;
        this.repeatAmount = repeatAmount;
        this.repeatInterval = repeatInterval;
    }

    public Mechanic(String name, Collection<? extends Argument> args) {
        super(name, args);

        this.targeter = targeter;
        this.delay = delay;
        this.repeatAmount = repeatAmount;
        this.repeatInterval = repeatInterval;
    }

    public Targeter<?> getTargeter() {
        return targeter;
    }

    public void setTargeter(Targeter<?> targeter) {
        this.targeter = targeter;
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

    protected long getInterval(int i) {
        return delay + i * repeatInterval;
    }

    public void cast(MechanicCaster caster) {
        List<?> targets = targeter.getTargets(caster, new ArrayList<>());

        for (Object obj : targets) {
            if (obj instanceof Player) {
                cast(caster, (Player) obj);
            } else if (obj instanceof Entity) {
                cast(caster, (Entity) obj);
            } else if (obj instanceof Location) {
                cast(caster, (Location) obj);
            }
        }
    }
}