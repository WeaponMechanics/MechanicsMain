package me.deecaad.core.commands;

import me.deecaad.core.MechanicsCore;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.*;

import static me.deecaad.core.MechanicsCore.debug;
import static net.kyori.adventure.text.Component.*;

@Deprecated()
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

            NamedTextColor GOLD = NamedTextColor.GOLD;
            NamedTextColor GRAY = NamedTextColor.GRAY;
            TextComponent.Builder builder = text();

            builder.append(text(parentPrefix).color(GRAY).decorate(TextDecoration.BOLD));
            builder.append(text(" Help ").color(GOLD).decorate(TextDecoration.BOLD));
            builder.append(text("(" + commands.size() + " Commands)").color(GRAY).decorate(TextDecoration.ITALIC));
            builder.append(newline());

            int i = 0;
            for (SubCommand command : commands.values()) {

                // Create a hoverable for the command
                HoverEvent<?> hover = HoverEvent.showText(text()
                       .append(text("Command: ").color(GOLD).append(text(command.getLabel()).color(GRAY).append(newline())))
                       .append(text("Description: ").color(GOLD).append(text(command.getDescription()).color(GRAY)).append(newline()))
                       .append(text("Usage: ").color(GOLD).append(text("/" + command.getPrefix() + " " + String.join("", command.getArgs())).color(GRAY).append(newline())))
                       .append(text("Permission: ").color(GOLD).append(text(command.getPermission() == null ? "N/A" : command.getPermission()).color(GRAY).append(newline())))
                       .append(newline())
                       .append(text("Click to auto-complete.").color(GRAY))
                );

                builder.append(text("  " + SYM + " ").color(GRAY));
                builder.append(text("/" + command.getPrefix()).color(GOLD).clickEvent(ClickEvent.suggestCommand("/" + command.getPrefix())).hoverEvent(hover));

                if (++i != commands.size()) {
                    // Every command goes on a new line, expect if this is last command
                    builder.append(newline());
                }
            }

            MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(builder);
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
            sender.sendMessage(ChatColor.RED + "Invalid permissions");
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
