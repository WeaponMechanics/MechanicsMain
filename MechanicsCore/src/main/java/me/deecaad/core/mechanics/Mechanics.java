package me.deecaad.core.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.targeters.SourceTargeter;
import me.deecaad.core.mechanics.targeters.TargetTargeter;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mechanics implements Serializer<Mechanics> {

    public static final Registry<Mechanic> MECHANICS = new Registry<>();
    public static final Registry<Targeter> TARGETERS = new Registry<>();
    public static final Registry<Condition> CONDITIONS = new Registry<>();

    private List<Mechanic> mechanics;

    /**
     * Default constructor for serializer.
     */
    public Mechanics() {
    }

    public Mechanics(List<Mechanic> mechanics) {
        this.mechanics = mechanics;
    }

    @Override
    public String getKeyword() {
        return "Mechanics";
    }

    public void use(CastData cast) {
        for (Mechanic mechanic : mechanics) {
            mechanic.use(cast);
        }
    }

    @NotNull
    @Override
    public Mechanics serialize(SerializeData data) throws SerializerException {
        List<Mechanic> mechanics = new LinkedList<>();

        if (data.config.isConfigurationSection(data.key)) {

        } else if (data.config.isList(data.key)) {
            for (String line : data.config.getStringList(data.key)) {
                try {
                    mechanics.add(serializeOne(line));
                } catch (InlineException ex) {
                    boolean isIndexAccurate = true;
                    int index = ex.getIndex();
                    if (index == -1) {
                        if (ex.getLookAfter() != null) index = line.indexOf(ex.getLookAfter());
                        index = line.indexOf(ex.getIssue(), index == -1 ? 0 : index);
                        isIndexAccurate = false;
                    }

                    String prefix = isIndexAccurate ? "Error happened here: " : "Error might be here: ";
                    ex.getException().addMessage(prefix + line);
                    if (index != -1) ex.getException().addMessage(StringUtil.repeat(" ", index + prefix.length()) + "^");

                    throw ex.getException();
                }
            }
        }

        // Extra check to see if the user has extra mechanics in config, when
        // it is better to leave it empty (So the config doesn't store anything).
        if (mechanics.isEmpty())
            throw data.exception(null, "Found an empty list of Mechanics, was this intentional?",
                    "Instead of using an empty list, please delete the option from config");

        return new Mechanics(mechanics);
    }

    public Mechanic serializeOne(String line) throws InlineException {

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
            targeter = new SourceTargeter(args);
        }

        mechanic.targeter = targeter;
        mechanic.conditions = conditions;
        return mechanic;
    }
}