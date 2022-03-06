package me.deecaad.core.commands;

import me.deecaad.core.commands.arguments.GreedyArgumentType;
import me.deecaad.core.commands.arguments.LiteralArgumentType;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommandBuilder implements Cloneable {

    // Package private for internal usage
    String label;
    Permission permission;
    Predicate<CommandSender> requirements;
    List<String> aliases;
    List<Argument<Object>> args;
    List<CommandBuilder> subcommands;
    CommandExecutor<? extends CommandSender> executor;
    String description;

    // For internal usage, stores the default values of optional arguments.
    Object[] optionalDefaultValues;

    // Use for saving performance on the help command
    TextComponent cache;


    public CommandBuilder(String label) {
        if (label == null || label.trim().isEmpty())
            throw new IllegalArgumentException("null or empty string: " + label);

        this.label = label;
        this.permission = null;
        this.aliases = new ArrayList<>();
        this.args = new ArrayList<>();
        this.subcommands = new ArrayList<>();
        this.executor = null;
        this.optionalDefaultValues = new Object[0];
    }

    public CommandBuilder withPermission(String permission) {
        Permission temp = new Permission(permission);
        return withPermission(temp);
    }

    public CommandBuilder withPermission(String permission, PermissionDefault def) {
        Permission temp = new Permission(permission, def);
        return withPermission(temp);
    }

    public CommandBuilder withPermission(Permission permission) {
        this.permission = permission;
        if (permission != null && Bukkit.getPluginManager().getPermission(permission.getName()) == null)
            Bukkit.getPluginManager().addPermission(permission);
        return this;
    }

    public CommandBuilder withRequirements(Predicate<CommandSender> requirements) {
        this.requirements = requirements;
        return this;
    }

    public CommandBuilder withAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    @SuppressWarnings("all")
    public CommandBuilder withArgument(Argument<?> argument) {
        Argument<?> previous = this.args.size() == 0 ? null : this.args.get(this.args.size() - 1);

        if (argument.isRequired() && previous != null && !previous.isRequired())
            throw new CommandException("Cannot have a required argument after optional argument. For: " + this + " + " + argument);
        if (argument.getType() instanceof GreedyArgumentType)
            if (this.args.stream().map(Argument::getType).anyMatch(GreedyArgumentType.class::isInstance))
                throw new CommandException("Cannot have multiple greedy arguments. For: " + this + " + " + argument);

        this.args.add((Argument<Object>) argument);
        return this;
    }

    @SuppressWarnings("all")
    public CommandBuilder withArguments(Argument<?>... arguments) {
        for (Argument<?> argument : arguments)
            this.withArgument(argument);
        return this;
    }

    @SuppressWarnings("all")
    public CommandBuilder withArguments(List<Argument<?>> arguments) {
        for (Argument<?> argument : arguments)
            this.withArgument(argument);
        return this;
    }

    public CommandBuilder withSubcommand(CommandBuilder builder) {
        this.subcommands.add(builder);
        return this;
    }

    public CommandBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public <T extends CommandSender> CommandBuilder executes(CommandExecutor<T> executor) {
        this.executor = executor;
        return this;
    }

    public CommandBuilder register() {

        // Handle this command's validations, as well as sub-command
        // validations. This is to help developers debug issues.
        this.validate();

        if (ReflectionUtil.getMCVersion() >= 13) {
            BrigadierCommand.register(this);
        }
        else {
            throw new IllegalStateException("oops forgot to add legacy");
        }

        return this;
    }


    public CommandBuilder registerHelp(HelpCommandBuilder.HelpColor color) {
        HelpCommandBuilder.register(this, color);
        return this;
    }

    /**
     * A command not can have both <b>arguments</b> and <b>sub-commands</b>.
     * If a command is without sub-commands, it must have an executor.
     * These rules apply to sub-commands.
     */
    private void validate() {
        if (!args.isEmpty() && !subcommands.isEmpty())
            throw new CommandException("Cannot use arguments and sub-commands at the same time!");
        if (subcommands.isEmpty() && executor == null)
            throw new CommandException("You forgot to add an executor or sub-commands to your command!");

        subcommands.forEach(CommandBuilder::validate);
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
    public CommandBuilder clone() {
        try {
            CommandBuilder builder = (CommandBuilder) super.clone();
            builder.subcommands = new ArrayList<>(builder.subcommands);
            builder.args = new ArrayList<>(builder.args);
            builder.cache = null; // This can be skipped, by why store it anyway?
            return builder;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        return "/" + label + " " + args.stream()
                .map(arg -> LiteralArgumentType.class.isInstance(arg.getType()) ? arg.getName() : "<" + arg.getName() + ">")
                .collect(Collectors.joining(" "))
                + " "
                + Arrays.stream(optionalDefaultValues)
                .map(Objects::toString)
                .collect(Collectors.joining(" "));
    }
}
