package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.deecaad.core.commands.LegacyCommandSyntaxException;
import me.deecaad.core.utils.ReflectionUtil;

import java.util.List;

public interface CommandArgumentType<T> {

    Class<T> getDataType();

    // * ----- BRIGADIER METHODS ----- * //
    // In versions 1.13+, we use Mojang's "version stable" command api called
    // brigadier. This is the preferred method of parsing a command, since
    // brigadier will handle errors.

    ArgumentType<T> getBrigadierType();

    T parse(CommandContext<Object> context);

    default boolean isBrigadier() {
        return ReflectionUtil.getMCVersion() >= 13;
    }

    // * ----- LEGACY METHODS ----- * //
    // In versions older then 1.13, these methods are used. Any
    // CommandArgumentType which does not implement these methods will not be
    // compatible with legacy versions.

    default T legacyParse(String arg) throws LegacyCommandSyntaxException {
        throw new IllegalStateException(getClass() + " does not support legacy MC versions");
    }

    default List<String> legacySuggestions(String input) {
        throw new IllegalStateException(getClass() + " does not support legacy MC versions");
    }
}
