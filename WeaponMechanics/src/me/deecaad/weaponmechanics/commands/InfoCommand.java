package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.UpdateChecker;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public class InfoCommand extends SubCommand {
    
    public InfoCommand() {
        super("wm", "info", "General plugin information");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        // Just used for the plugin's version. Could be
        // used for more information if needed
        PluginDescriptionFile desc = WeaponMechanics.getPlugin().getDescription();
        sender.sendMessage("§7➢ §6§lWeapon§7§lMechanics§7, v§o" + desc.getVersion());
        sender.sendMessage("§7➢  §6Page:§7 spigot.com/best_plugin_ever"); // TODO
        sender.sendMessage("§7➢  §6Author:§7 DeeCaaD");
        sender.sendMessage("§7➢  §6Contributors:§7 CJCrafter"); // Plugging myself :stuck_out_tongue:
        sender.sendMessage("§7➢  §6Command:§7 /weaponmechanics");
        
        UpdateChecker checker = WeaponMechanics.getUpdateChecker();
        if (checker != null) {
            if (checker.hasUpdateAvailable()) {
                if (sender instanceof Player) {
                    checker.whenPlayerJoinsAndUpdateIsFound((Player) sender);
                } else {
                    double updates = checker.getVersionsBehind();

                    // Red if x>=10 updates, yellow if x>3, else green
                    char color = updates >= 10 ? 'c': updates > 3 ? 'e' : 'a';
                    sender.sendMessage("§7➢  §6You are §" + color + updates + "§6 behind");
                    sender.sendMessage("§7➢  §6Server version: §7" + checker.getResourceCurrentVersion());
                    sender.sendMessage("§7➢  §6Update version: §7" + checker.getLatestUpdateVersion());
                    sender.sendMessage("§7➢  §6Download the update at: §7spigot.com/best_plugin_ever");
                }
            } else {
                sender.sendMessage("§7➢  §6Plugin is up to date");
            }
        }
    }
}
