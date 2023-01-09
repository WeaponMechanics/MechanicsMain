package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsCommand;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FireworkCommand extends SubCommand {

    public FireworkCommand() {
        super("wm test", "firework", "Spawns a packet firework", "<flight-time> <type> <color> <fade> <flicker> <trail>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Player only command!");
            return;
        }

        // Parse arguments of the command
        int flightTime = args.length > 0 ? Integer.parseInt(args[0]) : 40;
        FireworkEffect.Type type = args.length > 1 ? FireworkEffect.Type.valueOf(args[1]) : FireworkEffect.Type.BURST;
        Color color = args.length > 2 ? Color.fromRGB(Integer.parseInt(args[2], 16)) : Color.WHITE;
        Color fade = args.length > 3 ? Color.fromRGB(Integer.parseInt(args[3], 16)) : Color.GRAY;
        boolean flicker = args.length <= 4 || Boolean.parseBoolean(args[4]);
        boolean trail = args.length <= 5 || Boolean.parseBoolean(args[5]);

        WeaponMechanicsCommand.firework(player.getLocation(), flightTime, type, color, fade, flicker, trail);
    }

    @Override
    protected List<String> handleCustomTag(String[] args, String tag) {
        return switch (tag) {
            case "<flight-time>" -> Arrays.asList(tag, "10", "40");
            case "<type>" -> new ArrayList<>(EnumUtil.getOptions(FireworkEffect.Type.class));
            case "<color>", "<fade>" -> Arrays.asList(tag, "FFFFFF", "FF0000", "00FF00", "0000FF");
            case "<flicker>", "<trail>" -> Arrays.asList(tag, "true", "false");
            default -> Collections.singletonList(tag);
        };
    }
}
