package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.deecaad.core.commands.CommandData;
import me.deecaad.core.commands.SuggestionsBuilder;
import me.deecaad.core.commands.Tooltip;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public Function<CommandData, Tooltip[]> suggestions() {
        return (data) -> {
            if (data.current.isEmpty())
                return new Tooltip[]{ Tooltip.of("{", "Open tag") };

            char delimiter = 0;
            int stop;
            for (stop = data.current.length() - 1; stop >= 0 && "{,:".indexOf(delimiter) == -1; stop--)
                delimiter = data.current.charAt(stop);

            switch (delimiter) {
                case '{':
                case ',':
                    return SuggestionsBuilder.from(types.keySet()).apply(data);
                case ':':
                    int start;
                    delimiter = data.current.charAt(stop);
                    for (start = stop; start >= 0 && "{,:".indexOf(delimiter) == -1; start--)
                        delimiter = data.current.charAt(start);

                    String key = data.current.substring(start + 1, stop + 1);
                    String value = data.current.substring(stop + 2);

                    System.out.println("Current: " + data.current + ",   " + key + ": " + value);
                    if (!types.containsKey(key))
                        break;

                    for (Tooltip tip : types.get(key).suggestions.apply(data)) {
                        if (tip.suggestion().equalsIgnoreCase(value))
                            return new Tooltip[]{ Tooltip.of("}", "Close tag"), Tooltip.of(",", "Add another argument") };
                    }


                    int finalStop = stop;
                    return Arrays.stream(types.get(key).suggestions.apply(data))
                            .map(tip -> Tooltip.of(data.current.substring(0, finalStop + 1) + tip.suggestion(), tip.tip()))
                            .toArray(Tooltip[]::new);
            }

            return new Tooltip[]{  };
        };
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
