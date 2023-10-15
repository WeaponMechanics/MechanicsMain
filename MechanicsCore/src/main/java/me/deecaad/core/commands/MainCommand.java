package me.deecaad.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This abstract class outlines a bukkit plugin's main command that stores all
 * subcommands. A plugin should only have 1 main command, and all
 * implementations of {@link SubCommand} should be stored here. After
 * instantiating this class, it needs to be registered to the bukkit server's
 * main command map.
 *
 * @see SubCommand#register()
 */
@Deprecated()
public abstract class MainCommand extends BukkitCommand {

    protected String permission;
    protected SubCommands commands;

    /**
     * This constructor is only meant to be accessed from subclasses.
     *
     * @param name       The name/appearance of the command.
     * @param permission The readable value of a permission.
     */
    protected MainCommand(String name, String permission) {
        super(name);

        this.permission = permission;
        this.commands = new SubCommands("/" + name);

        // Get the shortest alias for the help command
        String prefix = name;
        for (String string : super.getAliases()) {
            if (string.length() < prefix.length())
                prefix = string;
        }

        SubCommand dummy = new SubCommand(prefix, "help", "General help information for commands", "<args>") {
            @Override
            public void execute(CommandSender sender, String[] args) {
            }
        };

        this.commands.register(dummy);
    }

    private boolean help(CommandSender sender, String[] args) {
        return commands.sendHelp(sender, args);
    }

    /**
     * Finds a {@link SubCommand} from the <code>args</code> and attempts to
     * execute it. If no command exists with the given information, the command
     * executor is sent a help message.
     *
     * @param sender The console/block/entity that sent the command.
     * @param label  The label of this command. Will always be equal to this
     *               command's tag, or one of this command's aliases.
     * @param args   The arguments given to find the {@link SubCommand}.
     * @return Returns true every time.
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "Invalid permissions!");
            return true;
        }

        // If this stays as false, send help message
        boolean isSuccessful = false;

        if (args.length > 0) {
            if (args[0].equals("help")) {
                isSuccessful = help(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                isSuccessful = commands.execute(args[0], sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        if (!isSuccessful) {
            help(sender, args);
        }
        return true;
    }

    /**
     * Finds a {@link SubCommand} from the <code>args</code> and attempts to
     * find possible <i>completions</i>, or options for what is currently
     * being typed. The options are filtered based on what is currently
     * being typed, and is later sorted into alphabetical order by the server.
     *
     * @param sender Who is typing the command.
     * @param alias  The label of the command.
     * @param args   The arguments being typed.
     * @return The possible options for arguments.
     */
    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(permission)) {
            return new ArrayList<>();
        }

        List<String> temp;

        // Command has arguments
        if (args.length > 1) {

            // Should the we follow help command tab completions?
            if (args[0].equals("help")) {
                if (args.length == 2) temp = commands.keys();
                else temp = commands.tabCompletions(args[1], Arrays.copyOfRange(args, 2, args.length));

                // Let subcommands handle tab completions
            } else {
                temp = commands.tabCompletions(args[0], Arrays.copyOfRange(args, 1, args.length));
            }
        } else {
            temp = commands.keys();
        }

        return temp.stream()
                .filter(string -> string.toLowerCase(Locale.ROOT).startsWith(args[args.length - 1].toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }
}
