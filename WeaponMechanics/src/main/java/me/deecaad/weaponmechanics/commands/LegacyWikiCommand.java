package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatColor.GRAY;
import static net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

@CommandPermission(permission = "weaponmechanics.command.wiki")
@Deprecated
public class LegacyWikiCommand extends SubCommand {

    public LegacyWikiCommand() {
        super("wm", "wiki", "Shows links to the Wiki");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        WeaponMechanicsCommand.wiki(sender);
    }
}
