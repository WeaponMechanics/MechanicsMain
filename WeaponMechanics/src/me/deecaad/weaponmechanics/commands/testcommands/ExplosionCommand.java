package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import me.deecaad.weaponmechanics.weapon.explode.types.CuboidExplosion;
import me.deecaad.weaponmechanics.weapon.explode.types.DefaultExplosion;
import me.deecaad.weaponmechanics.weapon.explode.types.ParabolicExplosion;
import me.deecaad.weaponmechanics.weapon.explode.types.SphericalExplosion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Useful commands for testing different explosion
 * sizes and shapes. This is a player only command,
 * and <i>should</i> only be registered by the
 * <code>TestCommand</code>.
 *
 * @see TestCommand
 */
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
    
    /**
     * Basic method that saves the <code>Material</code> of
     * all blocks, then sets the blocks' type to <code>Material.AIR</code>.
     * Roughly 10 seconds later, the blocks should regenerate.
     *
     * Since a random number (From 0-1 second, respectively) is
     * added the time to run the task, the blocks will regenerate
     * in a "staggered" pattern.
     *
     * @param blocks The blocks to destroy then regenerate
     */
    public void explode(Set<Block> blocks) {
        for (Block block : blocks) {
            Material type = block.getType();
            block.setType(Material.AIR);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    block.setType(type);
                }
            }.runTaskLater(WeaponMechanics.getPlugin(), (int) (200 + ThreadLocalRandom.current().nextDouble(20)));
        }
    }
    
    private class SphereExplosionCommand extends SubCommand {
        
        SphereExplosionCommand() {
            super("wm test explosion", "sphere", "Spherical explosion", "<3,5,16,32>");
        }
        
        @Override
        public void execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;
            
            double radius = args.length > 0 ? Double.parseDouble(args[0]) : 5.0;
            player.sendMessage("§6Causing a §7sphere§6 shaped explosion with a radius of §7" + radius);
            
            SphericalExplosion explosion = new SphericalExplosion(radius);
            explode(explosion.getBlocks(player.getLocation()));
        }
    }
    
    private class CubeExplosionCommand extends SubCommand {
        
        CubeExplosionCommand() {
            super("wm test explosion", "cube", "Cubical Explosion Test", INTEGERS + " " + INTEGERS);
        }
    
        @Override
        public void execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;
            
            int width  = (int)(args.length > 0 ? Double.parseDouble(args[0]) : 5.0);
            int height = (int)(args.length > 1 ? Double.parseDouble(args[1]) : 5.0);
            player.sendMessage("§6Causing a §7cube§6 shaped explosion with a width of §7" + width + "§6 and a height of §7" + height);
            
            CuboidExplosion explosion = new CuboidExplosion(width, height);
            explode(explosion.getBlocks(player.getLocation()));
        }
    }
    
    private class ParabolaExplosionCommand extends SubCommand {
        
        ParabolaExplosionCommand() {
            super("wm test explosion", "parabola", "Parabolic Explosion Test", "<0.1,0.25,0.5,1,2> <-1,-5,-10,-25>");
        }
    
        @Override
        public void execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;
            
            double angle = args.length > 0 ? Double.parseDouble(args[0]) : 0.5;
            double depth = args.length > 1 ? Double.parseDouble(args[1]) : -3.0;
            player.sendMessage("§6Causing a §7parabola§6 shaped explosion with an angle of §7" + angle + "§6 and a depth of §7" + depth);
    
            ParabolicExplosion explosion = new ParabolicExplosion(depth, angle);
            explode(explosion.getBlocks(player.getLocation()));
        }
    }

    private class DefaultExplosionCommand extends SubCommand {

        DefaultExplosionCommand() {
            super("wm test explosion", "default", "Parabolic Explosion Test", "<3,5,10>");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;

            double yield = args.length > 0 ? Double.parseDouble(args[0]) : 5;
            player.sendMessage("§6Causing a §7Minecraft§6 shaped explosion with an yield of §7" + yield);

            Explosion explosion = new DefaultExplosion(yield);
            explode(explosion.getBlocks(player.getLocation()));
        }
    }
}