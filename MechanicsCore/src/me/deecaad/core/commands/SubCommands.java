package me.deecaad.core.commands;

import me.deecaad.core.utils.StringUtils;
import org.bukkit.command.CommandSender;
import sun.misc.MessageUtils;

import java.util.*;

public class SubCommands {

    private Map<String, SubCommand> commands;

    public SubCommands() {
        commands = new HashMap<>();
    }

    /**
     * Registers a given sub-command with it's
     * label as it it's key (for execution)
     *
     * @param command to register
     */
    public void register(SubCommand command) {
        commands.put(command.getLabel(), command);
    }

    /**
     * Registers a given sub-command with the
     * given key (for execution)
     *
     * @param key The key to use for activation
     * @param command to register
     */
    public void register(String key, SubCommand command) {
        commands.put(key, command);
    }

    /**
     * @return Command map
     */
    public Map<String, SubCommand> get() {
        return commands;
    }

    /**
     * Gets the sub-command by it's activation key
     *
     * @param key The activation key
     * @return The command
     */
    public SubCommand get(String key) {
        return commands.get(key);
    }

    /**
     * Removes the command with the given
     * activation key
     *
     * @param key The activation key
     * @return The removed command
     */
    public SubCommand remove(String key) {
        return commands.remove(key);
    }

    /**
     * Gets the activation keys from every
     * sub-command
     *
     * @return The activation keys
     */
    public Set<String> keys() {
        return commands.keySet();
    }

    /**
     * @return If there are any sub-commands registered
     */
    public boolean isEmpty() {
        return commands.isEmpty();
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
        if (args.length == 0) {
            commands.forEach((key, cmd) -> sender.sendMessage(StringUtils.color("&7➢  " + cmd.toString())));
            return true;
        }

        SubCommand command = commands.get(args[0]);
        if (command == null) return false;

        return command.sendHelp(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    /**
     * Executes the given command, if present. Else
     * the default command is executed.
     *
     * @param key The command's activation key
     * @param sender Who is executing the command
     * @param args What is being typed
     */
    public boolean execute(String key, CommandSender sender, String[] args) {
        SubCommand command = commands.get(key);
        if (command == null) {
            return false;
        } else if (command.hasPermission(sender)) {
            command.execute(sender, args);
        } else {
            sender.sendMessage(StringUtils.color("&cInvalid permissions."));
        }
        return true;
    }

    /**
     * Gets the tab completions for a given command,
     * if present. Else an empty list is returned
     *
     * @param key the command's activation key
     * @param args what is being typed
     * @return tab completions
     */
    public List<String> tabCompletions(String key, String[] args) {
        SubCommand command = commands.get(key);
        if (command == null) {
            return new ArrayList<>();
        } else {
            return command.tabCompletions(args);
        }
    }
}
