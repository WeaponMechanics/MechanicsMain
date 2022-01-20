package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.info.InfoHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CommandPermission(permission = "weaponmechanics.commands.list")
public class ListWeaponsCommand extends SubCommand {

    // wm list <page>

    public ListWeaponsCommand() {
        super("wm", "list", "List all weapons by weapon title", "<page>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();

        // We need information to build a table of weapons. There should have 8
        // rows and 2 columns (Since the standard size chat has 10 rows, and
        // enough space for 2 columns of weapon-titles). We reserve 2 rows for
        // page turning buttons and a header.
        int maxPerPage = 2 * 8;
        List<String> weapons = info.getSortedWeaponList();

        // Parse command arguments
        // In the future, we could consider adding filters (E.x. Filter by
        // melee/full auto/semi auto/burst/explosive/etc.)
        int requestedPage = args.length > 0 ? Integer.parseInt(args[0]) - 1 : 0;

        // Check to see if the page exists
        if (requestedPage < 0 || requestedPage * maxPerPage >= weapons.size()) {
            sender.sendMessage(ChatColor.RED + "The page you requested (" + (requestedPage + 1) + ") does not exist.");
            return;
        }

        // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/map/MapFont.html
        // MapFont allows us to evaluate the length, in pixels, of a string. MC
        // chat (by default) is 320 pixels wide.
        ComponentBuilder builder = new ComponentBuilder();
        builder.append("================== WeaponMechanics ==================");

        int cellSize = 320 - MinecraftFont.Font.getWidth(" \u27A2 ") * 2;
        for (int i = requestedPage * maxPerPage; i < (requestedPage + 1) * maxPerPage && i < weapons.size(); i++) {

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
            if (weapon.hasItemMeta()) {
                ItemMeta meta = weapon.getItemMeta();
                assert meta != null;

                hover.append(TextComponent.fromLegacyText(meta.getDisplayName()));
                if (meta.hasLore() && meta.getLore() != null) {
                    for (String str : meta.getLore())
                        hover.append(TextComponent.fromLegacyText(str));
                }
            }

            // Add the weapon-title with hover/click events to the table.
            builder.append(" \u27A2 ").color(ChatColor.GOLD)
                    .append(cell.toString()).color(ChatColor.GRAY)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm get " + weapons.get(i)))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()));

            // After filling the 2 columns, we can move to the next row.
            if (i % 2 == 1)
                builder.append("\n");

        }

        // Add the 'previous page' and 'next page' options below the table
        builder.append("================== ").color(ChatColor.GOLD)
                .append("<<").color(ChatColor.GRAY).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to go to the previous page")))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm list " + (requestedPage - 1)))
                .append("                   ")
                .append(">>").color(ChatColor.GRAY).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to go to the next page")))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm list " + (requestedPage + 1)))
                .append(" ==================").color(ChatColor.GOLD);

        sender.spigot().sendMessage(builder.create());
    }

    @Override
    protected List<String> handleCustomTag(String[] args, String current) {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();

        switch (current) {
            case "<page>":
                int maxPerPage = 2 * 8;
                int pages = 1 + info.getSortedWeaponList().size() / maxPerPage;
                return IntStream.range(1, pages + 2).mapToObj(String::valueOf).collect(Collectors.toList());
            default:
                return super.handleCustomTag(args, current);
        }
    }
}
