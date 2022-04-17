package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class ListArgumentType<T> extends CommandArgumentType<List<T>> {

    private final List<T> examples;
    private final boolean strict;

    public ListArgumentType(List<T> examples, boolean strict) {
        this.examples = examples;
        this.strict = strict;
    }

    @Override
    public ArgumentType<?> getBrigadierType() {
        return StringArgumentType.string();
    }

    @Override
    public List<T> parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        String value = context.getInput();
        List<T> temp = new ArrayList<>();

        for (String str : value.split(",")) {
            try {
                temp.add(parse(str));
            } catch (CommandSyntaxException e) {
                throw e; // rethrow
            } catch (Exception e) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create(str);
            }
        }

        return temp;
    }

    @Override
    public CompletableFuture<Suggestions> suggestions(CommandContext<Object> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        String value = context.getInput();
        int newElementLocation = value.lastIndexOf(',') + 1;

        String previous = value.substring(0, newElementLocation);
        String current = value.substring(newElementLocation);

        // We need to determine if an argument is fully typed, so we can
        // suggest putting a comma for the next argument.
        boolean complete;

        if (strict) {
            complete = examples.stream().map(Objects::toString).anyMatch(current::equalsIgnoreCase);
        } else {
            try {
                parse(current); // throws an exception if invalid
                complete = true;
            } catch (Throwable ignore) {
                complete = false;
            }
        }

        if (complete)
            builder.suggest(previous + ",");
        else
            for (T example : examples)
                builder.suggest(previous + example.toString());

        return builder.buildFuture();
    }

    public abstract T parse(String str) throws CommandSyntaxException;


    public static ListArgumentType<Integer> integers(Integer... examples) {
        return new ListArgumentType<Integer>(Arrays.asList(examples), false) {
            @Override
            public Integer parse(String str) throws CommandSyntaxException {
                try {
                    return Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().create(str);
                }
            }
        };
    }

    public static ListArgumentType<Double> doubles(Double... examples) {
        return new ListArgumentType<Double>(Arrays.asList(examples), false) {
            @Override
            public Double parse(String str) throws CommandSyntaxException {
                try {
                    return Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble().create(str);
                }
            }
        };
    }

    public static <T extends Enum<T>> ListArgumentType<T> enums(Class<T> clazz) {
        return new ListArgumentType<T>(EnumUtil.getValues(clazz), true) {
            @Override
            public T parse(String str) throws CommandSyntaxException {
                try {
                    return EnumUtil.getIfPresent(clazz, str).orElseThrow(() -> new IllegalArgumentException(str));
                } catch (IllegalArgumentException ex) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
                            .create("Unknown: " + str + ", Did you mean: " + StringUtil.debugDidYouMean(str, clazz));
                }
            }
        };
    }


}
