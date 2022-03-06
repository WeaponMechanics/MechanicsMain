package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

        ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
        FireworkEffect effect = FireworkEffect.builder()
                .with(type)
                .withColor(color)
                .withFade(fade)
                .flicker(flicker)
                .trail(trail)
                .build();
        meta.addEffect(effect);
        itemStack.setItemMeta(meta);

        Random random = new Random();

        FakeEntity fakeEntity = CompatibilityAPI.getCompatibility().getEntityCompatibility().generateFakeEntity(player.getLocation(), EntityType.FIREWORK, itemStack);
        fakeEntity.setMotion(random.nextGaussian() * 0.001, 0.3, random.nextGaussian() * 0.001);
        fakeEntity.show();
        if (flightTime == 0) {
            fakeEntity.playEffect(EntityEffect.FIREWORK_EXPLODE);
            fakeEntity.remove();
            return;
        }
        new BukkitRunnable() {
            public void run() {
                fakeEntity.playEffect(EntityEffect.FIREWORK_EXPLODE);
                fakeEntity.remove();
            }
        }.runTaskLater(WeaponMechanics.getPlugin(), flightTime);
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
