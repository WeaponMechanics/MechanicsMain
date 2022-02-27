package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.EntityType;

public class EntityTypeArgumentType extends CommandArgumentType<EntityType> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().entitySummon();
    }

    @Override
    public EntityType parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getEntityType(context, key);
    }
}
