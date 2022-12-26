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

    private Targeter targeter;
    private List<Condition> conditions;
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
    @SuppressWarnings("unchecked")
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
            use0(cast);
            return;
        }

        // Schedule a repeating event to trigger the mechanic multiple times.
        int task = new BukkitRunnable() {
            int runs = 0;

            @Override
            public void run() {
                if (runs++ >= repeatAmount)
                    cancel();

                List<CastData> targets = targeter.getTargets(cast);
                OUTER:
                for (CastData target : targets) {
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


    @Override
    public final Mechanic expandedFormat(SerializeData data) {
        Mechanic mechanic = super.expandedFormat(data);
        return mechanic;
    }

    @Override
    public final Mechanic inlineFormat(String line) throws InlineException {

        // So here is the problem:
        // instead of just 'foo(arg1=hello)', we have 'foo(...) @... ?...',
        // and we need to use regex to find the targeter and the conditions.

        // This pattern groups everything and stops whenever it hits a '?', '@',
        // or the end of the string. This perfectly separates each section.
        // All the backslash stuff is for handling escaped \? and \@
        Pattern pattern = Pattern.compile(".+?(?=(?<!\\\\)(?:\\\\\\\\)*[?@]|$)");
        Matcher matcher = pattern.matcher(line);

        // This patterns groups the start of the string until the first '(' or
        // the end of the string. This is used to determine WHICH mechanic/
        // targeter/condition the user is trying to use.
        Pattern nameFinder = Pattern.compile(".+?(?=\\()");

        Mechanic mechanic = null;
        Targeter targeter = null;
        List<Condition> conditions = new LinkedList<>();

        while (matcher.find()) {
            int index = matcher.start();
            String group = matcher.group();

            switch (group.charAt(0)) {
                case '@' -> {
                    // Already have a targeter, user shouldn't have multiple
                    if (targeter != null)
                        throw new InlineException(index, new SerializerException("", new String[]{"Found multiple targeters with '@'", "Instead of using multiple targeters on the same line, try putting your mechanics on separate lines"}, ""));

                    // Try to figure out the name of the target. For example:
                    // '@EntitiesInRadius(radius=10)' => '@EntitiesInRadius'
                    Matcher nameMatcher = nameFinder.matcher(group);
                    if (!nameMatcher.find())
                        throw new InlineException(index + 1, new SerializerOptionsException("", "@Targeter", Mechanics.TARGETERS.getOptions(), group, ""));

                    String temp = nameMatcher.group().substring(1);
                    targeter = Mechanics.TARGETERS.get(temp);
                    try {
                        // We need to call the 'inlineFormat' method since the
                        // current targeter object is just an empty serializer.
                        targeter = targeter.inlineFormat(group);
                    } catch (InlineException ex) {
                        if (ex.getIndex() != -1)
                            ex.setIndex(ex.getIndex() + index);
                        ex.setOffset(ex.getOffset() + index);
                        throw ex;
                    }
                }

                case '?' -> {
                    // Try to figure out the name of the condition. For example:
                    // '?entity(PLAYER)' => '?entity'
                    Matcher nameMatcher = nameFinder.matcher(group);
                    if (!nameMatcher.find())
                        throw new InlineException(index + 1, new SerializerOptionsException("", "?Condition", Mechanics.CONDITIONS.getOptions(), group, ""));

                    String temp = nameMatcher.group().substring(1);
                    Condition condition = Mechanics.CONDITIONS.get(temp);
                    try {
                        // We need to call the 'inlineFormat' method since the
                        // current condition object is just an empty serializer.
                        conditions.add(condition.inlineFormat(group));
                    } catch (InlineException ex) {
                        if (ex.getIndex() != -1)
                            ex.setIndex(ex.getIndex() + index);
                        ex.setOffset(ex.getOffset() + index);
                        throw ex;
                    }
                }

                default -> {
                    if (mechanic != null)
                        throw new InlineException(index, new SerializerException("", new String[]{"Found multiple mechanics... If this is a condition or target, make sure it starts with '?' or '@' respectively"}, ""));

                    // Try to figure out the name of the mechanic. For example:
                    // 'potion(POISON)' => 'potion'
                    Matcher nameMatcher = nameFinder.matcher(group);
                    if (!nameMatcher.find())
                        throw new InlineException(index, new SerializerOptionsException("", "Mechanic", Mechanics.CONDITIONS.getOptions(), group, ""));

                    String temp = nameMatcher.group();
                    mechanic = Mechanics.MECHANICS.get(temp);
                    try {
                        // We need to call the 'inlineFormat' method since the
                        // current condition object is just an empty serializer.
                        mechanic = mechanic.inlineFormat(group);
                    } catch (InlineException ex) {
                        if (ex.getIndex() != -1)
                            ex.setIndex(ex.getIndex() + index);
                        ex.setOffset(ex.getOffset() + index);
                        throw ex;
                    }
                }
            }
        }

        if (mechanic == null)
            throw new InlineException(0, new SerializerException("", new String[] {"Could not find any Mechanic in the line"}, ""));
        if (targeter == null) {
            Map<Argument, Object> args = new HashMap<>();
            args.put(Targeter.OFFSET, null);
            targeter = new TargetTargeter(args);
        }

        mechanic.targeter = targeter;
        mechanic.conditions = conditions;
        return mechanic;
    }
}