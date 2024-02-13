package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FakeEntityCommand extends SubCommand {

    public FakeEntityCommand() {
        super("wm test", "fakeentity", "Spawns a fake entity", "<type> <move> <time> <gravity> <name>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Player only command!");
            return;
        }

        // Parse arguments of the command
        EntityType type = args.length > 0 ? EntityType.valueOf(args[0]) : EntityType.ZOMBIE;
        String moveType = args.length > 1 ? args[1] : "none";
        int time = args.length > 2 ? Integer.parseInt(args[2]) : 1200;
        boolean gravity = args.length > 3 ? Boolean.parseBoolean(args[3]) : false;
        String name = args.length > 4 ? StringUtil.colorBukkit(args[4]) : null;

        WeaponMechanicsCommand.spawn(player, player.getLocation(), type, moveType, time, gravity, name);
    }

    @Override
    protected List<String> handleCustomTag(String[] args, String tag) {
        return switch (tag) {
            case "<type>" -> new ArrayList<>(EnumUtil.getOptions(EntityType.class));
            case "<move>" -> Arrays.asList(tag, "none", "spin", "flash", "sky", "x");
            case "<time>" -> Arrays.asList(tag, "100", "200", "400", "1600");
            case "<gravity>" -> Arrays.asList(tag, "true", "false");
            default -> Collections.singletonList(tag);
        };
    }
}
