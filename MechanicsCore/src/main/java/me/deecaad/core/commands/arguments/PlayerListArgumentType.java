package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerListArgumentType extends CommandArgumentType<List<Player>> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().player();
    }

    @Override
    public List<Player> parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getPlayersSelector(context, key);
    }
}