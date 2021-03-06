package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.block.BlockCompatibility;
import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

@CommandPermission(permission = "weaponmechanics.commands.test.mask")
public class MaskCommand extends SubCommand {

    public MaskCommand() {
        super("wm test", "mask", "Masks the blocks around you", "<material> <range> <time>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Player only command!");
            return;
        }

        Player player = (Player) sender;
        Block location = player.getLocation().getBlock();

        Material material = args.length > 0 ? Material.valueOf(args[0]) : Material.STONE;
        int range = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        int time = args.length > 2 ? Integer.parseInt(args[2]) * 20 : 200;

        int minX = location.getX() - range;
        int maxX = location.getX() + range;
        int minY = NumberUtil.minMax(0, location.getY() - range, 255);
        int maxY = NumberUtil.minMax(0, location.getY() + range, 255);
        int minZ = location.getZ() - range;
        int maxZ = location.getZ() + range;

        World world = player.getWorld();
        final Map<Chunk, List<Block>> blockMap = new HashMap<>();
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);

                    if (!block.getType().isSolid()) {
                        continue;
                    } else if (!block.getChunk().isLoaded()) {
                        debug.debug("Tried to mask block outside of loaded chunks");
                        continue;
                    }

                    List<Block> temp = blockMap.computeIfAbsent(block.getChunk(), k -> new ArrayList<>());
                    temp.add(block);
                }
            }
        }

        debug.debug("Sending block mask to " + player.getName());
        BlockCompatibility blockCompatibility = CompatibilityAPI.getCompatibility().getBlockCompatibility();
        for (List<Block> blocks : blockMap.values()) {
            Object packet = blockCompatibility.getMultiBlockMaskPacket(blocks, material, (byte) -1);
            CompatibilityAPI.getCompatibility().sendPackets(player, packet);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (List<Block> blocks : blockMap.values()) {
                    Object packet = blockCompatibility.getMultiBlockMaskPacket(blocks, null);
                    CompatibilityAPI.getCompatibility().sendPackets(player, packet);
                }
            }
        }.runTaskLaterAsynchronously(WeaponMechanics.getPlugin(), time);
    }

    @Override
    protected List<String> handleCustomTag(String[] args, String tag) {
        switch (tag) {
            case "<material>":
                return new ArrayList<>(EnumUtil.getOptions(Material.class));
            case "<range>":
                return Arrays.asList(tag, "5", "10");
            case "<time>":
                return Arrays.asList(tag, "10", "20");
            default:
                return super.handleCustomTag(args, tag);
        }
    }
}
