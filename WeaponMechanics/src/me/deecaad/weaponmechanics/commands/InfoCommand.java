package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.UpdateChecker;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.List;

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
            sender.sendMessage("§7➢  §6Authors:§7 " + convertListToPrettyStringBuilder(authors).toString());
        }

        sender.sendMessage("§7➢  §6Command:§7 /weaponmechanics");
        List<String> softDepencies = desc.getSoftDepend();
        if (!softDepencies.isEmpty()) {
            sender.sendMessage("§7➢  §6Supported plugins:§7 " + convertListToPrettyStringBuilder(softDepencies).toString());
        }
        UpdateChecker updateChecker = WeaponMechanics.getUpdateChecker();
        if (updateChecker != null && updateChecker.hasUpdate()) {
            // Message is defined in me.deecaad.weaponmechanics.UpdateChecker
            updateChecker.onUpdateFound(sender, updateChecker.getSpigotResource());
        }
    }

    /**
     * First = "SomePlugin"
     * Rest = ", SomeOtherPlugin"
     * Basically just does this "SomePlugin, SomeOtherPlugin, AnotherPlugin"
     */
    private StringBuilder convertListToPrettyStringBuilder(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); ++i) {
            builder.append(i == 0 ? list.get(i) : ", " + list.get(i));
        }
        return builder;
    }
}
