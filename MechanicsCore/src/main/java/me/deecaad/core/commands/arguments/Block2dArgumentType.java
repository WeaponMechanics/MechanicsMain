package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.deecaad.core.commands.wrappers.Column;
import me.deecaad.core.compatibility.CompatibilityAPI;

public class Block2dArgumentType extends CommandArgumentType<Column> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return CompatibilityAPI.getCommandCompatibility().block2();
    }

    @Override
    public Column parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return CompatibilityAPI.getCommandCompatibility().getLocation2DBlock(context, key);
    }
}