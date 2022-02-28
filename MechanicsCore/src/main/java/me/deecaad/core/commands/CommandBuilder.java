package me.deecaad.core.commands;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

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

    public CommandBuilder withSubCommand(CommandBuilder builder) {
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

    public void register() {

        // First things first: Validation
        // A command may not have both *arguments* AND *sub-commands*
        // A command with sub-commands MAY NOT have an executor (subcommands handle that!)
        // A command without sub-commands MUST have an executor
        if (!args.isEmpty() && !subcommands.isEmpty())
            throw new CommandException("Cannot use arguments and sub-commands at the same time!");
        if (!subcommands.isEmpty() && executor != null)
            throw new CommandException("Cannot use an executor with subcommands! Register the executor with the subcommands, not the root command!");
        if (subcommands.isEmpty() && executor == null)
            throw new CommandException("You forgot to add an executor or sub-commands to your command!");

        if (ReflectionUtil.getMCVersion() >= 13) {
            BrigadierCommand.register(this);
        }
        else {
            throw new IllegalStateException("oops forgot to add legacy");
        }
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
            return builder;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
