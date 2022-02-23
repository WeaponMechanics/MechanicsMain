package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.deecaad.core.commands.CommandData;
import me.deecaad.core.commands.CommandException;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.command.CommandCompatibility;

import java.util.List;

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


    // * ----- LEGACY METHODS ----- * //
    // In versions older then 1.13, these methods are used. Any
    // CommandArgumentType which does not implement these methods will not be
    // compatible with legacy versions.

    public T legacyParse(String arg) throws CommandException {
        throw new IllegalStateException(getClass() + " does not support legacy MC versions");
    }

    public List<String> legacySuggestions(CommandData data) {
        throw new IllegalStateException(getClass() + " does not support legacy MC versions");
    }

    @Override
    public String toString() {

        // Each class name ends with "ArgumentType". Let's strip that away to
        // reveal the human-readable class name
        String name = getClass().getSimpleName();
        return name.substring(0, name.length() - "ArgumentType".length());
    }
}
