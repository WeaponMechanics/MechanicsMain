package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandPermission(permission = "weaponmechanics.commands.test.hitbox")
public class HitboxCommand extends SubCommand {

    HitboxCommand() {
        super("wm test", "hitbox", "Shows the hitboxes of nearby mobs", INTEGERS);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command is only available for players.");
            return;
        }
        Location location = player.getLocation();
        int ticks = (args.length > 0) ? Integer.parseInt(args[0]) : 200;

        WeaponMechanicsCommand.hitbox(sender, new ArrayList<>(location.getWorld().getNearbyEntities(location, 16, 16, 16)), ticks);
    }
}