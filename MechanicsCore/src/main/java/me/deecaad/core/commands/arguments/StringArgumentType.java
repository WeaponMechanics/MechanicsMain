package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

import java.util.ArrayList;
import java.util.List;

public class StringArgumentType extends CommandArgumentType<String> {

    private boolean quotes;
    private List<LiteralArgumentType> literals;

    public StringArgumentType() {
    }

    public StringArgumentType(boolean quotes) {
        this.quotes = quotes;
    }

    public List<LiteralArgumentType> getLiterals() {
        return literals;
    }

    public StringArgumentType withLiteral(String literal) {
        if (literals == null)
            literals = new ArrayList<>();

        literals.add(new LiteralArgumentType(literal));
        return this;
    }

    public StringArgumentType withLiterals(String... literals) {
        for (String literal : literals) {
            withLiteral(literal);
        }
        return this;
    }

    public void clear() {
        literals.clear();
    }

    @Override
    public ArgumentType<String> getBrigadierType() {
        if (quotes)
            return com.mojang.brigadier.arguments.StringArgumentType.string();
        else
            return com.mojang.brigadier.arguments.StringArgumentType.word();
    }

    @Override
    public String parse(CommandContext<Object> context, String key) {
        return context.getArgument(key, String.class);
    }
}
