package me.deecaad.core;

import me.deecaad.core.commands.Argument;
import me.deecaad.core.commands.CommandBuilder;
import me.deecaad.core.commands.CommandExecutor;
import me.deecaad.core.commands.HelpCommandBuilder;
import me.deecaad.core.commands.SuggestionsBuilder;
import me.deecaad.core.commands.arguments.StringArgumentType;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.TableBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
                })
                .collect(Collectors.toList());

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
                    hover.append(text("Authors: ", gray)).append(text(desc.getAuthors().toString(), gold))/*.append(newline())*/;

                    return text().content(plugin.getName().toUpperCase(Locale.ROOT))
                            .hoverEvent(hover.build())
                            .build();
                })
                .build();

        MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(table);
    }
}
