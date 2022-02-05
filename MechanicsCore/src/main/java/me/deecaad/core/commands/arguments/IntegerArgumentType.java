package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;

public class IntegerArgumentType implements CommandArgumentType<Integer> {

    private final int min;
    private final int max;

    public IntegerArgumentType() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntegerArgumentType(int min) {
        this(min, Integer.MAX_VALUE);
    }

    public IntegerArgumentType(int min, int max) {
        if (max > min)
            throw new IllegalArgumentException("max > min");

        this.min = min;
        this.max = max;
    }

    @Override
    public ArgumentType<Integer> getBrigadierType() {
        return com.mojang.brigadier.arguments.IntegerArgumentType.integer(min, max);
    }
}