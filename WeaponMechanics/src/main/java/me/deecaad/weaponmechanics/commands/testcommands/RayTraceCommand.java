package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermission(permission = "weaponmechanics.commands.test.raytrace")
public class RayTraceCommand extends SubCommand {

    RayTraceCommand() {
        super("wm test", "raytrace", "Shows the hitbox of current entity and/or block in sight", "<only_hit_position> <size> <distance> <time>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command is only available for players.");
            return;
        }

        boolean onlyHitPosition = args.length > 0 && Boolean.parseBoolean(args[0]);
        double size = args.length > 1 ? Double.parseDouble(args[1]) : 0.1;

        int distance = (args.length > 2) ? Integer.parseInt(args[2]) : 5;
        if (distance < 1) {
            sender.sendMessage(ChatColor.RED + "Distance was less than 1");
            return;
        }
        int time = (args.length > 3) ? Integer.parseInt(args[3]) : 200;

        WeaponMechanicsCommand.ray(player, !onlyHitPosition, size, distance, time);
    }
}
