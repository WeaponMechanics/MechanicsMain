package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.Entity;

import java.util.List;

public class EntityListArgumentType extends CommandArgumentType<List<Entity>> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().entities();
    }

    @Override
    public List<Entity> parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getEntitiesSelector(context, key);
    }
}