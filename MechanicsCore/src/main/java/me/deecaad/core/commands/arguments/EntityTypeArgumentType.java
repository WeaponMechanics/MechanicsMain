package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class EntityTypeArgumentType extends CommandArgumentType<EntityType> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().entitySummon();
    }

    @Override
    public EntityType parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getEntityType(context, key);
    }

    @Override
    public CompletableFuture<Suggestions> suggestions(CommandContext<Object> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return compatibility().entityKey().getSuggestions(context, builder);
    }
}
