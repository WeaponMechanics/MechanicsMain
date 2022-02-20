package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.Entity;

public class EntityArgumentType implements CommandArgumentType<Entity> {

    @Override
    public Class<Entity> getDataType() {
        return Entity.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ArgumentType<Entity> getBrigadierType() {
        return (ArgumentType<Entity>) compatibility().entity(EntitySelectorType.ENTITY);
    }

    @Override
    public Entity parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return (Entity) compatibility().getEntitySelector(context, key, EntitySelectorType.ENTITY);
    }
}
