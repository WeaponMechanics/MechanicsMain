package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class BooleanArgumentType extends CommandArgumentType<Boolean> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return BoolArgumentType.bool();
    }

    @Override
    public Boolean parse(CommandContext<Object> context, String key) {
        return BoolArgumentType.getBool(context, key);
    }

    @Override
    public boolean includeName() {
        return true;
    }
}
