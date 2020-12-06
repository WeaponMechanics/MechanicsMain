package me.deecaad.core.commands;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static me.deecaad.core.MechanicsCore.debug;

public abstract class SubCommand extends BukkitCommand {

    protected static final String PLAYERS = "<player>";
    protected static final String INTEGERS = "<amount>";
    protected static final String SUB_COMMANDS = "<subcommand>";

    private static final SimpleCommandMap COMMAND_MAP;

    static {
        Method getCommandMap = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftServer"), "getCommandMap");
        COMMAND_MAP = (SimpleCommandMap) ReflectionUtil.invokeMethod(getCommandMap, Bukkit.getServer());
    }

    protected SubCommands commands;
    private String prefix;
    private String[] args;

    public SubCommand(String parentPrefix, String label, String desc) {
        this(parentPrefix, label, desc, "");
    }

    public SubCommand(String parentPrefix, String label, String desc, String usage) {
        super(label);

        this.prefix = parentPrefix + " " + label;
        this.commands = new SubCommands(this.prefix);
        this.args = StringUtils.splitAfterWord(usage);

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

    public String[] getArgs() {
        return args;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getPermission() {
        if (getClass().isAnnotationPresent(CommandPermission.class)) {
            return getClass().getAnnotation(CommandPermission.class).permission();
        } else {
            return super.getPermission();
        }
    }

    /**
     * Sends information to the given sender about the
     * command defined by this class and the given args
     *
     * @param sender Who to send help to
     * @param args Command arguments
     * @return Whether or not the command is valid
     */
    protected boolean sendHelp(CommandSender sender, String[] args) {
        if (commands.isEmpty()) {
            if (CompatibilityAPI.getVersion() < 1.09 || !(sender instanceof Player)) {
                sender.sendMessage(StringUtils.color(toString()));
            } else {
                ComponentBuilder builder = new ComponentBuilder();
                for (BaseComponent component : TextComponent.fromLegacyText(StringUtils.color(toString()))) {
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + prefix));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to fill command")));
                    builder.append(component);
                }

                Player player = (Player) sender;
                player.spigot().sendMessage(builder.create());
            }
            return true;
        } else {
            return commands.sendHelp(sender, args);
        }
    }

    /**
     * Uses <code>this.args</code> to determine what
     * tab completion options the user
     *
     * @param args
     * @return
     */
    List<String> tabCompletions(String[] args) {

        // Nothing is being typed, so we have no suggestions
        if (args.length == 0) return new ArrayList<>();

        // The string from usage at the same index as what
        // is being typed from args. If args.length is greater
        // then usage.length, it is out of bounds. This probably
        // means that subcommands are in use or the user is typing
        // past what the command will take in
        String current = this.args.length >= args.length ? this.args[args.length - 1] : "OUT_OF_BOUNDS";

        debug.debug("TabCompleting: " + current);

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
                int index = ArrayUtils.indexOf(this.args, SUB_COMMANDS);

                // If this command does not have subcommands, give no info
                if (index == -1) return new ArrayList<>();
                // Else let subcommands handle tab completions
                else return commands.tabCompletions(args[index], Arrays.copyOfRange(args, index + 1, args.length));
            default:
                if (current.contains(",")) {
                    String[] split = current.replaceAll("[<>]", "").split(",");
                    return StringUtils.getList(split);
                } else {
                    return handleCustomTag(args, current);
                }
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

    /**
     * What should be done when this command is executed
     *
     * @param sender Who executed the command
     * @param args The arguments input by the user
     */
    public abstract void execute(CommandSender sender, String[] args);

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (getPermission() != null && !sender.hasPermission(getPermission())) {
            sender.sendMessage(getPermissionMessage());
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
    @Nonnull
    public List<String> tabComplete(@Nonnull CommandSender sender, @Nonnull String alias, @Nonnull String[] args) {
        if (!sender.hasPermission(getPermission())) {
            return Collections.emptyList();
        } else {
            return tabCompletions(args);
        }
    }

    @Override
    public String toString() {
        return StringUtils.color("&6/" + prefix + " " + String.join(" ", args) + "&7: " + description);
    }

    public void register() {
        COMMAND_MAP.register(getLabel(), this);
    }
}
