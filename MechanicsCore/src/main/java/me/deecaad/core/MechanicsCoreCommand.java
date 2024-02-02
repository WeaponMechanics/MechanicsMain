package me.deecaad.core;

import me.deecaad.core.commands.*;
import me.deecaad.core.commands.arguments.StringArgumentType;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.core.utils.TableBuilder;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.*;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GRAY;

public final class MechanicsCoreCommand {

    public static char SYM = '\u27A2';

    /**
     * Don't let anyone instantiate this class
     */
    private MechanicsCoreCommand() {
    }

    public static void build() {
        CommandBuilder command = new CommandBuilder("mechanicscore")
            .withPermission("mechanicscore.admin")
            .withDescription("MechanicsCore debug/test commands")

            .withSubcommand(new CommandBuilder("table")
                .withPermission("mechanicscore.commands.table")
                .withDescription("Helpful tables that are used on the wiki")

                .withSubcommand(new CommandBuilder("colors")
                    .withPermission("mechanicscore.commands.table.colors")
                    .withDescription("Shows legacy color codes and the adventure version")
                    .executes(CommandExecutor.any((sender, args) -> {
                        tableColors(sender);
                    }))))

            .withSubcommand(new CommandBuilder("item")
                .withPermission("mechanicscore.commands.item")
                .withDescription("Gives an item from the MechanicsCore > Items folder")
                .withArgument(new Argument<>("type", new StringArgumentType()).replace(SuggestionsBuilder.from(ItemSerializer.ITEM_REGISTRY.keySet())))
                .executes(CommandExecutor.player((sender, args) -> {
                    Supplier<ItemStack> item = ItemSerializer.ITEM_REGISTRY.get((String) args[0]);

                    if (item == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown item " + args[0]);
                        return;
                    }

                    sender.getInventory().addItem(item.get());
                })))

            .withSubcommand(new CommandBuilder("reloadcommands")
                .withPermission("mechanicscore.commands.reloadcommands")
                .withDescription("Resends command registry to all online players")
                .executes(CommandExecutor.any((sender, args) -> {
                    reloadCommands(sender);
                })))

            .withSubcommand(new CommandBuilder("printcommands")
                .withPermission("mechanicscore.commands.printcommands")
                .withDescription("Prints the server's entire command registry to json file")
                .withArgument(new Argument<>("file", new StringArgumentType(), "commands").append(SuggestionsBuilder.from("file", "commands")))
                .executes(CommandExecutor.any((sender, args) -> {
                    printCommands(sender, (String) args[0]);
                })))

            .withSubcommand(new CommandBuilder("plugins")
                .withPermission("mechanicscore.commands.plugins")
                .withDescription("Shows all plugins currently using MechanicsCore")
                .executes(CommandExecutor.any((sender, args) -> {
                    listPlugins(sender);
                })));

        HelpCommandBuilder.register(command, HelpCommandBuilder.HelpColor.from(GOLD, GRAY, SYM));
        command.register();
    }

    public static void reloadCommands(CommandSender sender) {
        Bukkit.getOnlinePlayers().forEach(CompatibilityAPI.getCommandCompatibility()::resendCommandRegistry);
        sender.sendMessage(ChatColor.GREEN + "Resent commands");
    }

    public static void printCommands(CommandSender sender, String name) {
        if (!name.endsWith(".json"))
            name += ".json";

        File output = new File(MechanicsCore.getPlugin().getDataFolder(), name);
        CompatibilityAPI.getCommandCompatibility().generateFile(output);
        sender.sendMessage(ChatColor.GREEN + "Wrote command registry to " + output);
    }

    public static void listPlugins(CommandSender sender) {
        List<Plugin> plugins = Arrays.stream(Bukkit.getPluginManager().getPlugins())
            .filter(plugin -> {
                PluginDescriptionFile desc = plugin.getDescription();
                return desc.getDepend().contains("MechanicsCore") || desc.getSoftDepend().contains("MechanicsCore");
            }).toList();

        Style gold = Style.style(NamedTextColor.GOLD);
        Style gray = Style.style(NamedTextColor.GRAY);
        TextComponent table = new TableBuilder()
            .withConstraints(TableBuilder.DEFAULT_CONSTRAINTS)
            .withElementChar('-')
            .withElementCharStyle(gold)
            .withFillChar('=')
            .withFillCharStyle(Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
            .withHeader("Plugins using MechanicsCore")
            .withHeaderStyle(gold)
            .withElementStyle(gray)
            .withAttemptSinglePixelFix()
            .withSupplier(i -> {
                if (plugins.size() <= i)
                    return empty();

                Plugin plugin = plugins.get(i);
                PluginDescriptionFile desc = plugin.getDescription();

                // When the player hovers over the plugin, we should see some
                // general plugin info (version, authors)
                // TODO All MechanicsCore plugins should inherit from a base class
                // TODO to provide more info, like commands and updates. "One stop shopping"
                TextComponent.Builder hover = text();
                hover.append(text("Version: ", gray)).append(text(desc.getVersion(), gold)).append(newline());
                hover.append(text("Authors: ", gray)).append(text(desc.getAuthors().toString(), gold))/* .append(newline()) */;

                return text().content(plugin.getName().toUpperCase(Locale.ROOT))
                    .hoverEvent(hover.build())
                    .build();
            })
            .build();

        MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(table);
    }

    public static void tableColors(CommandSender sender) {

        final List<ColorData> colors = new ArrayList<>();
        colors.add(new ColorData("&0", "<black>", NamedTextColor.BLACK));
        colors.add(new ColorData("&1", "<dark_blue>", NamedTextColor.DARK_BLUE));
        colors.add(new ColorData("&2", "<dark_green>", NamedTextColor.DARK_GREEN));
        colors.add(new ColorData("&3", "<dark_aqua>", NamedTextColor.DARK_AQUA));
        colors.add(new ColorData("&4", "<dark_red>", NamedTextColor.DARK_RED));
        colors.add(new ColorData("&5", "<dark_purple>", NamedTextColor.DARK_PURPLE));
        colors.add(new ColorData("&6", "<gold>", NamedTextColor.GOLD));
        colors.add(new ColorData("&7", "<gray>", NamedTextColor.GRAY));
        colors.add(new ColorData("&8", "<dark_gray>", NamedTextColor.DARK_GRAY));
        colors.add(new ColorData("&9", "<blue>", NamedTextColor.BLUE));
        colors.add(new ColorData("&a", "<green>", NamedTextColor.GREEN));
        colors.add(new ColorData("&b", "<aqua>", NamedTextColor.AQUA));
        colors.add(new ColorData("&c", "<red>", NamedTextColor.RED));
        colors.add(new ColorData("&d", "<light_purple>", NamedTextColor.LIGHT_PURPLE));
        colors.add(new ColorData("&e", "<yellow>", NamedTextColor.YELLOW));
        colors.add(new ColorData("&f", "<white>", NamedTextColor.WHITE));

        final List<ColorData> decorations = new ArrayList<>();
        decorations.add(new ColorData("&k", "<obfuscated>", TextDecoration.OBFUSCATED));
        decorations.add(new ColorData("&l", "<bold>", TextDecoration.BOLD));
        decorations.add(new ColorData("&m", "<strikethrough>", TextDecoration.STRIKETHROUGH));
        decorations.add(new ColorData("&n", "<underline>", TextDecoration.UNDERLINED));
        decorations.add(new ColorData("&o", "<italic>", TextDecoration.ITALIC));
        decorations.add(new ColorData("&r", "<reset>", NamedTextColor.WHITE));

        Component colorComponent = new TableBuilder()
            .withFillChar('=')
            .withFillCharStyle(Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
            .withHeader("COLORS")
            .withHeaderStyle(Style.style(NamedTextColor.GOLD))
            .withConstraints(TableBuilder.DEFAULT_CONSTRAINTS.setRows(8))
            .withSupplier(i -> {
                return colors.get(i).build();
            })
            .build();

        Component decorationComponent = new TableBuilder()
            .withFillChar('=')
            .withFillCharStyle(Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
            .withHeader("DECORATIONS")
            .withHeaderStyle(Style.style(NamedTextColor.GOLD))
            .withConstraints(TableBuilder.DEFAULT_CONSTRAINTS.setRows(3))
            .withSupplier(i -> {
                return decorations.get(i).build();
            })
            .build();

        Component miscComponent = new TableBuilder()
            .withFillChar('=')
            .withFillCharStyle(Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
            .withHeader("MISCELLANEOUS")
            .withHeaderStyle(Style.style(NamedTextColor.GOLD))
            .withConstraints(TableBuilder.DEFAULT_CONSTRAINTS.setRows(3).setColumns(1))
            .withSupplier(i -> {
                return switch (i) {
                    case 0 ->
                        new ColorData("&#7D5A2D", "<#7D5A2D>", TextColor.color(125, 90, 45)).alt("The five boxing wizards jump quickly").build();
                    case 1 ->
                        text("<rainbow> = ").append(MechanicsCore.getPlugin().message.deserialize("<rainbow>The quick brown fox jumps over the lazy dog"));
                    case 2 ->
                        text("<gradient:green:#ff0000> = ").append(MechanicsCore.getPlugin().message.deserialize("<gradient:green:#ff0000>A wizard's job is to vex chumps"));
                    default -> throw new RuntimeException("unreachable code");
                };
            })
            .build();

        Audience audience = MechanicsCore.getPlugin().adventure.sender(sender);
        audience.sendMessage(colorComponent.append(decorationComponent).append(miscComponent).append(new TableBuilder.Line('=', Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
            .build()));
    }

    private static class ColorData {

        private String legacy;
        private String adventure;
        private TextColor color;
        private TextDecoration decoration;

        private String altText;

        public ColorData(String legacy, String adventure, TextColor color) {
            this.legacy = legacy;
            this.adventure = adventure;
            this.color = color;
        }

        public ColorData(String legacy, String adventure, TextDecoration decoration) {
            this.legacy = legacy;
            this.adventure = adventure;
            this.decoration = decoration;
        }

        private ColorData alt(String altText) {
            this.altText = altText;
            return this;
        }

        private TextComponent build() {
            TextComponent.Builder builder = text();
            builder.append(text(adventure + " = "));

            String readable = altText != null ? altText : StringUtil.keyToRead(adventure.substring(1, adventure.length() - 1));
            if (color != null)
                builder.append(text(readable).color(color));
            else
                builder.append(text(readable).decorate(decoration));

            return builder.build();
        }
    }
}
