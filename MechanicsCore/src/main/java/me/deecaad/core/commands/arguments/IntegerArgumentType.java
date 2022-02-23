package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.deecaad.core.commands.CommandData;
import me.deecaad.core.commands.CommandException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IntegerArgumentType extends CommandArgumentType<Integer> {

    private final int min;
    private final int max;

    public IntegerArgumentType() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntegerArgumentType(int min) {
        this(min, Integer.MAX_VALUE);
    }

    public IntegerArgumentType(int min, int max) {
        if (max < min)
            throw new IllegalArgumentException("max > min");

        this.min = min;
        this.max = max;
    }

    @Override
    public ArgumentType<Integer> getBrigadierType() {
        return com.mojang.brigadier.arguments.IntegerArgumentType.integer(min, max);
    }

    @Override
    public Integer parse(CommandContext<Object> context, String key) {
        return context.getArgument(key, Integer.class);
    }

    @Override
    public Integer legacyParse(String arg) throws CommandException {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException ex) {
            throw new CommandException("Expected integer, got: " + arg, ex);
        }
    }

    @Override
    public List<String> legacySuggestions(CommandData input) {
        return IntStream.range(min, max + 1)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
    }
}