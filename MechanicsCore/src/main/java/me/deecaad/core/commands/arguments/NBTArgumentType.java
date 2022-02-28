package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class NBTArgumentType extends CommandArgumentType<Object> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().nbtCompound();
    }

    @Override
    public Object parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return null;
    }
}
