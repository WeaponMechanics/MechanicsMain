package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.Entity;

public class EntityArgumentType extends CommandArgumentType<Entity> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().entity();
    }

    @Override
    public Entity parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getEntitySelector(context, key);
    }
}
