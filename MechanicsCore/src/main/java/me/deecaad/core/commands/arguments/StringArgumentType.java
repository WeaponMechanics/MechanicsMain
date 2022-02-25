package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

public class StringArgumentType extends CommandArgumentType<String> {

    private boolean allowSpecialCharacters;

    public StringArgumentType() {
    }

    /**
     * Use this constructor whenever you want to allow special characters.
     * When <code>allowSpecialCharacters = false</code>, then characters
     * like '*', '(' and '=' will not be allowed.
     *
     * <p>Note that some characters, like '_', '.', '-', and '+' are allowed
     * even when <code>allowSpecialCharacters = false</code>.
     *
     * @param allowSpecialCharacters true to allow special characters.
     */
    public StringArgumentType(boolean allowSpecialCharacters) {
        this.allowSpecialCharacters = allowSpecialCharacters;
    }

    @Override
    public ArgumentType<String> getBrigadierType() {
        return allowSpecialCharacters
                ? com.mojang.brigadier.arguments.StringArgumentType.word()
                : com.mojang.brigadier.arguments.StringArgumentType.string();
    }

    @Override
    public String parse(CommandContext<Object> context, String key) {
        return context.getArgument(key, String.class);
    }
}
