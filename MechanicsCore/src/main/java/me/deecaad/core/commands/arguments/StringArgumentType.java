package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

public class StringArgumentType extends CommandArgumentType<String> {

    @Override
    public ArgumentType<String> getBrigadierType() {
        return com.mojang.brigadier.arguments.StringArgumentType.word();
    }

    @Override
    public String parse(CommandContext<Object> context, String key) {
        return context.getArgument(key, String.class);
    }
}
