package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import me.deecaad.core.commands.CommandData;
import me.deecaad.core.commands.SuggestionsBuilder;
import me.deecaad.core.commands.Tooltip;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MapArgumentType extends CommandArgumentType<Map<String, Object>> {

    public static final Function<Function<CommandData, Tooltip[]>, MapValueType<Integer>> INT =
            (suggestions) -> new MapValueType<>(Integer.class, suggestions);
    public static final Function<Function<CommandData, Tooltip[]>, MapValueType<Integer>> DOUBLE =
            (suggestions) -> new MapValueType<>(Double.class, suggestions);
    public static final Function<Function<CommandData, Tooltip[]>, MapValueType<Integer>> STRING =
            (suggestions) -> new MapValueType<>(String.class, suggestions);
    public static final Function<Function<CommandData, Tooltip[]>, MapValueType<Integer>> LIST =
            (suggestions) -> new MapValueType<>(List.class, suggestions);
    public static final Function<Function<CommandData, Tooltip[]>, MapValueType<Integer>> MAP =
            (suggestions) -> new MapValueType<>(Map.class, suggestions);

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
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(key);
            if (!types.get(key).type.isInstance(value))
                throw new SimpleCommandExceptionType(new LiteralMessage("Expected " + types.get(key).type.getSimpleName() + " got '" + value + "'")).create();
        }
        return nbt;
    }

    @Override
    public CompletableFuture<Suggestions> suggestions(CommandContext<Object> context, com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        String current = builder.getRemaining();
        CommandData data = new CommandData(compatibility().getCommandSender(context), new Object[0], builder.getInput(), builder.getRemaining());

        if (current.isEmpty()) {
            builder.suggest("{", new LiteralMessage("Open tag"));
            return builder.buildFuture();
        }

        char delimiter = 0;
        int stop;
        for (stop = current.length() - 1; stop >= 0 && "{,:".indexOf(delimiter) == -1; stop--)
            delimiter = current.charAt(stop);

        switch (delimiter) {
            case '{':
            case ',':
                for (String str : types.keySet())
                    builder.suggest(str);
                return builder.buildFuture();
            case ':':
                int start;
                delimiter = current.charAt(stop);
                for (start = stop; start >= 0 && "{,:".indexOf(delimiter) == -1; start--)
                    delimiter = current.charAt(start);

                String key = current.substring(start + 1, stop + 1);
                String value = current.substring(stop + 2);

                System.out.println("Current: " + current + ",   " + key + ": " + value);
                if (!types.containsKey(key))
                    break;

                for (Tooltip tip : types.get(key).suggestions.apply(data)) {
                    if (tip.suggestion().equalsIgnoreCase(value)) {
                        builder.suggest("}", new LiteralMessage("Close tag"));
                        builder.suggest(",", new LiteralMessage("Add another element"));
                        return builder.buildFuture();
                    }
                }

                int finalStop = stop;
                Arrays.stream(types.get(key).suggestions.apply(data))
                        .map(tip -> Tooltip.of(data.current.substring(0, finalStop + 1) + tip.suggestion(), tip.tip()))
                        .forEach(tip -> builder.suggest(tip.suggestion(), new LiteralMessage(tip.tip())));
        }

        return builder.buildFuture();
    }

    public static class MapValueType<T> {

        private final Class<?> type;
        private final Function<CommandData, Tooltip[]> suggestions;

        private MapValueType(Class<?> type, Function<CommandData, Tooltip[]> suggestions) {
            this.type = type;
            this.suggestions = suggestions;
        }
    }
}
