package me.deecaad.core.commands.arguments;

public class Argument<T> {

    private final CommandArgumentType<T> type;
    private final T defaultValue; // null when isRequired
    private final boolean isRequired;

    /**
     * Construct an argument that the {@link org.bukkit.command.CommandSender}
     * must explicitly define.
     *
     * @param type The non-null expected type.
     */
    public Argument(CommandArgumentType<T> type) {
        this.type = type;
        this.defaultValue = null;
        this.isRequired = true;
    }

    /**
     * Construct an optional argument.
     *
     * @param type The non-null expected type.
     * @param defaultValue The value to use when the player doesn't define one.
     */
    public Argument(CommandArgumentType<T> type, T defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.isRequired = false;
    }

    public CommandArgumentType<T> getType() {
        return type;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return isRequired;
    }
}
