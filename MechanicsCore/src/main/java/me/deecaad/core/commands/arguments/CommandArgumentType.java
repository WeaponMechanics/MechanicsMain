package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import me.deecaad.core.commands.LegacyCommandSyntaxException;
import me.deecaad.core.utils.ReflectionUtil;

import java.util.List;

public interface CommandArgumentType<T> {

    ArgumentType<T> getBrigadierType();

    default boolean isBrigadier() {
        return ReflectionUtil.getMCVersion() >= 13;
    }

    // * ----- LEGACY METHODS ----- * //
    // In versions older then 1.13, these methods are used. Any
    // CommandArgumentType which does not implement these methods will not be
    // compatible with legacy versions.

    default T legacyParse() throws LegacyCommandSyntaxException {
        throw new IllegalStateException(getClass() + " does not support legacy MC versions");
    }

    default List<String> legacySuggestions(String input) {
        throw new IllegalStateException(getClass() + " does not support legacy MC versions");
    }
}
