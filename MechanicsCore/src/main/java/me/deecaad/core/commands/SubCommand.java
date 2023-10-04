package me.deecaad.core.commands;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

/**
 * This immutable abstract class outlines a subsection or small section of a
 * plugin's command structure. A subcommand can be registered as a bukkit
 * command, but it should instead belong to the instantiating plugin's
 * {@link MainCommand}.
 */
@Deprecated()
public abstract class SubCommand extends BukkitCommand {

    protected static final String PLAYERS = "<player>";
    protected static final String INTEGERS = "<amount>";
    protected static final String SUB_COMMANDS = "<subcommand>";

    private static final SimpleCommandMap COMMAND_MAP;

    static {
        Method getCommandMap = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftServer"), "getCommandMap");
        COMMAND_MAP = (SimpleCommandMap) ReflectionUtil.invokeMethod(getCommandMap, Bukkit.getServer());
    }

    protected final SubCommands commands;
    private final String prefix;
    private final String[] args;

    /**
     * Constructor for simple subcommands that can only be used for 1 single
     * purpose. Subcommands using this constructor should not use any
     * arguments. E.x. Reload commands, plugin information commands.
     *
     * @param parentPrefix The arguments that come before the label. This is
     *                     usually the main command's label, but it may also
     *                     include parent subcommand's labels. If this
     *                     subcommand is being used as a standalone command,
     *                     this argument should be an empty {@link String}.
     * @param label        The unique name for this command. Should not be
     *                     null.
     * @param desc         A simple readable description for what this command
     *                     does.
     */
    public SubCommand(String parentPrefix, String label, String desc) {
        this(parentPrefix, label, desc, "");
    }

    /**
     * Constructor for advanced subcommands that have different behaviors based
     * on the command executor's arguments.
     *
     * @param parentPrefix The arguments that come before the label. This is
     *                     usually the main command's label, but it may also
     *                     include parent subcommand's labels. If this
     *                     subcommand is being used as a standalone command,
     *                     this argument should be an empty {@link String}.
     * @param label        The unique name for this command. Should not be
     *                     null.
     * @param desc         A simple readable description for what this command
     *                     does.
     * @param usage        The arguments that the command can take. Arguments
     *                     should be separated by spaces. Commands
     *                     which utilise custom tags need to override
     *                     {@link #handleCustomTag(String[], String)}. E.x.
     *                     <samp>&lt;player&gt; &lt;amount&gt;</samp>.
     */
    public SubCommand(String parentPrefix, String label, String desc, String usage) {
        super(label = label.toLowerCase(Locale.ROOT));

        this.prefix = parentPrefix.toLowerCase(Locale.ROOT) + " " + label;
        this.commands = new SubCommands(this.prefix);
        this.args = StringUtil.splitAfterWord(usage);

        setDescription(desc);

        if (getClass().isAnnotationPresent(CommandPermission.class)) {

            // Create the bukkit permission from the command permission
            CommandPermission perm = getClass().getAnnotation(CommandPermission.class);
            String str = perm.permission();
            Permission permission = new Permission(str);

            // Setup the parent "*" stuff
            permission.addParent(str.substring(0, str.lastIndexOf(".")) + ".*", true);

            // Register the permission
            Bukkit.getPluginManager().addPermission(permission);
        }
    }

    /**
     * Returns a non-null clone of the arguments that this subcommand accepts.
     *
     * @return A copy of the arguments.
     */
    public String[] getArgs() {
        return args.clone();
    }

    /**
     * Gets the non-null text put before the command, useful for command lists.
     *
     * @return The prefix of the subcommand.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the {@link String} value of the permission a command executor must
     * have to use this subcommand. If the implementing class is not annotated
     * by {@link CommandPermission}, this method may return <code>null</code>.
     *
     * @return The readable version of the permission
     */
    @Override
    public String getPermission() {
        if (getClass().isAnnotationPresent(CommandPermission.class)) {
            return getClass().getAnnotation(CommandPermission.class).permission();
        } else {
            return super.getPermission();
        }
    }

    protected boolean sendHelp(CommandSender sender, String[] args) {
        if (commands.isEmpty()) {
            TextComponent.Builder builder = text();

            // ChatColor.GOLD + "/" + prefix + " " + String.join(" ", args) + ChatColor.GRAY + ": " + description
            HoverEvent<?> hover = HoverEvent.showText(text("Click to fill command").color(NamedTextColor.GRAY));
            ClickEvent click = ClickEvent.suggestCommand("/" + prefix);

            builder.append(text("/" + prefix + " " + String.join(" ", args)).color(NamedTextColor.GOLD).clickEvent(click).hoverEvent(hover));
            builder.append(text(": " + description).color(NamedTextColor.GRAY).clickEvent(click).hoverEvent(hover));

            MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(builder);

            return true;
        } else {
            return commands.sendHelp(sender, args);
        }
    }

    List<String> tabCompletions(String[] args) {

        // Nothing is being typed, so we have no suggestions
        if (args.length == 0) return new ArrayList<>();

        // The string from usage at the same index as what
        // is being typed from args. If args.length is greater
        // then usage.length, it is out of bounds. This probably
        // means that subcommands are in use or the user is typing
        // past what the command will take in
        String current = this.args.length >= args.length ? this.args[args.length - 1] : "OUT_OF_BOUNDS";

        switch (current) {
            case PLAYERS:
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            case INTEGERS:
                return StringUtil.getList("1", "16", "64", "128");
            case SUB_COMMANDS:
                return commands.keys();
            case "OUT_OF_BOUNDS":
                int index = ArrayUtils.indexOf(this.args, SUB_COMMANDS);

                // If this command does not have subcommands, give no info
                if (index == -1) return new ArrayList<>();
                    // Else let subcommands handle tab completions
                else return commands.tabCompletions(args[index], Arrays.copyOfRange(args, index + 1, args.length));
            default:
                if (current.contains(",")) {
                    String[] split = current.replaceAll("[<>]", "").split(",");
                    return StringUtil.getList(split);
                } else {
                    return handleCustomTag(args, current);
                }
        }
    }

    /**
     * Returns a list of strings for the custom tag <code>tag</code>. This
     * method is called when an unrecognized tag is used during tab
     * completion.
     *
     * @param args    The arguments the user typed.
     * @param current The custom tag to handle.
     * @return The tab completions for the custom tag.
     */
    protected List<String> handleCustomTag(String[] args, String current) {
        return StringUtil.getList(current);
    }

    /**
     * What should be done when this command is executed.
     *
     * @param sender Who executed the command
     * @param args   The arguments input by the user
     */
    public abstract void execute(CommandSender sender, String[] args);

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (getPermission() != null && !sender.hasPermission(getPermission())) {
            sender.sendMessage(getPermissionMessage() == null ? ChatColor.RED + "Invalid Permissions" : getPermissionMessage());
            return false;
        }

        if (args.length > 1) {
            if (args[0].equals("help")) {
                sendHelp(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                execute(sender, args);
            }
        } else {
            execute(sender, args);
        }

        return true;
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (getPermission() != null && !sender.hasPermission(getPermission())) {
            return Collections.emptyList();
        } else {
            return tabCompletions(args);
        }
    }

    @Override
    public String toString() {
        return ChatColor.GOLD + "/" + prefix + " " + String.join(" ", args) + ChatColor.GRAY + ": " + description;
    }

    public void register() {
        COMMAND_MAP.register(getLabel(), this);
    }
}
