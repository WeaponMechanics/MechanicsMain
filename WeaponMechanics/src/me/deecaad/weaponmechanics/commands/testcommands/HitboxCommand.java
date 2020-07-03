package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.Collection;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

@CommandPermission(permission = "weaponmechanics.commands.test.hitbox")
public class HitboxCommand extends SubCommand {

    HitboxCommand() {
        super("wm test", "hitbox", "Shows the hitboxes of nearby mobs", INTEGERS);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only available for players.");
            return;
        }
        Player player = (Player) sender;
        Location location = player.getLocation();
        int ticks = (args.length > 0) ? Integer.parseInt(args[0]) : 200;

        Collection<Entity> entities = location.getWorld().getNearbyEntities(location, 16, 16, 16);
        Configuration basicConfiguration = WeaponMechanics.getBasicConfigurations();
        new BukkitRunnable() {
            int ticksPassed = 0;
            public void run() {

                for (Entity entity : entities) {
                    if (!(entity instanceof LivingEntity)) continue;
                    if (entity.equals(player)) continue;

                    EntityType type = entity.getType();

                    double head = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.HEAD.name(), -1);
                    double body = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.BODY.name(), -1);
                    double legs = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.LEGS.name(), -1);
                    double feet = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.FEET.name(), -1);

                    if (head == -1 || body == -1 || legs == -1 || feet == -1) {
                        debug.log(LogLevel.ERROR, "Entity type " + type.name() + " is missing some of its damage point values, please add it",
                                "Located at file /CrackShotPlus/config.yml in Entity_Hitboxes." + type.name() + " in configurations",
                                "Its missing one of these: HEAD, BODY, LEGS or FEET");
                        continue;
                    }
                    double sumOf = head + body + legs + feet;
                    if (Math.abs(sumOf - 1.0) > 1e-5) { // If the numbers are not super close together (floating point issues)
                        debug.log(LogLevel.ERROR, "Entity type " + type.name() + " hit box values sum doesn't match 1.0",
                                "Located at file /CrackShotPlus/config.yml in Entity_Hitboxes." + type.name() + " in configurations",
                                "Now the total sum was " + sumOf + ", please make it 1.0.");
                        continue;
                    }

                    BoundingBox box = entity.getBoundingBox();
                    double max = box.getMaxY();
                    double height = box.getHeight();

                    double headY = max - (height * head);
                    double bodyY = max - (height * (head + body));
                    double legsY = max - (height * (head + body + legs));
                    double feetY = max - (height * (head + body + legs + feet)); // this could also be just box.getMinY()

                    for (double x = box.getMinX(); x <= box.getMaxX(); x += 0.25) {
                        for (double z = box.getMinZ(); z <= box.getMaxZ(); z += 0.25) {

                            if (head > 0.0) {
                                entity.getWorld().spawnParticle(Particle.REDSTONE, x, headY, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.RED, 1.0f), true);
                            }
                            if (body > 0.0) {
                                entity.getWorld().spawnParticle(Particle.REDSTONE, x, bodyY, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.ORANGE, 1.0f), true);
                            }
                            if (legs > 0.0) {
                                entity.getWorld().spawnParticle(Particle.REDSTONE, x, legsY, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.YELLOW, 1.0f), true);
                            }
                            if (feet > 0.0) {
                                entity.getWorld().spawnParticle(Particle.REDSTONE, x, feetY, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.GREEN, 1.0f), true);
                            }
                        }
                    }
                }

                ticksPassed += 5;
                if (ticksPassed >= ticks) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(WeaponMechanics.getPlugin(), 0, 5);
    }
}