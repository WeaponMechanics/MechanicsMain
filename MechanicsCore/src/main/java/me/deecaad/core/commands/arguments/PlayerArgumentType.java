package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.Player;

public class PlayerArgumentType extends CommandArgumentType<Player> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().player();
    }

    @Override
    public Player parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getPlayerSelector(context, key);
    }
}
