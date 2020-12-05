package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamage;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import me.deecaad.weaponmechanics.weapon.explode.ExplosionShape;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DefaultExposure;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.RegenerationData;
import me.deecaad.weaponmechanics.weapon.explode.shapes.CuboidExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.DefaultExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ParabolicExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.SphericalExplosion;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Useful commands for testing different explosion
 * sizes and shapes. This is a player only command,
 * and <i>should</i> only be registered by the
 * <code>TestCommand</code>.
 *
 * @see TestCommand
 */
@CommandPermission(permission = "weaponmechanics.commands.test.explosion")
public class ExplosionCommand extends SubCommand {

    public ExplosionCommand() {
        super("wm test", "explosion", "Explode functions for devs", SUB_COMMANDS);

        commands.register(new SphereExplosionCommand());
        commands.register(new CubeExplosionCommand());
        commands.register(new ParabolaExplosionCommand());
        commands.register(new DefaultExplosionCommand());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        if (args.length > 0) {
            commands.execute(args[0], sender, Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        sendHelp(sender, args);
    }

    private void explode(ExplosionShape shape, Player player, Location loc) {

        new BukkitRunnable() {
            @Override
            public void run() {
                RegenerationData regeneration = new RegenerationData(160, 2, 1);
                BlockDamage blockDamage = new BlockDamage(true, 1, 1, true, new HashMap<>(), new HashMap<>());
                new Explosion(null, shape, new DefaultExposure(), blockDamage, regeneration, null, 0, 0.50, true).explode(player, loc, null);
            }
        }.runTaskLater(WeaponMechanics.getPlugin(), 100);
    }

    @CommandPermission(permission = "weaponmechanics.commands.test.explosion.sphere")
    private class SphereExplosionCommand extends SubCommand {

        SphereExplosionCommand() {
            super("wm test explosion", "sphere", "Spherical explosion", "<3,5,16,32>");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;

            double radius = args.length > 0 ? Double.parseDouble(args[0]) : 5.0;
            player.sendMessage("§6Causing a §7sphere§6 shaped explosion with a radius of §7" + radius);

            explode(new SphericalExplosion(radius), player, player.getLocation());
        }
    }

    @CommandPermission(permission = "weaponmechanics.commands.test.explosion.cube")
    private class CubeExplosionCommand extends SubCommand {

        CubeExplosionCommand() {
            super("wm test explosion", "cube", "Cubical Explosion Test", INTEGERS + " " + INTEGERS);
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;

            int width = (int) (args.length > 0 ? Double.parseDouble(args[0]) : 5.0);
            int height = (int) (args.length > 1 ? Double.parseDouble(args[1]) : 5.0);
            player.sendMessage("§6Causing a §7cube§6 shaped explosion with a width of §7" + width + "§6 and a height of §7" + height);

            explode(new CuboidExplosion(width, height), player, player.getLocation());
        }
    }

    @CommandPermission(permission = "weaponmechanics.commands.test.explosion.parabola")
    private class ParabolaExplosionCommand extends SubCommand {

        ParabolaExplosionCommand() {
            super("wm test explosion", "parabola", "Parabolic Explosion Test", "<angle> <depth>");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;

            double angle = args.length > 0 ? Double.parseDouble(args[0]) : 0.5;
            double depth = args.length > 1 ? Double.parseDouble(args[1]) : -3.0;
            player.sendMessage("§6Causing a §7parabola§6 shaped explosion with an angle of §7" + angle + "§6 and a depth of §7" + depth);

            explode(new ParabolicExplosion(depth, angle), player, player.getLocation());
        }
    }

    @CommandPermission(permission = "weaponmechanics.commands.test.explosion.default")
    private class DefaultExplosionCommand extends SubCommand {

        DefaultExplosionCommand() {
            super("wm test explosion", "default", "Parabolic Explosion Test", "<3,5,10>");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;

            double yield = args.length > 0 ? Double.parseDouble(args[0]) : 5;
            player.sendMessage("§6Causing a §7Minecraft§6 shaped explosion with an yield of §7" + yield);

            explode(new DefaultExplosion(yield), player, player.getLocation());
        }
    }
}