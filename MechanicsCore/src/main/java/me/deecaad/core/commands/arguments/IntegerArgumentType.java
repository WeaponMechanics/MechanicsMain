package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.deecaad.core.commands.CommandData;
import me.deecaad.core.commands.Tooltip;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;
import java.util.stream.IntStream;

public class IntegerArgumentType extends CommandArgumentType<Integer> {

    public static Function<CommandData, Tooltip[]> ITEM_COUNT = data -> {
        int stackSize = 64;
        for (Object obj : data.previousArguments)
            if (obj instanceof ItemStack)
                stackSize = ((ItemStack) obj).getMaxStackSize();

        return IntStream.rangeClosed(1, stackSize).sorted().mapToObj(Tooltip::of).toArray(Tooltip[]::new);
    };

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
    public boolean includeName() {
        return true;
    }
}