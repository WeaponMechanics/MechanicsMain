package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import org.bukkit.command.CommandSender;

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
