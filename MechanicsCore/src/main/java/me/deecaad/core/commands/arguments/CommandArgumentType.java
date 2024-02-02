package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.deecaad.core.commands.Argument;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.command.CommandCompatibility;

import java.util.concurrent.CompletableFuture;

public abstract class CommandArgumentType<T> {

    public final CommandCompatibility compatibility() {
        return CompatibilityAPI.getCommandCompatibility();
    }

    // * ----- BRIGADIER METHODS ----- * //
    // In versions 1.13+, we use Mojang's "version stable" command api called
    // brigadier. This is the preferred method of parsing a command, since
    // brigadier will handle errors.

    public abstract ArgumentType<?> getBrigadierType();

    public abstract T parse(CommandContext<Object> context, String key) throws CommandSyntaxException;

    public CompletableFuture<Suggestions> suggestions(CommandContext<Object> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        SuggestionProvider<Object> suggestions = (c, b) -> getBrigadierType().listSuggestions(c, b);
        return suggestions.getSuggestions(context, builder);
    }

    /**
     * Usually, an argument's datatype is pretty intuitive. For example, an {@link EntityArgumentType}
     * is usually used as a target selector. An {@link IntegerArgumentType} is never as intuitive, so it
     * should override this method to return <code>true</code>.
     *
     * @return true to include the {@link Argument#getName()}
     */
    public boolean includeName() {
        return false;
    }

    // * ----- OTHER METHODS ----- * //

    @Override
    public String toString() {

        // Each class name ends with "ArgumentType". Let's strip that away to
        // reveal the human-readable class name
        String name = getClass().getSimpleName();
        return name.substring(0, name.length() - "ArgumentType".length());
    }
}
