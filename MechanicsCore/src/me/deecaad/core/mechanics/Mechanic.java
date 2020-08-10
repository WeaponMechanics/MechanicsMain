package me.deecaad.core.mechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.effects.Delayable;
import me.deecaad.core.effects.Repeatable;
import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.SerializerData;
import me.deecaad.core.mechanics.serialization.StringSerializable;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

@SerializerData(name = "MECHANIC_NO_USE", args = {"delay~INTEGER", "repeatAmount~INTEGER~repeat", "repeatInterval~INTEGER~interval"})
public abstract class Mechanic implements StringSerializable<Mechanic>, Targetable, Delayable, Repeatable {

    private Targeter<?> targeter;

    protected int delay;
    protected int repeatAmount = 1; // Everything still works if this is 0, fyi
    protected int repeatInterval;

    public Mechanic() {
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

    @Override
    public Mechanic serialize(Map<String, Object> data) {
        delay = (int) data.getOrDefault("delay", 0);
        repeatAmount = (int) data.getOrDefault("repeatAmount", 1);
        repeatInterval = (int) data.getOrDefault("repeatInterval", 0);
        return this;
    }

    /**
     *
     *
     * If a <code>Mechanic</code> can be run async, it should
     * override this method to be run async
     *
     * @param caster What is casting the mechanic
     * @return The bukkit task id of the mechanic casted
     */
    public int cast(MechanicCaster caster) {
        return new BukkitRunnable() {
            
            // How many times this has been looped
            int counter = 0;
            
            @Override
            public void run() {
                List<?> targets;
                try {
                    targets = targeter.getTargets(caster);
                } catch (Exception e) {
                    debug.log(LogLevel.ERROR, "Unhandled exception caugh during target receiving: ", e);
                    return;
                }

                if (debug.canLog(LogLevel.DEBUG))
                    debug.debug("Mechanic " + Mechanic.this.toString() + " casted at " + targets);

                // This is valid behavoir of targeters if nothing is targeted
                if (targets == null) {
                    return;
                }

                // Cast the mechanic at each target
                for (Object obj : targets) {
                    if (obj == null)
                        continue;

                    try {
                        if (obj instanceof Player) {
                            cast(caster, (Player) obj);
                        } else if (obj instanceof Entity) {
                            cast(caster, (Entity) obj);
                        } else if (obj instanceof Location) {
                            cast(caster, (Location) obj);
                        }
                    } catch (Exception e) {
                        debug.log(LogLevel.ERROR, "Unhandled exception caught during mechanic casting: ", e);
                    }
                }

                // Cancel once there have been enough loops
                if (++counter >= repeatAmount) {
                    cancel();
                }
            }
        }.runTaskTimer(MechanicsCore.getPlugin(), delay, repeatInterval).getTaskId();
    }
}