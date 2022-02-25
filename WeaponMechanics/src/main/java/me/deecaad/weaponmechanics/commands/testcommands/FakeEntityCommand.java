package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Player only command!");
            return;
        }

        Player player = (Player) sender;

        // Parse arguments of the command
        EntityType type = args.length > 0 ? EntityType.valueOf(args[0]) : EntityType.ZOMBIE;
        String moveType = args.length > 1 ? args[1] : "none";
        int time =        args.length > 2 ? Integer.parseInt(args[2]) : 1200;
        boolean gravity = args.length > 3 ? Boolean.parseBoolean(args[3]) : false;
        String name     = args.length > 4 ? StringUtil.color(args[4]) : null;

        FakeEntity entity = CompatibilityAPI.getEntityCompatibility().generateFakeEntity(player.getLocation(), type, type == EntityType.DROPPED_ITEM ? new ItemStack(Material.STONE_AXE) : null);
        entity.setGravity(gravity);
        entity.setDisplay(name);
        entity.show(player);
        entity.setMotion(0, 0, 0);

        new BukkitRunnable() {

            // Some temp vars for the different move types
            int ticksAlive = 0;
            boolean flash = true;

            @Override
            public void run() {
                if (ticksAlive++ >= time) {
                    entity.remove();
                    cancel();
                    return;
                }

                switch (moveType) {
                    case "spin":
                        entity.setRotation(entity.getYaw() + 5.0f, entity.getYaw() / 2.0f);
                        break;
                    case "flash":
                        if (ticksAlive % 10 == 0) {
                            flash = !flash;
                            entity.setGlowing(flash);
                            entity.updateMeta();
                        }
                        break;
                    case "sky":
                        //entity.setMotion(0, 0.08, 0);
                        entity.setPosition(entity.getX(), entity.getY() + 0.1, entity.getZ());
                        break;
                    case "x":
                        //entity.setMotion(0.08, 0, 0);
                        entity.setPosition(entity.getX() + 0.1, entity.getY(), entity.getZ());
                        break;
                }
            }
        }.runTaskTimerAsynchronously(WeaponMechanics.getPlugin(), 0, 0);
    }

    @Override
    protected List<String> handleCustomTag(String[] args, String tag) {
        switch (tag) {
            case "<type>":
                return new ArrayList<>(EnumUtil.getOptions(EntityType.class));
            case "<move>":
                return Arrays.asList(tag, "none", "spin", "flash", "sky", "x");
            case "<time>":
                return Arrays.asList(tag, "100", "200", "400", "1600");
            case "<gravity>":
                return Arrays.asList(tag, "true", "false");
            default:
                return Collections.singletonList(tag);
        }
    }
}
