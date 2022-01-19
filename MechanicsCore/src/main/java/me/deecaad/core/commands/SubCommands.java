package me.deecaad.core.commands;

import me.deecaad.core.utils.StringUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.*;

import static me.deecaad.core.MechanicsCore.debug;
import static net.md_5.bungee.api.ChatColor.*;

public class SubCommands {

    public static char SYM = '\u27A2';
    private final Map<String, SubCommand> commands;
    private final String parentPrefix;

    public SubCommands(String parentPrefix) {
        this.commands = new HashMap<>(8); // Half of default size to save RAM
        this.parentPrefix = parentPrefix;
    }

    /**
     * Registers a given sub-command with its
     * label as if it's key (for execution)
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
        SubCommand command = commands.get(key);

        // If we couldn't find a command, check each registered command's aliases.
        if (command == null) {
            for (SubCommand cmd : commands.values())
                for (String alias : cmd.getAliases())
                    if (Objects.equals(key, alias))
                        return cmd;
        }

        return command;
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
     * Gets the label + aliases of every registered {@link SubCommand}.
     * Modifying the returned list does not modify the registered commands.
     *
     * @return The non-null list of keys.
     */
    public List<String> keys() {
        List<String> keys = new ArrayList<>(commands.keySet());

        for (BukkitCommand command : commands.values()) {
            keys.addAll(command.getAliases());
        }

        return keys;
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
    boolean sendHelp(CommandSender sender, String[] args) {
        if (args.length == 0) {

            // Create the messages with hover message
            ComponentBuilder builder = new ComponentBuilder();
            builder.append("Showing ").color(GOLD).bold(true)
                    .append(parentPrefix).color(GRAY).bold(true)
                    .append(" Help ").color(GOLD).bold(true)
                    .append("(" + commands.size() + " Commands)").reset().color(GRAY).italic(true)
                    .append("\n");

            for (SubCommand command : commands.values()) {

                // Create a hoverable for the command
                BaseComponent[] hover = new ComponentBuilder("Command: ").color(GOLD)
                        .append(command.getLabel()).color(GRAY)
                        .append("\n").append("Description: ").color(GOLD)
                        .append(command.getDescription()).color(GRAY)
                        .append("\n").append("Usage: ").color(GOLD)
                        .append("/" + command.getPrefix() + " " + String.join("", command.getArgs())).color(GRAY)
                        .append("\n").append("Permission: ").color(GOLD)
                        .append(command.getPermission() == null ? "N/A" : command.getPermission()).color(GRAY)
                        .append("\n\n").append("Click to auto-complete.").color(GRAY).create();

                builder.append("  " + SYM + " ").reset().color(GRAY);
                BaseComponent component = new TextComponent("/" + command.getPrefix());
                component.setColor(GOLD);
                component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + command.getPrefix()));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                builder.append(component);

                // Every command goes on a new line
                builder.append("\n");
            }

            // Remove the last new line, since it's extra
            builder.removeComponent(builder.getCursor());

            sender.spigot().sendMessage(builder.create());
            return true;
        }

        SubCommand command = get(args[0]);
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
        SubCommand command = get(key.toLowerCase(Locale.ROOT));
        if (command == null) {
            return false;
        } else if (command.getPermission() == null || sender.hasPermission(command.getPermission())) {
            command.execute(sender, args);
        } else {
            sender.sendMessage(RED + "Invalid permissions");
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
    List<String> tabCompletions(String key, String[] args) {
        SubCommand command = get(key.toLowerCase(Locale.ROOT));
        if (command == null) {
            debug.debug("Unknown sub-command: " + key);
            return new ArrayList<>();
        } else {
            return command.tabCompletions(args);
        }
    }
}
