package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.UpdateChecker;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@CommandPermission(permission = "weaponmechanics.commands.info")
public class InfoCommand extends SubCommand {

    public InfoCommand() {
        super("wm", "info", "General plugin information");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        PluginDescriptionFile desc = WeaponMechanics.getPlugin().getDescription();
        sender.sendMessage("§7➢ §6§lWeapon§7§lMechanics§7, v§o" + desc.getVersion());

        List<String> authors = desc.getAuthors();
        if (!authors.isEmpty()) {
            sender.sendMessage("§7➢  §6Authors:§7 " + ArrayUtils.toString(authors));
        }

        // The main command
        sender.sendMessage("§7➢  §6Command:§7 /weaponmechanics");

        // Informs the user about any updates
        UpdateChecker updateChecker = WeaponMechanics.getUpdateChecker();
        if (updateChecker != null && updateChecker.hasUpdate()) {
            updateChecker.onUpdateFound(sender, updateChecker.getSpigotResource());
        }

        // Sends information about the server version
        sender.sendMessage("§7➢  §6Server:§7 " + Bukkit.getName() + " " + Bukkit.getVersion());

        // Information about MechanicsCore
        sender.sendMessage("§7➢  §6MechanicsCore:§7 " + MechanicsCore.getPlugin().getDescription().getVersion());

        // Information about java
        sender.sendMessage("§7➢  §6Java:§7 " + System.getProperty("java.version"));

        // Gets all supported plugins
        Set<String> softDepencies = new LinkedHashSet<>(desc.getSoftDepend());
        softDepencies.addAll(MechanicsCore.getPlugin().getDescription().getSoftDepend());
        softDepencies.remove("MechanicsCore");
        sender.sendMessage("§7➢  §6Supported plugins:§7 " + ArrayUtils.toString(softDepencies));
    }
}
