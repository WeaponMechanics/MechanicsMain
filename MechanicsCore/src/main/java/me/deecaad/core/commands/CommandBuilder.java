package me.deecaad.core.commands;

import me.deecaad.core.commands.arguments.LiteralArgumentType;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
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

    /**
     * Consider the command: /wm test explosion sphere 5.0 3 DEFAULT #logs
     * It is relatively intuitive, but what is 'test'? 'explosion'? What other
     * options do we have? What do they all do?
     *
     * <p>We want to be able to use /wm help, /wm help test,
     * /wm help test explosion, and /wm help test explosion sphere.
     *
     * <p>For any
     * command with sub-commands, 'help' should list the sub-commands with
     * their descriptions. Therefore, /wm help test explosion produces:
     * <blockquote><pre>{@code
     *      /wm test explosion: Spawns in an explosion that regenerates
     *
     *      <sphere>: Causes a spherical explosion
     *      <cube>: Causes a cubical explosion
     *      <parabola>: Causes a parabolic explosion
     *      <default>: Causes a vanilla MC explosion
     * }</pre></blockquote>
     *
     *
     * <p>For any command without sub-commands, we should give the user
     * information on the arguments of the command. Therefore, /wm help give
     * produces:
     * <blockquote><pre>{@code
     *      /wm give: Gives the target(s) with requested weapon(s)
     *      Usage: /wm give <target*> <weapon*> <amount> <data>
     *
     *      <target*>: Who is given the weapon(s).
     *      <weapon*>: Which weapon to choose, use "*" to give all.
     *      <amount>: How many weapons should be given. Default: 1
     *      <data>: Should any extra data be added to the weapon? Default: {}
     * }</pre></blockquote>
     *
     * @return A non-null reference to this (builder pattern).
     */
    public CommandBuilder registerHelp() {
        CommandBuilder help = new CommandBuilder("help");
        for (CommandBuilder subcommand : subcommands) {
            help.withSubCommand(new CommandBuilder(subcommand.label)
                    .withPermission(subcommand.permission)
                    .withRequirements(subcommand.requirements));
        }

        if (!subcommands.isEmpty()) {



            subcommands.forEach(CommandBuilder::registerHelp);
            return this;
        }

        return this;
    }

    private static void a(CommandBuilder parent) {}


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
            return builder;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        return "/" + label + " " + args.stream()
                .map(Argument::getName)
                .map(s -> "<" + s + ">")
                .collect(Collectors.joining(" "))
                + " "
                + Arrays.stream(optionalDefaultValues)
                .map(Objects::toString)
                .collect(Collectors.joining(" "));
    }
}
