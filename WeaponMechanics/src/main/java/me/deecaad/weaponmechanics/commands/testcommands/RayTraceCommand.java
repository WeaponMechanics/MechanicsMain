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
        super("wm test", "raytrace", "Shows the hitbox of current entity and/or block in sight", "<distance> <time>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only available for players.");
            return;
        }

        Player player = (Player) sender;

        int distance = (args.length > 0) ? Integer.parseInt(args[0]) : 5;
        if (distance < 1) {
            sender.sendMessage(ChatColor.RED + "Distance was less than 1");
            return;
        }
        int time = (args.length > 1) ? Integer.parseInt(args[1]) : 200;

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

                    outline(blockBox, player);
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

                        outline(entityBox, player);
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

    private void outline(HitBox hitBox, Player player) {
        double maxX = hitBox.getMaxX();
        double minX = hitBox.getMinX();

        double maxY = hitBox.getMaxY();
        double minY = hitBox.getMinY();

        double maxZ = hitBox.getMaxZ();
        double minZ = hitBox.getMinZ();

        double step = 0.1;
        for (double x = minX; x <= maxX; x += step) {
            for (double y = minY; y <= maxY; y += step) {
                for (double z = minZ; z <= maxZ; z += step) {
                    int components = 0;
                    if (x == minX || x + step > maxX) components++;
                    if (y == minY || y + step > maxY) components++;
                    if (z == minZ || z + step > maxZ) components++;
                    if (components >= 2) {
                        player.getWorld().spawnParticle(Particle.REDSTONE, x, y, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.RED, 0.5f), true);
                    }
                }
            }
        }
    }
}
