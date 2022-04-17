package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.loot.LootTable;

import java.util.concurrent.CompletableFuture;

public class LootTableArgumentType extends CommandArgumentType<LootTable> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().key();
    }

    @Override
    public LootTable parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getLootTable(context, key);
    }

    @Override
    public CompletableFuture<Suggestions> suggestions(CommandContext<Object> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return compatibility().lootKey().getSuggestions(context, builder);
    }
}
