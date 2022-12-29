package me.deecaad.core.mechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.file.inline.InlineSerializer;
import me.deecaad.core.file.inline.types.IntegerType;
import me.deecaad.core.file.serializers.VectorSerializer;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.targeters.TargetTargeter;
import me.deecaad.core.mechanics.targeters.Targeter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Mechanic is the most powerful tool available to server-admins through
 * config. A Mechanic is an action, basically a block of code, that the admin
 * writes in YAML format. These actions can be executed conditionally using
 * {@link Condition} and can get specifically targeted using {@link me.deecaad.core.mechanics.targeters.Targeter}.
 */
public abstract class Mechanic extends InlineSerializer<Mechanic> {

    // Inline arguments
    public static final Argument REPEAT_AMOUNT = new Argument("repeatAmount", new IntegerType(1), 1);
    public static final Argument REPEAT_INTERVAL = new Argument("repeatInterval", new IntegerType(0), 0);
    public static final Argument DELAY_BEFORE_PLAY = new Argument("delayBeforePlay", new IntegerType(0), 0);

    // package-private for serialization phase
    Targeter targeter;
    List<Condition> conditions;
    private int repeatAmount;
    private int repeatInterval;
    private int delayBeforePlay;

    /**
     * Default constructor for serializer.
     */
    public Mechanic() {
        // DO NOT USE THIS CONSTRUCTOR IN YOUR MECHANICS!!!
        // call super(args) instead
    }

    /**
     * Map constructor for inline-serializer.
     */
    public Mechanic(Map<Argument, Object> args) {
        repeatAmount = (int) args.get(REPEAT_AMOUNT);
        repeatInterval = (int) args.get(REPEAT_INTERVAL);
        delayBeforePlay = (int) args.get(DELAY_BEFORE_PLAY);
    }

    @Override
    public ArgumentMap args() {
        return new ArgumentMap(REPEAT_AMOUNT, REPEAT_INTERVAL, DELAY_BEFORE_PLAY);
    }

    /**
     * This method will trigger this Mechanic.
     *
     * <p>This method <i>probably shouldn't</i> be overridden. It handles the
     * global repeat, delay, targeter, and condition code. The behavior of the
     * specific Mechanic is handled by the protected {@link #use0(CastData)}
     * method.
     *
     * @param cast The non-null cast data.
     */
    public final void use(CastData cast) {

        // If there is no need to schedule event, skip the event process.
        if (repeatAmount == 1 && repeatInterval == 0 && delayBeforePlay == 0) {
            OUTER:
            for (CastData target : targeter.getTargets(cast)) {
                for (Condition condition : conditions)
                    if (!condition.isAllowed(target))
                        continue OUTER;

                use0(cast);
            }
            return;
        }

        // Schedule a repeating event to trigger the mechanic multiple times.
        int task = new BukkitRunnable() {
            int runs = 0;

            @Override
            public void run() {
                if (runs++ >= repeatAmount)
                    cancel();

                OUTER:
                for (CastData target : targeter.getTargets(cast)) {
                    for (Condition condition : conditions)
                        if (!condition.isAllowed(target))
                            continue OUTER;

                    use0(cast);
                }
            }
        }.runTaskTimer(MechanicsCore.getPlugin(), delayBeforePlay, repeatInterval).getTaskId();

        // This allows developers to consume task ids from playing a Mechanic.
        // Good for canceling tasks early.
        if (cast.getTaskIdConsumer() != null)
            cast.getTaskIdConsumer().accept(task);
    }

    /**
     * This method should be overridden to define the behavior of the Mechanic.
     * For example, a Potion mechanic may use the {@link CastData#getTarget()}
     * method to apply a potion effect.
     *
     * @param cast The non-null data including source/target information.
     */
    protected abstract void use0(CastData cast);
}