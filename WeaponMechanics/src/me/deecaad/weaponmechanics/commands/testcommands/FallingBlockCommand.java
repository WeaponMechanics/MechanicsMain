package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.ICompatibility;
import me.deecaad.compatibility.entity.EntityCompatibility;
import me.deecaad.compatibility.entity.FallingBlockWrapper;
import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.deecaad.compatibility.entity.EntityCompatibility.EntityMeta.FIRE;
import static me.deecaad.compatibility.entity.EntityCompatibility.EntityMeta.GLOWING;

@CommandPermission(permission = "weaponmechanics.commands.test.fallingblock")
public class FallingBlockCommand extends SubCommand {

    public FallingBlockCommand() {
        super("wm test", "fallingblock", "Spawns a falling block", "<material> <x> <y> <z> <remove>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Player only command!");
            return;
        }

        Player player = (Player) sender;

        Material material = args.length > 0 ? Material.valueOf(args[0]) : Material.STONE;
        double x = args.length > 1 ? Double.parseDouble(args[1]) : 0.1;
        double y = args.length > 2 ? Double.parseDouble(args[2]) : 1.0;
        double z = args.length > 3 ? Double.parseDouble(args[3]) : 0.1;
        int remove = args.length > 4 ? Integer.parseInt(args[4]) : 200;
        final Vector motion = new Vector(x, y, z);

        ICompatibility compatibility = CompatibilityAPI.getCompatibility();
        EntityCompatibility entityCompatibility = compatibility.getEntityCompatibility();

        FallingBlockWrapper blockWrapper = entityCompatibility.createFallingBlock(player.getLocation(), material, (byte) -1, motion);
        Object block = blockWrapper.getEntity();
        Object spawnPacket = entityCompatibility.getSpawnPacket(block);
        Object metaPacket = entityCompatibility.getMetadataPacket(block, true, GLOWING, FIRE);
        Object velocityPacket = entityCompatibility.getVelocityPacket(block, motion);
        Object destroyPacket = entityCompatibility.getDestroyPacket(block);

        compatibility.sendPackets(player, spawnPacket, metaPacket, velocityPacket);
        new BukkitRunnable() {
            @Override
            public void run() {
                compatibility.sendPackets(player, destroyPacket);
            }
        }.runTaskLaterAsynchronously(WeaponMechanics.getPlugin(), Math.min(remove, blockWrapper.getTimeToHitGround()));
    }

    @Override
    protected List<String> handleCustomTag(String[] args, String tag) {
        switch (tag) {
            case "<material>":
                return new ArrayList<>(EnumUtil.getOptions(Material.class));
            case "<x>":
            case "<y>":
            case "<z>":
                return Arrays.asList(tag, "0.0", "2.0", "5.0");
            case "<remove>":
                return Arrays.asList(tag, "100", "200");
            default:
                return super.handleCustomTag(args, tag);
        }
    }
}
