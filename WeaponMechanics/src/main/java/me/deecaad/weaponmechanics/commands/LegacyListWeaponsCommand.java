package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.info.InfoHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MinecraftFont;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CommandPermission(permission = "weaponmechanics.commands.list")
@Deprecated
public class LegacyListWeaponsCommand extends SubCommand {

    // wm list <page>

    public LegacyListWeaponsCommand() {
        super("wm", "list", "List all weapons by weapon title", "<page>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int page = Integer.parseInt(args.length == 0 ? "1" : args[0]);
        WeaponMechanicsCommand.list(sender, page);
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
