package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.Recoil;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class RecoilCommand extends SubCommand {

    public RecoilCommand() {
        super("wm test", "recoil", "Tries recoil with given values", "<rotation-time> <recover-time> <yaws> <pitches> <fire-rate> <shoot-time>");
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
            sender.sendMessage("With this test command only fire rates between 1-20 are allowed.");
            return;
        }

        int shootTime = Integer.parseInt(args[5]);

        Recoil recoil = new Recoil(rotationTime, yaws, pitches, recoverTime);
        IPlayerWrapper playerWrapper = WeaponMechanics.getPlayerWrapper((Player) sender);
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {

                if (playerWrapper.isRightClicking()) {
                    recoil.start((Player) sender);
                }

                ticks += fireRate;
                if (ticks > shootTime) {
                    cancel();
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, fireRate);
    }
}