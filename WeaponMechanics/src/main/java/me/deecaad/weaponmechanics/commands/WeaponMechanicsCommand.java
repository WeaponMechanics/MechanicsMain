package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.CommandBuilder;
import me.deecaad.core.commands.SuggestionsBuilder;
import me.deecaad.core.commands.Tooltip;
import me.deecaad.core.commands.Argument;
import me.deecaad.core.commands.arguments.EntityListArgumentType;
import me.deecaad.core.commands.arguments.IntegerArgumentType;
import me.deecaad.core.commands.arguments.StringArgumentType;
import me.deecaad.core.commands.CommandExecutor;
import me.deecaad.weaponmechanics.UpdateChecker;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.weapon.info.InfoHandler;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MinecraftFont;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;
import static org.bukkit.ChatColor.*;
import static org.bukkit.ChatColor.GRAY;

@SuppressWarnings("unchecked")
public class WeaponMechanicsCommand {

    public static String WIKI = "https://github.com/WeaponMechanics/MechanicsMain/wiki";
    public static char SYM = '\u27A2';

    public static void build() {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();

        CommandBuilder command = new CommandBuilder("weaponmechanics")
                .withAliases("wm")
                .withPermission("weaponmechanics.admin")
                .withDescription("WeaponMechanics' main command")
                .withSubCommand(new CommandBuilder("give")
                        .withPermission("weaponmechanics.commands.give")
                        .withDescription("Gives the target(s) with requested weapon(s)")
                        .withArgument(new Argument<>("target", new EntityListArgumentType()))
                        .withArgument(new Argument<>("weapon", new StringArgumentType(true))
                                .replace(SuggestionsBuilder.from(info.getSortedWeaponList())))
                        .withArgument(new Argument<>("amount", new IntegerArgumentType(1, 64), 1)
                                .append(SuggestionsBuilder.from(1, 16, 32, 64)))
                        .executes(CommandExecutor.any((sender, args) -> give(sender, (List<Entity>) args[0], (String) args[1], (int) args[2]))))

                .withSubCommand(new CommandBuilder("get")
                        .withPermission("weaponmechanics.commands.get")
                        .withDescription("Gives you the requested weapon(s)")
                        .withArgument(new Argument<>("weapon", new StringArgumentType(true))
                                .replace(SuggestionsBuilder.from(info.getSortedWeaponList())))
                        .withArgument(new Argument<>("amount", new IntegerArgumentType(1), 1)
                                .append(SuggestionsBuilder.from(1, 16, 32, 64)))
                        .executes(CommandExecutor.entity((sender, args) -> give(sender, Collections.singletonList(sender), (String) args[0], (int) args[1]))))

                .withSubCommand(new CommandBuilder("info")
                        .withPermission("weaponmechanics.commands.info")
                        .withDescription("Displays version/debug information about WeaponMechanics and your server")
                        .executes(CommandExecutor.any((sender, args) -> info(sender))))

                .withSubCommand(new CommandBuilder("list")
                        .withPermission("weaponmechanics.commands.list")
                        .withDescription("Lists a table of weapons loaded by WeaponMechanics")
                        .withArgument(new Argument<>("page", new IntegerArgumentType(1), 1)
                                .append(data -> IntStream.range(1, 1 + info.getSortedWeaponList().size() / 16).mapToObj(Tooltip::of).toArray(Tooltip[]::new))))

                .withSubCommand(new CommandBuilder("wiki")
                        .withPermission("weaponmechanics.commands.wiki")
                        .withDescription("Shows useful (clickable) links to specific useful areas on the wiki")
                        .executes(CommandExecutor.any((sender, args) -> wiki(sender))))

                .withSubCommand(new CommandBuilder("reload")
                        .withPermission("weaponmechanics.commands.reload")
                        .withDescription("Reloads WeaponMechanics' weapon configuration without restarting the server")
                        .executes(CommandExecutor.any((sender, args) -> WeaponMechanicsAPI.getInstance().onReload().thenRunSync(() -> sender.sendMessage(ChatColor.GREEN + "Reloaded configuration")))));


        CommandBuilder test = new CommandBuilder("test")
                .withPermission("weaponmechanics.commands.test")
                .withDescription("Contains useful commands for developers and testing and debugging")
                .withSubCommand(new CommandBuilder("nbt")
                        .withPermission("weaponmechanics.commands.test.nbt")
                        .withDescription(""));

        command.withSubCommand(test);
        command.register();
    }

    public static void give(CommandSender sender, List<Entity> targets, String weaponTitle, int amount) {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();
        weaponTitle = info.getWeaponTitle(weaponTitle);
        ItemStack weapon = info.generateWeapon(weaponTitle, amount);

        if (targets.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No entities were found");
            return;
        }

        int count = 0;
        for (Entity entity : targets) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.getInventory().addItem(weapon);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                count++;

            } else if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).getEquipment().setItemInMainHand(weapon);
                count++;

            }
        }

        if (count > 1) {
            sender.sendMessage(ChatColor.GRAY + "" + count + ChatColor.GREEN + " entities were given " + ChatColor.GRAY + amount + " " + weaponTitle + ChatColor.GREEN + "s");
        } else if (count == 1) {
            sender.sendMessage(ChatColor.GRAY + targets.get(0).getName() + ChatColor.GREEN + " was given " + ChatColor.GRAY + amount + " " + weaponTitle + ChatColor.GREEN + "s");
        } else {
            sender.sendMessage(ChatColor.RED + "No entities were given a weapon");
        }
    }

    public static void info(CommandSender sender) {
        PluginDescriptionFile desc = WeaponMechanics.getPlugin().getDescription();
        sender.sendMessage("" + GRAY + GOLD + BOLD + "Weapon" + GRAY + BOLD + "Mechanics"
                + GRAY + ", v" + ITALIC + desc.getVersion());

        sender.sendMessage("  " + GRAY + SYM + GOLD + " Authors: " + GRAY + String.join(", ", desc.getAuthors()));
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Command:" + GRAY + " /weaponmechanics");

        // Informs the user about any updates
        UpdateChecker updateChecker = WeaponMechanics.getUpdateChecker();
        if (updateChecker != null && updateChecker.hasUpdate()) {
            updateChecker.onUpdateFound(sender, updateChecker.getSpigotResource());
        }

        // Sends information about the server version
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Server: " + GRAY + Bukkit.getName() + " " + Bukkit.getVersion());

        // Information about MechanicsCore
        sender.sendMessage("  " + GRAY + SYM + GOLD + " MechanicsCore: " + GRAY + MechanicsCore.getPlugin().getDescription().getVersion());

        // Information about java
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Java: " + GRAY + System.getProperty("java.version"));

        // Gets all supported plugins
        Set<String> softDepends = new LinkedHashSet<>(desc.getSoftDepend());
        softDepends.addAll(MechanicsCore.getPlugin().getDescription().getSoftDepend());
        softDepends.remove("MechanicsCore");
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Supported plugins: " + GRAY + String.join(", ", softDepends));
    }

    public static void list(CommandSender sender, int requestedPage) {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();

        // We need information to build a table of weapons. There should have 8
        // rows and 2 columns (Since the standard size chat has 10 rows, and
        // enough space for 2 columns of weapon-titles). We reserve 2 rows for
        // page turning buttons and a header.
        int maxPerPage = 2 * 8;
        List<String> weapons = info.getSortedWeaponList();

        // Check to see if the page exists
        if (requestedPage < 0 || requestedPage * maxPerPage >= weapons.size()) {
            sender.sendMessage(net.md_5.bungee.api.ChatColor.RED + "The page you requested (" + (requestedPage + 1) + ") does not exist.");
            return;
        }

        // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/map/MapFont.html
        // MapFont allows us to evaluate the length, in pixels, of a string. MC
        // chat (by default) is 320 pixels wide.
        ComponentBuilder builder = new ComponentBuilder();
        builder.append("==================").color(net.md_5.bungee.api.ChatColor.GOLD)
                .append("  WeaponMechanics  ").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true)
                .append("==================").color(net.md_5.bungee.api.ChatColor.GOLD).italic(false)
                .append("\n");

        int cellSize = 160 - MinecraftFont.Font.getWidth(" » ") * 2;
        int i;
        for (i = requestedPage * maxPerPage; i < (requestedPage + 1) * maxPerPage && i < weapons.size(); i++) {

            // Each table cell needs to fit within the pixel size limit. This
            // prevents an empty row from being created and messing up the
            // table's format. If a weapon-title is longer then the cell size,
            // the weapon-title is trimmed down to the proper size.
            StringBuilder cell = new StringBuilder(weapons.get(i));
            while (MinecraftFont.Font.getWidth(cell.toString()) < cellSize)
                cell.append(' ');
            while (MinecraftFont.Font.getWidth(cell.toString()) > cellSize)
                cell.setLength(cell.length() - 1);

            ItemStack weapon = info.generateWeapon(weapons.get(i), 1);
            ComponentBuilder hover = new ComponentBuilder();

            // We want to display the gun so the player knows: 1) Exactly which
            // weapon they are choosing, 2) That they can click the buttons
            // TODO Use show item using NMS? SHOW_ITEM enum is useless, so...
            if (weapon == null) {
                hover.append("Error in weapon config checking, check console!").color(net.md_5.bungee.api.ChatColor.RED);
            } else if (weapon.hasItemMeta()) {
                ItemMeta meta = weapon.getItemMeta();
                assert meta != null;

                hover.append(TextComponent.fromLegacyText(meta.getDisplayName()));
                if (meta.hasLore() && meta.getLore() != null) {
                    for (String str : meta.getLore())
                        hover.append(TextComponent.fromLegacyText(str));
                }
            }

            // Add the weapon-title with hover/click events to the table.
            builder.append(" \u27A2 ").reset().color(net.md_5.bungee.api.ChatColor.GOLD)
                    .append(cell.toString()).color(net.md_5.bungee.api.ChatColor.GRAY)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm get " + weapons.get(i)))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()));

            // After filling the 2 columns, we can move to the next row.
            if (i % 2 == 1)
                builder.append("\n");
        }

        // If there weren't enough weapons to fill up a row completely, then
        // we need to add a new line for the page selector.
        if (i % 2 == 1)
            builder.append("\n");

        // Add the 'previous page' and 'next page' options below the table
        builder.append("================== ").reset().color(net.md_5.bungee.api.ChatColor.GOLD)
                .append("«").color(net.md_5.bungee.api.ChatColor.GRAY).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to go to the previous page")))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm list " + (requestedPage)))
                .append("                   ").reset()
                .append("»").color(net.md_5.bungee.api.ChatColor.GRAY).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to go to the next page")))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm list " + (requestedPage + 2)))
                .append(" ==================").color(net.md_5.bungee.api.ChatColor.GOLD).bold(false);

        sender.spigot().sendMessage(builder.create());
    }

    public static void wiki(CommandSender sender) {

        ComponentBuilder builder = new ComponentBuilder();
        builder.append("Weapon").color(GOLD).bold(true)
                .append("Mechanics").color(net.md_5.bungee.api.ChatColor.GRAY).bold(true)
                .append(" Wiki (Click an option)").bold(false).color(net.md_5.bungee.api.ChatColor.GRAY).italic(true)
                .append("\n").italic(false);

        BaseComponent[] hover = new ComponentBuilder()
                .append("Click to go to Wiki.").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true)
                .create();

        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Information", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Skins", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Projectile", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Shooting", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Reloading", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Damage", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Explosion", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Scoping", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Firearms", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Melee", hover));

        sender.spigot().sendMessage(builder.create());
    }

    private static BaseComponent build(String name, BaseComponent[] hover) {
        BaseComponent component = new TextComponent(name);
        component.setColor(GOLD);
        component.setClickEvent(new ClickEvent(OPEN_URL, WIKI + "/" + name));
        component.setHoverEvent(new HoverEvent(SHOW_TEXT, hover));
        return component;
    }
}
