package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.UpdateChecker;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.ArrayUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.bukkit.ChatColor.*;

@CommandPermission(permission = "weaponmechanics.commands.info")
public class InfoCommand extends SubCommand {

    public static char SYM = '\u27A2';

    public InfoCommand() {
        super("wm", "info", "General plugin information");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        PluginDescriptionFile desc = WeaponMechanics.getPlugin().getDescription();
        sender.sendMessage("" + GRAY + GOLD + BOLD + "Weapon" + GRAY + BOLD + "Mechanics"
                + GRAY + ", v" + ITALIC + desc.getVersion());

        sender.sendMessage("  " + GRAY + SYM + GOLD + " Authors: " + GRAY + ArrayUtil.toString(desc.getAuthors()));
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
        Set<String> softDepencies = new LinkedHashSet<>(desc.getSoftDepend());
        softDepencies.addAll(MechanicsCore.getPlugin().getDescription().getSoftDepend());
        softDepencies.remove("MechanicsCore");
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Supported plugins: " + GRAY + ArrayUtil.toString(softDepencies));
    }
}
