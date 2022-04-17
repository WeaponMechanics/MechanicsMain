package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class DoubleArgumentType extends CommandArgumentType<Double> {

    private final double min;
    private final double max;

    public DoubleArgumentType() {
        this(Double.MIN_VALUE);
    }

    public DoubleArgumentType(double min) {
        this(min, Double.MAX_VALUE);
    }

    public DoubleArgumentType(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public ArgumentType<?> getBrigadierType() {
        return com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(min, max);
    }

    @Override
    public Double parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(context, key);
    }

    @Override
    public boolean includeName() {
        return true;
    }
}
