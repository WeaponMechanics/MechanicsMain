package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.function.Supplier;

public class MultiLiteralArgumentType extends CommandArgumentType<String> {

    private final String[] literals;

    public MultiLiteralArgumentType(String[] literals) {
        if (literals == null || literals.length == 0)
            throw new IllegalArgumentException("empty literals");

        for (String literal : literals)
            if (literal == null || literal.isEmpty())
                throw new IllegalArgumentException("bad literal");

        this.literals = literals.clone();
    }

    public MultiLiteralArgumentType(String label, String[] aliases) {
        this(((Supplier<String[]>) () -> {
            String[] args = new String[aliases.length + 1];
            args[0] = label;
            System.arraycopy(aliases, 0, args, 1, aliases.length);
            return args;
        }).get());
    }

    public String[] getLiterals() {
        return literals;
    }

    @Override
    public ArgumentType<?> getBrigadierType() {
        return null;
    }

    @Override
    public String parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        throw new IllegalStateException("You should not try to parse this argument...?");
    }
}
