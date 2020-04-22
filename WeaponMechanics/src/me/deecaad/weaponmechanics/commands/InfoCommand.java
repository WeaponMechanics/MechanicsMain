package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.UpdateChecker;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.List;

public class InfoCommand extends SubCommand {
    
    public InfoCommand() {
        super("wm", "info", "General plugin information");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        PluginDescriptionFile desc = WeaponMechanics.getPlugin().getDescription();
        sender.sendMessage("§7➢ §6§lWeapon§7§lMechanics§7, v§o" + desc.getVersion());
        sender.sendMessage("§7➢  §6Author:§7 DeeCaaD");
        sender.sendMessage("§7➢  §6Contributors:§7 CJCrafter");
        sender.sendMessage("§7➢  §6Command:§7 /weaponmechanics");
        List<String> softDepencies = desc.getSoftDepend();
        if (!softDepencies.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < softDepencies.size(); ++i) {
                // First = SomePlugin
                // Rest = , SomeOtherPlugin
                // Basically just does this SomePlugin, SomeOtherPlugin, AnotherPlugin
                builder.append(i == 0 ? softDepencies.get(i) : ", " + softDepencies.get(i));
            }
            sender.sendMessage("§7➢  §6Supported plugins:§7 " + builder.toString());
        }
        UpdateChecker updateChecker = WeaponMechanics.getUpdateChecker();
        if (updateChecker != null && updateChecker.hasUpdate()) {
            // Message is defined in me.deecaad.weaponmechanics.UpdateChecker
            updateChecker.onUpdateFound(sender, updateChecker.getSpigotResource());
        }
    }
}
