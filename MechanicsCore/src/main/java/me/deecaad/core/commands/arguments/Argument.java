package me.deecaad.core.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.deecaad.core.commands.CommandData;
import me.deecaad.core.commands.LegacyCommandSyntaxException;
import me.deecaad.core.commands.Tooltip;

import java.util.function.Function;

public class Argument<T> {

    private final String name;
    private final CommandArgumentType<T> type;
    private final T defaultValue; // null when isRequired
    private final boolean isRequired;

    private Function<CommandData, Tooltip[]> suggestions;
    private boolean isReplaceSuggestions;

    /**
     * Construct an argument that the {@link org.bukkit.command.CommandSender}
     * must explicitly define.
     *
     * @param type The non-null expected type.
     */
    public Argument(String name, CommandArgumentType<T> type) {
        this.type = type;
        this.defaultValue = null;
        this.isRequired = true;

        if (!name.startsWith("<"))
            this.name = "<" + name + ">";
        else
            this.name = name;
    }

    /**
     * Construct an optional argument.
     *
     * @param type The non-null expected type.
     * @param defaultValue The value to use when the player doesn't define one.
     */
    public Argument(String name, CommandArgumentType<T> type, T defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.isRequired = false;

        if (!name.startsWith("<"))
            this.name = "<" + name + ">";
        else
            this.name = name;
    }

    /**
     * Returns the human-readable name of the argument. The name should be 1
     * word, and be descriptive to how this argument will be used in the
     * command. Examples: target, x, y, z, item.
     *
     * @return The non-null argument name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the data-type of the argument. Used internally to parse
     * arguments, and to handle suggestions.
     *
     * @return The non-null data-type.
     */
    public CommandArgumentType<T> getType() {
        return type;
    }

    /**
     * Returns the default value when {@link #isRequired()} is false, or
     * <code>null</code> when {@link #isRequired()} is true.
     *
     * @return The value to use when the user doesn't define one.
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns <code>true</code> if this argument is a required (if the user
     * doesn't define a value for this argument, the command will fail!).
     *
     * @return true if this argument is required.
     */
    public boolean isRequired() {
        return isRequired;
    }

    /**
     * Returns the extra suggestion provider, or else null.
     *
     * @return The nullable extra suggestion provider.
     */
    public Function<CommandData, Tooltip[]> getSuggestions() {
        return suggestions;
    }

    /**
     * Returns <code>true</code> if the suggestions should replace the default
     * suggestions.
     *
     * @return true to override the default suggestions.
     */
    public boolean isReplaceSuggestions() {
        return isReplaceSuggestions;
    }

    /**
     * Adds the given suggestions during tab completions to the pre-defined
     * list of suggestions for this argument's {@link CommandArgumentType}.
     *
     * @param suggestions The suggestions to add to the list.
     * @return A non-null reference pointing to this argument (builder pattern).
     */
    public Argument<T> append(Function<CommandData, Tooltip[]> suggestions) {
        this.suggestions = suggestions;
        return this;
    }

    /**
     * Replaced the pre-defined list of suggestions with the given suggestions.
     *
     * @param suggestions The suggestions to add to the list.
     * @return A non-null reference pointing to this argument (builder pattern).
     */
    public Argument<T> replace(Function<CommandData, Tooltip[]> suggestions) {
        this.suggestions = suggestions;
        this.isReplaceSuggestions = true;
        return this;
    }

    public T parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return type.parse(context, key);
    }

    public T parse(String str) throws LegacyCommandSyntaxException {
        return type.legacyParse(str);
    }
}
