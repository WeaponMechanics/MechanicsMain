package me.deecaad.core.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

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
 */
public class HelpCommandBuilder {

    /**
     * Don't let anyone instantiate this class
     */
    private HelpCommandBuilder() {
    }

    public static void register(CommandBuilder command, HelpColor color) {
        CommandBuilder help = new CommandBuilder("help")
                .withAliases("?")
                .withDescription("Displays useful help information");
        buildHelp(help, command, color);
        command.withSubcommand(help);
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
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + help.description + " " + subcommand.label));

            if (subcommand.cache == null) {
                if (subcommand.subcommands.isEmpty())
                    subcommand.cache = buildCommandWithoutSubcommands(help, parent, color);
                else
                    subcommand.cache = buildCommandWithSubcommands(help, parent, color);
            }

            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, subcommand.cache));
        }

        return builder.create();
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
}
