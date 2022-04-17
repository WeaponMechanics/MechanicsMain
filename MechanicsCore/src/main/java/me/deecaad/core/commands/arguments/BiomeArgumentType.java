package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.block.Biome;

import java.util.concurrent.CompletableFuture;

public class BiomeArgumentType extends CommandArgumentType<Biome> {

    @Override
    public ArgumentType<?> getBrigadierType() {
        return compatibility().key();
    }

    @Override
    public Biome parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return compatibility().getBiome(context, key);
    }

    @Override
    public CompletableFuture<Suggestions> suggestions(CommandContext<Object> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return compatibility().biomeKey().getSuggestions(context, builder);
    }
}
