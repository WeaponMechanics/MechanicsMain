package me.deecaad.core.commands;

import me.deecaad.core.utils.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SubCommand {

    protected static final String PLAYERS = "<player>";
    protected static final String INTEGERS = "<amount>";
    protected static final String SUB_COMMANDS = "<subcommand>";

    protected SubCommands commands;
    protected String prefix;
    private String label;
    private String desc;
    private String[] usage;

    public SubCommand(String parentPrefix, String label, String desc) {
        this(parentPrefix, label, desc, "");
    }

    public SubCommand(String parentPrefix, String label, String desc, String usage) {
        this.commands = new SubCommands();
        this.prefix = parentPrefix + " " + label;
        this.label = label;
        this.desc = desc;
        this.usage = StringUtils.splitAfterWord(usage);
    }

    public String getLabel() {
        return label;
    }

    public String getDesc() {
        return desc;
    }

    public String[] getUsage() {
        return usage;
    }

    /**
     * Sends information to the given sender about the
     * command defined by this class and the given args
     *
     * @param sender Who to send help to
     * @param args Command arguments
     * @return Whether or not the command is valid
     */
    public boolean sendHelp(CommandSender sender, String[] args) {
        if (commands.isEmpty()) {
            sender.sendMessage(StringUtils.color(toString()));
            return true;
        } else {
            return commands.sendHelp(sender, args);
        }
    }

    /**
     * Attempts to get TabCompletions based on what is currently
     * being typed. This is the more "general" method that may
     * let other overridden methods handle tabcompletions or let
     * subcommands handle tab completions. This method probably
     * shouldn't be overridden
     *
     * /ench enchant withering 3
     *
     * @param args
     * @return
     */
    public List<String> tabCompletions(String[] args) {

        // Nothing is being typed, so we have no suggestions
        if (args.length == 0) return new ArrayList<>();

        // The string from usage at the same index as what
        // is being typed from args. If args.length is greater
        // then usage.length, it is out of bounds. This probably
        // means that subcommands are in use or the user is typing
        // past what the command will take in
        String current = usage.length >= args.length ? usage[args.length - 1] : "OUT_OF_BOUNDS";

        switch (current) {
            case PLAYERS:
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            case INTEGERS:
                return StringUtils.getList("1", "16", "64", "128");
            case SUB_COMMANDS:
                return new ArrayList<>(commands.keys());
            case "OUT_OF_BOUNDS":
                int index = ArrayUtils.indexOf(usage, SUB_COMMANDS);

                // If this command does not have subcommands, give no info
                if (index == -1) return new ArrayList<>();
                // Else let subcommands handle tab completions
                else return commands.tabCompletions(args[index], Arrays.copyOfRange(args, index + 1, args.length));
            default:
                return tabCompletions(args, current);
        }
    }

    /**
     * Gets the tab completion based on the currently typed String.
     *
     * By default, this just checks for a tag like: "hi, how, are, you".
     * and adds each element into the list of tab completions.
     *
     * @param current What is currently being typed
     * @return The tab completions
     */
    protected List<String> tabCompletions(String[] args, String current) {
        if (current.contains(",")) {
            String[] split = current.replaceAll("[<>]", "").split(",");
            return StringUtils.getList(split);
        } else {
            return handleCustomTag(args, current);
        }
    }

    /**
     * If you have a custom tag, like "my_doubles" (Something that
     * isn't included in the default options), this method should be
     * overridden to handle that.
     *
     * @param args The arguments the user typed
     * @param tag The custom tag to handle
     * @return The tabcompletions for the custom tag
     */
    protected List<String> handleCustomTag(String[] args, String tag) {
        return StringUtils.getList(tag);
    }


    public abstract void execute(CommandSender sender, String[] args);

    @Override
    public String toString() {
        return "&6/" + prefix + " " + String.join(" ", usage) + "&7: " + desc;
    }
}
