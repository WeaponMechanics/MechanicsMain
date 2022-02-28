package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class TimeArgumentType extends CommandArgumentType<Integer> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().time();
    }

    @Override
    public Integer parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getTime(context, key);
    }
}
