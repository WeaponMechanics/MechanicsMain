package me.deecaad.weaponmechanics.commands.testcommands;

import com.cjcrafter.scheduler.TaskImplementation;
import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.Recoil;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@CommandPermission(permission = "weaponmechanics.commands.test.recoil")
public class RecoilCommand extends SubCommand {

    public RecoilCommand() {
        super("wm test", "recoil", "Tries recoil with given values", "<push-time> <recover-time> <yaws> <pitches> <fire-rate> <shoot-time>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        long rotationTime = Long.parseLong(args[0]);
        long recoverTime = Long.parseLong(args[1]);

        List<Float> yaws = new ArrayList<>();
        for (String yaw : args[2].split(",")) {
            yaws.add(Float.parseFloat(yaw));
        }

        List<Float> pitches = new ArrayList<>();
        for (String pitch : args[3].split(",")) {
            pitches.add(Float.parseFloat(pitch));
        }

        int fireRate = Integer.parseInt(args[4]);
        if (fireRate < 1 || fireRate > 20) {
            sender.sendMessage(ChatColor.RED + "Only fire rates between 1-20 are allowed for testing.");
            return;
        }

        int shootTime = Integer.parseInt(args[5]);

        Recoil recoil = new Recoil(rotationTime, recoverTime, yaws, pitches, null, null);
        PlayerWrapper playerWrapper = WeaponMechanics.getPlayerWrapper((Player) sender);
        WeaponMechanics.getInstance().getFoliaScheduler().entity(playerWrapper.getPlayer()).runAtFixedRate(new Consumer<>() {
            int ticks = 0;
            @Override
            public void accept(TaskImplementation task) {
                if (playerWrapper.isRightClicking()) {
                    recoil.start((Player) sender, true);
                }

                ticks += fireRate;
                if (ticks > shootTime) {
                    task.cancel();
                }
            }
        }, 0, fireRate);
    }
}