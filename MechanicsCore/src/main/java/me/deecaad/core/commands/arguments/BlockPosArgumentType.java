package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.deecaad.core.compatibility.CompatibilityAPI;
import org.bukkit.block.Block;

public class BlockPosArgumentType extends CommandArgumentType<Block> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return CompatibilityAPI.getCommandCompatibility().location();
    }

    @Override
    public Block parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return CompatibilityAPI.getCommandCompatibility().getLocationBlock(context, key);
    }
}
