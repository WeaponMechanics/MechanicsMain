package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.effects.Effect;
import me.deecaad.core.effects.shapes.*;
import me.deecaad.core.effects.types.DnaParticleEffect;
import me.deecaad.core.effects.types.ShapedParticleEffect;
import me.deecaad.core.utils.VectorUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;

@CommandPermission(permission = "weaponmechanics.commands.test.shape")
public class ShapeCommand extends SubCommand {

    public ShapeCommand() {
        super("wm test", "shape", "Draws a given shape for 20 seconds", SUB_COMMANDS);

        commands.register(new SphereCommand());
        commands.register(new DnaCommand());
        commands.register(new LightningCommand());
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

    private void draw(Location loc, Shape shape) {
        World world = loc.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null");
        }

        Effect effect = new ShapedParticleEffect(Particle.FLAME, 1, 0, 0, 0, null, shape, 0);
        draw(loc, effect);
    }

    private void draw(Location loc, Effect effect) {
        for (int i = 0; i < 8; i++) {
            Bukkit.getScheduler().runTaskLater(WeaponMechanics.getPlugin(), () -> effect.spawn(WeaponMechanics.getPlugin(), loc), i * 30);
        }
    }

    @CommandPermission(permission = "weaponmechanics.commands.test.shape.sphere")
    private class SphereCommand extends SubCommand {

        SphereCommand() {
            super("wm test shape", "sphere", "Draw a sphere", INTEGERS + " " + INTEGERS);
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            Location loc = ((Player) sender).getLocation();
            int radius = args.length > 0 ? Integer.parseInt(args[0]) : 2;
            int points = args.length > 1 ? Integer.parseInt(args[1]) : 512;
            draw(loc, new Sphere(radius, points));
        }
    }

    @CommandPermission(permission = "weaponmechanics.commands.test.shape.dna")
    private class DnaCommand extends SubCommand {

        DnaCommand() { // /wm test shape dna <length> <points> <amplitude> <rungs> <loops>
            super("wm test shape", "dna", "Draw a DNA double helix", "<length> <points> <radius> <rungs> <loops>");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;

            double length = args.length > 0 ? Double.parseDouble(args[0]) : 10;
            int points = args.length > 1 ? Integer.parseInt(args[1]) : 32;
            double amplitude = args.length > 2 ? Double.parseDouble(args[2]) : 2;
            int rungs = args.length > 3 ? Integer.parseInt(args[3]) : 10;
            int loops = args.length > 4 ? Integer.parseInt(args[4]) : 1;

            Vector vector = VectorUtils.setLength(player.getLocation().getDirection(), length);

            Circle circle = new Circle(points, amplitude);
            Spiral spiral = new Spiral(circle, vector, loops);
            Effect effect = new DnaParticleEffect(spiral, rungs, Particle.FLAME);
            draw(player.getLocation(), effect);
        }
    }

    @CommandPermission(permission = "weaponmechanics.commands.test.shape.lightning")
    private class LightningCommand extends SubCommand {

        LightningCommand() {
            super("wm test shape", "lightning", "Draw a bolt of lightning", "<length> <mindepth> <maxdepth> <minpoints> <maxpoints> <branches>");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {

            double length = args.length > 0 ? Double.parseDouble(args[0]) : 10;
            int minDepth = args.length > 1 ? Integer.parseInt(args[1]) : 3;
            int maxDepth = args.length > 2 ? Integer.parseInt(args[2]) : 5;
            int minPoints = args.length > 3 ? Integer.parseInt(args[3]) : 10;
            int maxPoints = args.length > 4 ? Integer.parseInt(args[4]) : 10;
            int branches = args.length > 5 ? Integer.parseInt(args[5]) : 3;

            Lightning shape = new Lightning();
            shape.setAxis(VectorUtils.setLength(((Player) sender).getLocation().getDirection(), length));
            Effect effect = new ShapedParticleEffect(Particle.FLAME, 1, 0, 0, 0, null, shape, 0);
            draw(((Player) sender).getLocation(), effect);
        }
    }
}
