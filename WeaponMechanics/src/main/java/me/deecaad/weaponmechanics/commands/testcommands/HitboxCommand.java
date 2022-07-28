package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsCommand;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import org.bukkit.ChatColor;
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

import java.util.ArrayList;
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
            sender.sendMessage(ChatColor.RED + "This command is only available for players.");
            return;
        }
        Player player = (Player) sender;
        Location location = player.getLocation();
        int ticks = (args.length > 0) ? Integer.parseInt(args[0]) : 200;

        WeaponMechanicsCommand.hitbox(sender, new ArrayList<>(location.getWorld().getNearbyEntities(location, 16, 16, 16)), ticks);
    }
}