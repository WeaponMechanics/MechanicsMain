package me.deecaad.core.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.deecaad.core.commands.arguments.CommandArgumentType;
import me.deecaad.core.compatibility.CompatibilityAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.util.function.Function;
import java.util.function.Predicate;

public class Argument<T> {

    private final String name;
    private final CommandArgumentType<T> type;
    private final T defaultValue; // null when isRequired
    private final boolean isRequired;

    // Package private for internal use
    Function<CommandData, Tooltip[]> suggestions;
    Permission permission;
    Predicate<CommandSender> requirements;
    boolean isReplaceSuggestions;
    boolean listed;
    String description;

    /**
     * Construct an argument that the {@link org.bukkit.command.CommandSender}
     * must explicitly define.
     *
     * @param type The non-null expected type.
     */
    public Argument(String name, CommandArgumentType<T> type) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("bad name");

        this.name = name;
        this.type = type;
        this.defaultValue = null;
        this.isRequired = true;
        this.listed = true;
    }

    /**
     * Construct an optional argument.
     *
     * @param type The non-null expected type.
     * @param defaultValue The value to use when the player doesn't define one.
     */
    public Argument(String name, CommandArgumentType<T> type, T defaultValue) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("bad name");

        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.isRequired = false;
        this.listed = true;
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
     * Adds the given suggestions during tab completions to the pre-defined
     * list of suggestions for this argument's {@link CommandArgumentType}.
     *
     * @param suggestions The suggestions to add to the list.
     * @return A non-null reference pointing to this argument (builder pattern).
     */
    public Argument<T> append(Function<CommandData, Tooltip[]> suggestions) {
        this.suggestions = suggestions;
        this.isReplaceSuggestions = false;
        return this;
    }

    /**
     * Replaced the pre-defined list of suggestions with the given suggestions.
     *
     * @param suggestions The suggestions to add to the list.
     * @return A non-null reference to this (builder pattern).
     */
    public Argument<T> replace(Function<CommandData, Tooltip[]> suggestions) {
        this.suggestions = suggestions;
        this.isReplaceSuggestions = true;
        return this;
    }

    /**
     * Used for the help command. Shows this short description for this
     * argument. The description should be relatively short (<10 words).
     *
     * @param description The description to use, or null.
     * @return A non-null reference to this (builder pattern).
     */
    public Argument<T> withDesc(String description) {
        this.description = description;
        return this;
    }

    /**
     * When a {@link CommandSender} does not have the given permission, they
     * will not be able to see or use this command. If the permission was not
     * previously registered, this method will register it.
     *
     * @param permission The permission to require, or null.
     * @return A non-null reference to this (builder pattern).
     */
    public Argument<T> withPermission(Permission permission) {
        this.permission = permission;
        if (permission != null && Bukkit.getPluginManager().getPermission(permission.getName()) == null)
            Bukkit.getPluginManager().addPermission(permission);
        return this;
    }

    /**
     * When a {@link CommandSender} does not test <code>true</code> to the
     * given predicate, they will not be able to see or use this command. While
     * this can be used for {@link Permission}, you should use
     * {@link #withPermission(Permission)} instead.
     *
     * @param requirements The predicate to use, or null.
     * @return A non-null reference to this (builder pattern).
     */
    public Argument<T> withRequirements(Predicate<CommandSender> requirements) {
        this.requirements = requirements;
        return this;
    }

    public boolean isListed() {
        return listed;
    }

    public Argument<T> setListed(boolean listed) {
        this.listed = listed;
        return this;
    }

    public T parse(CommandContext<Object> context, String key) throws CommandSyntaxException {
        return type.parse(context, key);
    }

    public T parse(String str) throws CommandException {
        return type.legacyParse(str);
    }

    public Predicate<Object> requirements() {
        return nms -> {
            CommandSender sender = CompatibilityAPI.getCommandCompatibility().getCommandSenderRaw(nms);
            if (permission != null && !sender.hasPermission(permission))
                return false;
            else if (requirements != null && !requirements.test(sender))
                return false;
            else
                return true;
        };
    }

    @Override
    public String toString() {
        return name;
    }

    public ComponentBuilder append(ComponentBuilder builder) {
        ChatColor color = builder.getCurrentComponent().getColor();

        if (isRequired()) {
            builder.append(" <" + name).color(color);
            builder.append("*").color(net.md_5.bungee.api.ChatColor.RED);
            builder.append(">").color(color);
        } else {
            builder.append(" <" + name + ">").color(color);
        }

        return builder;
    }
}
