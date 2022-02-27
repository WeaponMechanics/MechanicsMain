package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.deecaad.core.commands.wrappers.Location2d;
import me.deecaad.core.compatibility.CompatibilityAPI;

public class Location2dArgumentType extends CommandArgumentType<Location2d> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return CompatibilityAPI.getCommandCompatibility().location();
    }

    @Override
    public Location2d parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return CompatibilityAPI.getCommandCompatibility().getLocation2DPrecise(context, key);
    }
}
