package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermission(permission = "weaponmechanics.commands.test.nbt")
public class NBTCommand extends SubCommand {

    public NBTCommand() {
        super("wm test", "nbt", "Shows NBT tags");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Player only command!");
            return;
        }

        WeaponMechanicsCommand.nbt(sender, (Player) sender);
    }
}
