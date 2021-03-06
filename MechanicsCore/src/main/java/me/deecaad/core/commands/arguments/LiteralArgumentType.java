package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

public class LiteralArgumentType extends CommandArgumentType<String> {

    private final String literal;

    public LiteralArgumentType(String literal) {
        this.literal = literal;

        if (literal == null || literal.isEmpty())
            throw new IllegalArgumentException("Bad literal");
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public ArgumentType<?> getBrigadierType() {
        return null;
    }

    @Override
    public String parse(CommandContext<Object> context, String key) {
        return literal;
    }
}
