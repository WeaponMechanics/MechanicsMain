package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class GreedyArgumentType extends CommandArgumentType<String> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public String parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return StringArgumentType.getString(context, key);
    }
}
