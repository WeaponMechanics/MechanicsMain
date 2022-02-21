package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.deecaad.core.commands.CommandBuilder;

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

    public MultiLiteralArgumentType(CommandBuilder builder) {
        this(builder.getLabel(), builder.getAliases().toArray(new String[0]));
    }

    public MultiLiteralArgumentType(String label, String[] aliases) {
        this.literals = new String[aliases.length + 1];
        this.literals[0] = label;
        System.arraycopy(aliases, 0, this.literals, 1, aliases.length);
    }

    public String[] getLiterals() {
        return literals;
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
    public String parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        throw new IllegalStateException("You should not try to parse this argument...?");
    }
}
