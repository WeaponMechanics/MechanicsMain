package me.deecaad.core.commands;

import joptsimple.internal.Strings;
import me.deecaad.core.utils.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

import static me.deecaad.core.MechanicsCore.debug;

public class SubCommands {

    private Map<String, SubCommand> commands;
    private String parentPrefix;

    public SubCommands(String parentPrefix) {
        this.commands = new HashMap<>();
        this.parentPrefix = parentPrefix;
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
    boolean sendHelp(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(StringUtil.color(toString()));
            } else {

                // Create the messages with ho
                ComponentBuilder builder = new ComponentBuilder();
                builder.append("Showing " + commands.size() + " Sub-Commands (" + parentPrefix + ")").color(ChatColor.GOLD).append("\n");

                for (SubCommand command : commands.values()) {

                    // Create a hoverable for the command
                    BaseComponent[] hover = new ComponentBuilder("Command: ").color(ChatColor.GOLD)
                            .append(command.getLabel()).color(ChatColor.GRAY)
                            .append("\n").append("Description: ").color(ChatColor.GOLD)
                            .append(command.getDescription()).color(ChatColor.GRAY)
                            .append("\n").append("Usage: ").color(ChatColor.GOLD)
                            .append("/" + command.getPrefix() + " " + Strings.join(command.getArgs(), " ")).color(ChatColor.GRAY)
                            .append("\n").append("Permission: ").color(ChatColor.GOLD)
                            .append(command.getPermission() == null ? "N/A" : command.getPermission()).color(ChatColor.GRAY)
                            .append("\n\n").append("Click to auto-complete.").color(ChatColor.GRAY).create();

                    builder.append("➢ ").color(ChatColor.GRAY);
                    BaseComponent component = new TextComponent("/" + command.getPrefix());
                    component.setColor(ChatColor.GOLD);
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + command.getPrefix()));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                    builder.append(component);

                    // Every command goes on a new line
                    builder.append("\n");
                }

                // Remove the last new line, since it's extra
                builder.removeComponent(builder.getCursor());

                Player player = (Player) sender;
                player.spigot().sendMessage(builder.create());
            }
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
        } else if (command.getPermission() == null || sender.hasPermission(command.getPermission())) {
            command.execute(sender, args);
        } else {
            sender.sendMessage(StringUtil.color("&cInvalid permissions."));
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
        SubCommand command = commands.get(key);
        if (command == null) {
            debug.debug("Unknown sub-command: " + key);
            return new ArrayList<>();
        } else {
            return command.tabCompletions(args);
        }
    }
}
