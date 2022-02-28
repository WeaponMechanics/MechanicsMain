package me.deecaad.core.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class SuggestionsBuilder {

    private final List<Tooltip> options;
    private Function<CommandData, Tooltip[]> function;

    public SuggestionsBuilder() {
        this.options = new ArrayList<>();
    }

    /**
     * Adds the given option to the suggestions list. The given type may be a
     * {@link List}, an {@link java.lang.reflect.Array}, a {@link Tooltip}, or
     * any object that overrides the {@link #toString()} method to produce a
     * simple, human-readable result.
     *
     * <p>For example, 'DOG', 'HOUSE', 'ALL', and 'GLASS' are all readable, but
     * 'MyObject@3423', 'MyObject{a=2, b=3, c=4}' are not readable. In general,
     * suggestions should not include special characters or spaces (See
     * {@link com.mojang.brigadier.StringReader#isAllowedInUnquotedString(char)}).
     *
     * @param option The non-null option to add to the suggestions list.
     * @return A non-null reference to this (builder-pattern).
     */
    @SuppressWarnings("unchecked")
    public SuggestionsBuilder with(Object option) {
        if (option == null)
            throw new IllegalArgumentException("option cannot be null");

        // I highly doubt people will add their own Tooltips or lists like
        // this, since their code would be unreadable. However, we will
        // support nested arrays/lists.
        if (option instanceof Tooltip)
            options.add((Tooltip) option);
        else if (option instanceof Collection)
            with((Collection<Object>) option);
        else if (option.getClass().isArray())
            with((Object[]) option);
        else
            options.add(Tooltip.of(option));

        return this;
    }

    /**
     * Adds all the given options to the suggestions list.
     *
     * @param options The non-null array of options to add.
     * @return A non-null reference to this (builder pattern).
     */
    public SuggestionsBuilder with(Object... options) {
        for (Object obj : options)
            with(obj);

        return this;
    }

    /**
     * Adds all the given options to the suggestions list.
     *
     * @param options The non-null list of options to add.
     * @return A non-null reference to this (builder pattern).
     */
    public SuggestionsBuilder with(Collection<Object> options) {
        for (Object obj : options)
            with(obj);

        return this;
    }

    /**
     * Use this method whenever you want to use the 'boiler-plate' methods
     * ({@link #with(Object)}) along with a tab-completion that uses
     * {@link CommandData}.
     *
     * @param function The function to generate tab completions, or null.
     * @return A non-null reference to this (builder pattern).
     */
    public SuggestionsBuilder with(Function<CommandData, Tooltip[]> function) {
        this.function = function;
        return this;
    }

    /**
     * Builds the suggestion provider function containing each option.
     *
     * @return The suggestion provider function.
     */
    public Function<CommandData, Tooltip[]> build() {
        return data -> {
            List<Tooltip> suggestions = new ArrayList<>(options.size() + (function == null ? 0 : 10));
            suggestions.addAll(options);

            if (function != null)
                Collections.addAll(suggestions, function.apply(data));

            return suggestions.toArray(new Tooltip[0]);
        };
    }

    // Shorthand function for common simple usages
    public static Function<CommandData, Tooltip[]> from(Object... options) {
        return new SuggestionsBuilder().with(options).build();
    }
}
