package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.IWeaponCompatibility;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.RayTraceResult;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.Collection;

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
        IWeaponCompatibility weaponCompatibility = WeaponCompatibilityAPI.getWeaponCompatibility();

        new BukkitRunnable() {
            int ticker = 0;
            @Override
            public void run() {
                Location location = player.getEyeLocation();
                Vector direction = location.getDirection();
                BlockIterator blocks = new BlockIterator(player.getWorld(), location.toVector(), direction, 0.0, distance);

                while (blocks.hasNext()) {
                    Block block = blocks.next();

                    HitBox blockBox = weaponCompatibility.getHitBox(block);
                    if (blockBox == null) continue;

                    RayTraceResult rayTraceResult = blockBox.rayTrace(location.toVector(), direction);
                    if (rayTraceResult == null) continue;

                    if (onlyHitPosition) {
                        rayTraceResult.outlineOnlyHitPosition(player);
                    } else {
                        blockBox.outlineAllBoxes(player);
                    }
                    player.sendMessage("Block: " + block.getType());
                    break;
                }

                Collection<Entity> entities = player.getWorld().getNearbyEntities(location, distance, distance, distance);
                if (!entities.isEmpty()) {
                    for (Entity entity : entities) {
                        if (!(entity instanceof LivingEntity) || entity.getEntityId() == player.getEntityId()) continue;

                        HitBox entityBox = weaponCompatibility.getHitBox(entity);
                        if (entityBox == null) continue;

                        RayTraceResult rayTraceResult = entityBox.rayTrace(location.toVector(), direction);
                        if (rayTraceResult == null) continue;

                        if (onlyHitPosition) {
                            rayTraceResult.outlineOnlyHitPosition(player);
                        } else {
                            entityBox.outlineAllBoxes(player);
                        }
                        player.sendMessage("Entity: " + entity.getType());

                        break;
                    }
                }

                if (++ticker >= time) {
                    cancel();
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, 0);

    }
}
