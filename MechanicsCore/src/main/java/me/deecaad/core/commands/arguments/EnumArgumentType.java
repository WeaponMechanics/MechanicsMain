package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.SerializerUtil;
import org.bukkit.entity.EntityType;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EnumArgumentType<T extends Enum<T>> extends CommandArgumentType<T> {

    private final Class<T> clazz;

    public EnumArgumentType(Class<T> clazz) {
        this.clazz = clazz;

        if (clazz == EntityType.class)
            throw new IllegalArgumentException("Use EntityTypeArgumentType");
    }

    public Class<T> getClazz() {
        return clazz;
    }

    @Override
    public ArgumentType<?> getBrigadierType() {
        return StringArgumentType.word(); // enums may only contain letters and underscores
    }

    @Override
    public T parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        String str = context.getArgument(key, String.class);
        Optional<T> optional = EnumUtil.getIfPresent(clazz, str);

        if (optional.isPresent())
            return optional.get();
        else
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Did you mean: " + SerializerUtil.didYouMeanEnum(str, clazz));
    }

    @Override
    public CompletableFuture<Suggestions> suggestions(CommandContext<Object> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String str : EnumUtil.getOptions(clazz)) {
            if (str.toLowerCase(Locale.ROOT).startsWith(remaining))
                builder.suggest(str);
        }
        return builder.buildFuture();
    }
}
