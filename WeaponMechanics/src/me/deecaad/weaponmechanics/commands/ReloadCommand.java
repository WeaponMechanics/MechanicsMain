package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import org.bukkit.command.CommandSender;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

@CommandPermission(permission = "weaponmechanics.commands.reload")
public class ReloadCommand extends SubCommand {
    
    public ReloadCommand() {
        super("wm", "reload", "Reloads the plugin's config");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        WeaponMechanics plugin = WeaponMechanicsAPI.getInstance();
        if (plugin == null) {
            debug.log(LogLevel.WARN, "Tried to reload before the plugin loaded the API",
                    "Please wait before using /wm reload");
            return;
        }
        plugin.onReload();
        sender.sendMessage("§aReloaded configuration. Check console for errors.");
    }
}
