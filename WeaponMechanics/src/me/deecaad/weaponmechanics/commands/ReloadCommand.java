package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.command.CommandSender;

@CommandPermission(permission = "weaponmechanics.commands.reload")
public class ReloadCommand extends SubCommand {
    
    public ReloadCommand() {
        super("wm", "reload", "Reloads the plugin's config");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        WeaponMechanics plugin = (WeaponMechanics) WeaponMechanics.getPlugin();

        plugin.onReload();
        sender.sendMessage("§aReloaded configuration. Check console for errors.");
    }
}
