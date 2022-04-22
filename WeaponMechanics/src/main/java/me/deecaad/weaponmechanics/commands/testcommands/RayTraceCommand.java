package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.RayTrace;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@CommandPermission(permission = "weaponmechanics.commands.test.raytrace")
public class RayTraceCommand extends SubCommand {

    RayTraceCommand() {
        super("wm test", "raytrace", "Shows the hitbox of current entity and/or block in sight", "<only_hit_position> <distance> <time>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only available for players.");
            return;
        }

        Player player = (Player) sender;

        boolean onlyHitPosition = args.length > 0 && Boolean.parseBoolean(args[0]);
        int distance = (args.length > 1) ? Integer.parseInt(args[1]) : 5;
        if (distance < 1) {
            sender.sendMessage(ChatColor.RED + "Distance was less than 1");
            return;
        }
        int time = (args.length > 2) ? Integer.parseInt(args[2]) : 200;

        sender.sendMessage(ChatColor.GREEN + "Showing hitboxes in distance " + distance + " for " + time + " ticks");

        RayTrace rayTrace = new RayTrace().withEntityFilter(entity -> entity.getEntityId() == player.getEntityId());
        if (onlyHitPosition) {
            rayTrace.withOutlineHitPosition(player);
        } else {
            rayTrace.withOutlineHitBox(player);
        }

        new BukkitRunnable() {
            int ticker = 0;
            @Override
            public void run() {
                Location location = player.getEyeLocation();
                Vector direction = location.getDirection();

                rayTrace.cast(player.getWorld(), location.toVector(), direction, distance);

                if (++ticker >= time) {
                    cancel();
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, 0);

    }
}
