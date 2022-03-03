package me.deecaad.core.commands;

import me.deecaad.core.commands.arguments.GreedyArgumentType;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
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
    BaseComponent[] cache;


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

        if (argument.isRequired() && !previous.isRequired())
            throw new CommandException("Cannot have a required argument after optional argument. For: " + this + " + " + argument);
        if (argument.getType() instanceof GreedyArgumentType)
            if (this.args.stream().anyMatch(a -> GreedyArgumentType.class.isInstance(argument.getType())))
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
     *      Permission: weaponmechanics.commands.give
     *      Aliases: [] #
     *
     *      <target*>: Who is given the weapon(s).
     *      <weapon*>: Which weapon to choose, use "*" to give all.
     *      <amount>: How many weapons should be given. Default: 1
     *      <data>: Should any extra data be added to the weapon? Default: {}
     * }</pre></blockquote>
     *
     * @return A non-null reference to this (builder pattern).
     */
    public CommandBuilder registerHelp(HelpColor color) {
        CommandBuilder help = new CommandBuilder("help").withDescription("Shows useful information about the commands");
        buildHelp(help, this, color);
        withSubcommand(help);
        return this;
    }

    private static void buildHelp(CommandBuilder help, CommandBuilder parent, HelpColor color) {
        for (CommandBuilder subcommand : parent.subcommands) {
            CommandBuilder subHelp = new CommandBuilder(subcommand.label)
                    .withPermission(subcommand.permission)
                    .withRequirements(subcommand.requirements)
                    .withDescription(help.description + " " + subcommand.label);
            help.withSubcommand(subHelp);

            buildHelp(subHelp, subcommand, color);
        }

        // When a command has no sub-commands, we need to build an argument-based
        // help command (Which will show the player how to use command arguments)
        if (help.subcommands.isEmpty()) {
            help.executes(CommandExecutor.any((sender, args) -> {
                if (parent.cache == null)
                    parent.cache = buildCommandWithoutSubcommands(help, parent, color);

                sender.spigot().sendMessage(parent.cache);
            }));
        }

        // When a command has sub-commands, we need to build a list-based
        // help command (Which will show the player which commands they can use)
        else {
            help.executes(CommandExecutor.any((sender, args) -> {
                if (parent.cache == null)
                    parent.cache = buildCommandWithSubcommands(help, parent, color);

                sender.spigot().sendMessage(parent.cache);
            }));
        }
    }

    private static BaseComponent[] buildCommandWithSubcommands(CommandBuilder help, CommandBuilder parent, HelpColor color) {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append("/" + help.description + ": ").color(color.a);
        builder.append(parent.description).color(color.b).append("\n");

        // If a permission is present, lets add "click to copy" support.
        if (parent.permission != null) {
            builder.append("Permission: ").color(color.a)
                    .retain(ComponentBuilder.FormatRetention.EVENTS)
                    .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, parent.permission.getName()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to copy").create()));
            builder.append(parent.permission.getName()).color(color.b);
            builder.append("\n");
        }

        if (!parent.aliases.isEmpty()) {
            builder.reset().append("Aliases: ").color(color.a);
            builder.append(parent.aliases.toString()).color(color.b);
            builder.append("\n");
        }

        builder.reset().append("\n\n");
        for (CommandBuilder subcommand : parent.subcommands) {
            builder.append("<" +subcommand.label + ">: ")
                    .color(color.a)
                    .retain(ComponentBuilder.FormatRetention.EVENTS)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND))
        }
    }

    private static BaseComponent[] buildCommandWithoutSubcommands(CommandBuilder help, CommandBuilder parent, HelpColor color) {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append("/" + help.description + ": ").color(color.a);
        builder.append(parent.description).color(color.b).append("\n");

        // If a permission is present, lets add "click to copy" support.
        if (parent.permission != null) {
            builder.append("Permission: ").color(color.a)
                    .retain(ComponentBuilder.FormatRetention.EVENTS)
                    .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, parent.permission.getName()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to copy").create()));
            builder.append(parent.permission.getName()).color(color.b);
            builder.append("\n");
        }

        if (!parent.aliases.isEmpty()) {
            builder.reset().append("Aliases: ").color(color.a);
            builder.append(parent.aliases.toString()).color(color.b);
            builder.append("\n");
        }

        // Show how to use the command + allow them to click to auto-fill
        builder.reset().append("Usage: ").color(color.a);
        builder.append("/" + help.description).color(color.b)
                .retain(ComponentBuilder.FormatRetention.EVENTS)
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, help.description))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to suggest").create()));

        // Shows <arg1*> <arg2*> <arg3> <arg4> <...>
        for (Argument<?> arg : parent.args)
            arg.append(builder.color(color.a));

        // When a command has arguments, we should show the descriptions of
        // each argument
        if (!parent.args.isEmpty()) {
            builder.reset().append("\n\n");

            // Shows <arg1*>: <desc>\n <arg2>: <desc>\n...
            for (Argument<?> arg : parent.args) {
                arg.append(builder.color(color.a));
                builder.append(": ").color(color.a);
                builder.append(arg.description).color(color.b);
                builder.append("\n");
            }
        }

        return builder.create();
    }

    public static final class HelpColor {
        public ChatColor a;
        public ChatColor b;
        public String symbol;

        public HelpColor(ChatColor a, ChatColor b, String symbol) {
            this.a = a;
            this.b = b;
            this.symbol = symbol;
        }
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
                .map(Argument::getName)
                .map(s -> "<" + s + ">")
                .collect(Collectors.joining(" "))
                + " "
                + Arrays.stream(optionalDefaultValues)
                .map(Objects::toString)
                .collect(Collectors.joining(" "));
    }
}
