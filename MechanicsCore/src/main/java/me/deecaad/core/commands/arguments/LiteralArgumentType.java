package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

public class LiteralArgumentType implements CommandArgumentType<String> {

    private final String literal;

    public LiteralArgumentType(String literal) {
        this.literal = literal;
    }

    @Override
    public Class<String> getDataType() {
        return String.class;
    }

    @Override
    public ArgumentType<String> getBrigadierType() {
        return null;
    }

    @Override
    public String parse(CommandContext<Object> context) {
        return literal;
    }
}
