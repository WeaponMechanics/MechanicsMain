package me.deecaad.core.mechanics;

import me.deecaad.core.file.*;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.targeters.SourceTargeter;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.deecaad.core.file.InlineSerializer.NAME_FINDER;

public class Mechanics implements Serializer<Mechanics> {

    public static final Registry<Mechanic> MECHANICS = new Registry<>("Mechanic");
    public static final Registry<Targeter> TARGETERS = new Registry<>("Targeter");
    public static final Registry<Condition> CONDITIONS = new Registry<>("Condition");

    private List<Mechanic> mechanics;

    /**
     * Default constructor for serializer.
     */
    public Mechanics() {
    }

    public Mechanics(List<Mechanic> mechanics) {
        this.mechanics = mechanics;
    }

    public List<Mechanic> getMechanics() {
        return mechanics;
    }

    @Override
    public String getKeyword() {
        return "Mechanics";
    }

    @Nullable
    @Override
    public String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/Mechanics";
    }

    public void use(CastData cast) {
        for (Mechanic mechanic : mechanics) {
            mechanic.use(cast);
        }
    }

    @NotNull
    @Override
    public Mechanics serialize(SerializeData data) throws SerializerException {
        List<?> list = data.config.getList(data.key);
        List<Mechanic> mechanics = new ArrayList<>();

        if (list == null) {
            throw data.exception(null, "Could not find any list... Are you still using the outdated Mechanics format?");
        }

        for (Object obj : list) {
            Mechanic mechanic = serializeOne(data, obj.toString());
            mechanics.add(mechanic);
        }

        return new Mechanics(mechanics);
    }

    public Mechanic serializeOne(SerializeData data, String line) throws SerializerException {

        // So here is the problem:
        // instead of just 'foo(arg1=hello)', we have 'foo(...) @... ?...',
        // and we need to use regex to find the targeter and the conditions.

        // This pattern groups everything and stops whenever it hits a '?', '@',
        // or the end of the string. This perfectly separates each section.
        // All the backslash stuff is for handling escaped \? and \@
        Pattern pattern = Pattern.compile(".+?(?=(?<!\\\\)(?:\\\\\\\\)*[?@]|$)");
        Matcher matcher = pattern.matcher(line);

        Mechanic mechanic = null;
        Targeter targeter = null;
        List<Condition> conditions = new LinkedList<>();

        while (matcher.find()) {
            int index = matcher.start();
            String group = matcher.group();

            try {
                switch (group.charAt(0)) {
                    case '@' -> {
                        // Already have a targeter, user shouldn't have multiple
                        if (targeter != null)
                            throw data.exception(null, "Found multiple targeters with '@'", "Instead of using multiple targeters on the same line, try putting your mechanics on separate lines");

                        // Try to figure out the name of the target. For example:
                        // '@EntitiesInRadius(radius=10)' => '@EntitiesInRadius'
                        Matcher nameMatcher = NAME_FINDER.matcher(group);
                        if (!nameMatcher.find())
                            throw data.exception(null, "Could not determine the name of the targeter", SerializerException.forValue(group));

                        String temp = nameMatcher.group().substring(1);
                        targeter = TARGETERS.get(temp);
                        if (targeter == null)
                            throw new SerializerOptionsException("", "@Targeter", TARGETERS.getOptions(), temp, "");

                        // We need to call the 'inlineFormat' method since the
                        // current targeter object is just an empty serializer.
                        Map<String, MapConfigLike.Holder> args = InlineSerializer.inlineFormat(group.substring(1));
                        SerializeData nested = new SerializeData(targeter, data.file, null, new MapConfigLike(args).setDebugInfo(data.file, data.key, line));
                        targeter = targeter.serialize(nested);
                    }

                    case '?' -> {
                        // Try to figure out the name of the condition. For example:
                        // '?entity(PLAYER)' => '?entity'
                        Matcher nameMatcher = NAME_FINDER.matcher(group);
                        if (!nameMatcher.find())
                            throw new InlineSerializer.FormatException(0, "Could not determine the name of the condition");

                        String temp = nameMatcher.group().substring(1);
                        Condition condition = Mechanics.CONDITIONS.get(temp);

                        // We need to call the 'inlineFormat' method since the
                        // current condition object is just an empty serializer.
                        Map<String, MapConfigLike.Holder> args = InlineSerializer.inlineFormat(group.substring(1));
                        SerializeData nested = new SerializeData(condition, data.file, null, new MapConfigLike(args).setDebugInfo(data.file, data.key, line));
                        conditions.add(condition.serialize(nested));
                    }

                    default -> {
                        // Already have a mechanic, so the user probably mis-labeled something
                        if (mechanic != null)
                            throw data.exception(null, "Found multiple targeters with '@'", "Instead of using multiple targeters on the same line, try putting your mechanics on separate lines");

                        // Try to figure out the name of the target. For example:
                        // '@EntitiesInRadius(radius=10)' => '@EntitiesInRadius'
                        Matcher nameMatcher = NAME_FINDER.matcher(group);
                        if (!nameMatcher.find())
                            throw data.exception(null, "Could not determine the name of the targeter", SerializerException.forValue(group));

                        String temp = nameMatcher.group();
                        mechanic = MECHANICS.get(temp);
                        if (mechanic == null)
                            throw new SerializerOptionsException("", "Mechanic", MECHANICS.getOptions(), temp, "");

                        // We need to call the 'inlineFormat' method since the
                        // current mechanic object is just an empty serializer.
                        Map<String, MapConfigLike.Holder> args = InlineSerializer.inlineFormat(group.substring(1));
                        SerializeData nested = new SerializeData(mechanic, data.file, null, new MapConfigLike(args).setDebugInfo(data.file, data.key, line));
                        mechanic = mechanic.serialize(nested);
                    }
                }

                if (mechanic == null)
                    throw new InlineSerializer.FormatException(0, "Could not determine mechanic");

            } catch (InlineSerializer.FormatException ex) {
                String indent = "    ";
                throw data.exception(null, ex.getMessage(), indent + line,
                        StringUtil.repeat(" ", index + ex.getIndex() + indent.length() - 1) + "^");
            }
        }

        // Always default to the source targeter
        if (targeter == null)
            targeter = new SourceTargeter();

        mechanic.targeter = targeter;
        mechanic.conditions = conditions;
        return mechanic;
    }
}