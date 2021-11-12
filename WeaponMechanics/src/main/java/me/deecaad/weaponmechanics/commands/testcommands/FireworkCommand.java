package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.entity.EntityCompatibility;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.EnumUtil;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Player only command!");
            return;
        }

        Player player = (Player) sender;

        // Parse arguments of the command
        byte flightTime = args.length > 0 ? Byte.parseByte(args[0]) : 40;
        FireworkEffect.Type type = args.length > 1 ? FireworkEffect.Type.valueOf(args[1]) : FireworkEffect.Type.BURST;
        Color color = args.length > 2 ? Color.fromRGB(Integer.parseInt(args[2], 16)) : Color.WHITE;
        Color fade = args.length > 3 ? Color.fromRGB(Integer.parseInt(args[3], 16)) : Color.GRAY;
        boolean flicker = args.length <= 4 || Boolean.parseBoolean(args[4]);
        boolean trail = args.length <= 5 || Boolean.parseBoolean(args[5]);

        FireworkEffect effect = FireworkEffect.builder()
                .with(type)
                .withColor(color)
                .withFade(fade)
                .flicker(flicker)
                .trail(trail)
                .build();

        EntityCompatibility compatibility = CompatibilityAPI.getCompatibility().getEntityCompatibility();
        compatibility.spawnFirework(player.getLocation(), Collections.singleton(player), flightTime, effect);
    }

    @Override
    protected List<String> handleCustomTag(String[] args, String tag) {
        switch (tag) {
            case "<flight-time>":
                return Arrays.asList(tag, "10", "40");
            case "<type>":
                return new ArrayList<>(EnumUtil.getOptions(FireworkEffect.Type.class));
            case "<color>":
            case "<fade>":
                return Arrays.asList(tag, "FFFFFF", "FF0000", "00FF00", "0000FF");
            case "<flicker>":
            case "<trail>":
                return Arrays.asList(tag, "true", "false");
            default:
                return Collections.singletonList(tag);
        }
    }
}
