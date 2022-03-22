package me.deecaad.core.commands;

import me.deecaad.core.MechanicsCore;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;

import java.util.Locale;

import static net.kyori.adventure.text.Component.*;

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
                .withDescription(command.label);
        buildHelp(help, command, color);
        help.withDescription("Displays useful information about how to use the commands");
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
            help.cache = buildCommandWithoutSubcommands(help, parent, color).build();
            help.executes(CommandExecutor.any((sender, args) -> {
                MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(help.cache);
            }));

            // When a sub-command has at least 1 required argument, then we
            // should show the help information by default. Consider the
            // command '/wm give'. It fails since it is missing the required
            // arguments, so we should instead execute as if the user had
            // run the command '/wm help give'
            if (!parent.args.isEmpty() && parent.args.get(0).isRequired()) {
                parent.executes(CommandExecutor.any((sender, args) -> {
                    MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(help.cache);
                }));
            }
        }

        // When a command has sub-commands, we need to build a list-based
        // help command (Which will show the player which commands they can use)
        else {
            help.cache = buildCommandWithSubcommands(help, parent, color).build();
            help.executes(CommandExecutor.any((sender, args) -> {
                MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(help.cache);
            }));

            // Consider the command '/wm'. It doesn't have an executor, and it
            // should show useful information.
            if (parent.executor == null) {
                parent.executes(CommandExecutor.any((sender, args) -> {
                    MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(help.cache);
                }));
            }
        }
    }

    private static TextComponent.Builder universal(CommandBuilder help, CommandBuilder parent, HelpColor color) {
        TextComponent.Builder builder = text();

        builder.append(text().content("/" + help.description + ": ").style(color.a));
        builder.append(text().content(String.valueOf(parent.description)).style(color.b));
        builder.append(newline());

        // If a permission is present, lets add "click to copy" support.
        if (parent.permission != null) {
            builder.append(text().content("Permission: ").style(color.a).clickEvent(ClickEvent.copyToClipboard(parent.permission.getName())).hoverEvent(text("Click to copy")));
            builder.append(text().content(parent.permission.getName()).style(color.b).clickEvent(ClickEvent.copyToClipboard(parent.permission.getName())).hoverEvent(text("Click to copy")));
            builder.append(newline());
        }

        if (!parent.aliases.isEmpty()) {
            builder.append(text().content("Aliases: ").style(color.a));
            builder.append(text().content(String.join(", ", parent.aliases)).style(color.b));
            builder.append(newline());
        }

        return builder;
    }

    private static TextComponent.Builder buildCommandWithSubcommands(CommandBuilder help, CommandBuilder parent, HelpColor color) {
        TextComponent.Builder builder = universal(help, parent, color);

        builder.append(newline());
        for (CommandBuilder subcommand : parent.subcommands) {
            builder.append(text().content("<" + subcommand.label + ">: ").style(color.a).clickEvent(ClickEvent.suggestCommand("/" + help.description + " " + subcommand.label)).hoverEvent(subcommand.cache));
            builder.append(text().content(String.valueOf(subcommand.description)).style(color.b).clickEvent(ClickEvent.suggestCommand("/" + help.description + " " + subcommand.label)).hoverEvent(subcommand.cache));
            builder.append(newline());
        }

        return builder;
    }

    private static TextComponent.Builder buildCommandWithoutSubcommands(CommandBuilder help, CommandBuilder parent, HelpColor color) {
        TextComponent.Builder builder = universal(help, parent, color);

        // Show how to use the command + allow them to click to auto-fill
        builder.append(text().content("Usage: ").style(color.a));
        builder.append(text().content("/" + help.description).style(color.b).clickEvent(ClickEvent.suggestCommand("/" + help.description)).hoverEvent(text("Click to run")));

        // Shows <arg1*> <arg2*> <arg3> <arg4> <...>
        for (Argument<?> arg : parent.args)
            arg.append(builder, color.b);

        // When a command has arguments, we should show the descriptions of
        // each argument
        if (!parent.args.isEmpty()) {
            builder.append(newline());
            builder.append(newline());

            // Shows <arg1*>: <desc>\n <arg2>: <desc>\n...
            for (Argument<?> arg : parent.args) {
                arg.append(builder, color.a);

                builder.append(text().content(": ").style(color.a));
                builder.append(text().content(String.valueOf(arg.description)).style(color.b));
                builder.append(newline());
            }
        }

        return builder;
    }


    public static final class HelpColor {

        public Style a;
        public Style b;
        public String symbol;

        public HelpColor(Style a, Style b, String symbol) {
            this.a = a;
            this.b = b;
            this.symbol = symbol;
        }

        public static HelpColor from(ChatColor a, ChatColor b, char c) {
            return new HelpColor(
                    Style.style(NamedTextColor.NAMES.value(a.name().toLowerCase(Locale.ROOT)), TextDecoration.BOLD),
                    Style.style(NamedTextColor.NAMES.value(b.name().toLowerCase(Locale.ROOT))),
                    String.valueOf(c)
            );
        }
    }
}
