package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.deecaad.core.compatibility.CompatibilityAPI;
import org.bukkit.Location;

public class LocationArgumentType extends CommandArgumentType<Location> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return CompatibilityAPI.getCommandCompatibility().location();
    }

    @Override
    public Location parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return CompatibilityAPI.getCommandCompatibility().getLocationPrecise(context, key);
    }
}
