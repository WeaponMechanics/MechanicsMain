package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.CommandData;
import me.deecaad.core.utils.StringUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MapArgumentType extends CommandArgumentType<Map<String, Object>> {

    private final Map<String, MapValueType<?>> types;

    public MapArgumentType() {
        types = new HashMap<>();
    }

    public MapArgumentType with(String key, MapValueType<?> data) {
        types.put(key, data);
        return this;
    }

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().nbtCompound();
    }

    @Override
    public Map<String, Object> parse(CommandContext<Object> context, String cmd) throws CommandSyntaxException {
        Map<String, Object> nbt = compatibility().getCompound(context, cmd);
        for (String key : nbt.keySet()) {
            Object value = nbt.get(key);

            if (!types.containsKey(key))
                throw new SimpleCommandExceptionType(new LiteralMessage("Unknown argument '" + key + "', did you mean '" + StringUtil.didYouMean(key, types.keySet()) + "'")).create();
            if (!types.get(key).type.isInstance(value))
                throw new SimpleCommandExceptionType(new LiteralMessage("Expected " + types.get(key).type.getSimpleName() + " got '" + value + "'")).create();
        }
        return nbt;
    }

    @Override
    public CompletableFuture<Suggestions> suggestions(CommandContext<Object> context, com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        String current = builder.getRemaining();
        CommandData data = new CommandData(compatibility().getCommandSender(context), new Object[0], builder.getInput(), builder.getRemaining());

        // When the currently types string is empty, simply put an open bracket
        if (current.isEmpty()) {
            builder.suggest("{", new LiteralMessage("Open tag"));
            return builder.buildFuture();
        }

        // So now we deal with this:
        // {ammo:1,firemode:0,skipMainhand:true}
        // Significant chars are '{' and ',' and ':'
        // Lets trace back to one of those and go from there

        int i = traceBack(current, current.length() - 1);
        int j = traceBack(current, i - 1);
        char delimiter = current.charAt(i);

        String allBefore = current.substring(0, i);
        String before = current.substring(i == 0 ? 0 : j + 1, i);
        String after = current.substring(i + 1);

        // Parse all previous arguments so we don't have repeats
        Pattern pattern = Pattern.compile("\\w+:\\w+");
        Matcher matcher = pattern.matcher(allBefore);
        List<String> usesKeys = new ArrayList<>();
        while (matcher.find())
            usesKeys.add(matcher.group().split(":")[0]);

        List<String> suggestions;

        // Currently, user is typing the 'key' part of key:value.
        if (delimiter == '{' || delimiter == ',') {
            if (types.containsKey(after)) {
                suggestions = Collections.singletonList(after + ":");
            } else {
                suggestions = types.keySet()
                        .stream()
                        .filter(s -> s.startsWith(after))
                        .filter(s -> !usesKeys.contains(s)) // filter out repeats
                        .collect(Collectors.toList());
            }
        }

        // Currently, user is typing the 'value' part of key:value.
        else if (delimiter == ':') {
            MapValueType<?> type = types.get(before);
            if (type == null)
                suggestions = Collections.singletonList("Invalid Input");
            else if (type.suggestions.contains(after)) {
                suggestions = Arrays.asList(after + ",", after + "}");
            } else {
                suggestions = type.suggestions
                        .stream()
                        .filter(s -> s.startsWith(after))
                        .collect(Collectors.toList());
            }
        }

        else {
            suggestions = Collections.singletonList("SHOULD NOT HAPPEN");
        }

        suggestions.stream().map(suggestion -> allBefore + delimiter + suggestion).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int traceBack(String str, int start) {
        if (start < 0)
            return 0;

        for (int i = start; i >= 0; i--) {
            if ("{,:".indexOf(str.charAt(i)) != -1) {
                return i;
            }
        }

        MechanicsCore.debug.debug("Could not find any of '" + "{,:" + "' in '" + str + "' from '" + start + "'");
        return -1;
    }



    public static class MapValueType<T> {

        private final Class<?> type;
        private final List<String> suggestions;

        private MapValueType(Class<?> type, Object[] suggestions) {
            this.type = type;
            this.suggestions = Arrays.stream(suggestions).map(Object::toString).collect(Collectors.toList());
        }
    }

    public static MapValueType<Integer> INT(Integer... suggestions) {
        return new MapValueType<>(Integer.class, suggestions);
    }

    public static MapValueType<Double> DOUBLE(Double... suggestions) {
        return new MapValueType<>(Double.class, suggestions);
    }

    public static MapValueType<String> STRING(String... suggestions) {
        return new MapValueType<>(String.class, suggestions);
    }

    public static MapValueType<List> LIST(String... suggestions) {
        return new MapValueType<>(List.class, suggestions);
    }
}
