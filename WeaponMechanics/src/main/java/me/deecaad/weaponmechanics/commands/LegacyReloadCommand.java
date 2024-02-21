package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandPermission(permission = "weaponmechanics.commands.reload")
@Deprecated
public class LegacyReloadCommand extends SubCommand {

    public LegacyReloadCommand() {
        super("wm", "reload", "Reloads the plugin's config");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        WeaponMechanics plugin = WeaponMechanicsAPI.getInstance();

        plugin.onReload().thenRunSync(() -> sender.sendMessage(ChatColor.GREEN + "Reloaded configuration."));
    }
}
