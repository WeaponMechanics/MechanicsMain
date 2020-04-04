package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.weaponmechanics.WeaponMechanics;
import net.minecraft.server.v1_15_R1.PacketPlayOutPosition;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Recoil {

    public void rotateCamera(Player player) {
        Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> teleportFlags = new HashSet<>(Arrays.asList(PacketPlayOutPosition.EnumPlayerTeleportFlags.X,
                PacketPlayOutPosition.EnumPlayerTeleportFlags.Y,
                PacketPlayOutPosition.EnumPlayerTeleportFlags.Z,
                PacketPlayOutPosition.EnumPlayerTeleportFlags.X_ROT,
                PacketPlayOutPosition.EnumPlayerTeleportFlags.Y_ROT));

        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        long millisBetweenRotations = 5;

        long rotationTime = 100;
        float rotateYaw = 15;
        float rotatePitch = 10 * -1;

        int timerRotations = (int) (rotationTime / millisBetweenRotations);
        float timerRotateYawPerIteration = rotateYaw / timerRotations;
        float timerRotatePitchPerIteration = rotatePitch / timerRotations;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            int i = 0;

            @Override
            public void run() {

                if (i == 0) {
                    Bukkit.broadcastMessage("Starting millis rotation...");
                }

                playerConnection.sendPacket(new PacketPlayOutPosition(0, 0, 0, timerRotateYawPerIteration, timerRotatePitchPerIteration, teleportFlags, 0));

                if (++i >= timerRotations) {
                    cancel();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage("Reset rotation...");
                            playerConnection.sendPacket(new PacketPlayOutPosition(0, 0, 0, rotateYaw * -1, rotatePitch * -1, teleportFlags, 0));
                        }
                    }.runTaskLater(WeaponMechanics.getPlugin(), 20);
                }
            }
        }, 0, millisBetweenRotations);



        int rotations = (int) (rotationTime / 50);
        float rotateYawPerIteration = rotateYaw / rotations;
        float rotatePitchPerIteration = rotatePitch / rotations;
        new BukkitRunnable() {

            int i = 0;

            @Override
            public void run() {
                if (i == 0) {
                    Bukkit.broadcastMessage("Starting tick rotation...");
                }

                playerConnection.sendPacket(new PacketPlayOutPosition(0, 0, 0, rotateYawPerIteration, rotatePitchPerIteration, teleportFlags, 0));

                if (++i >= rotations) {
                    cancel();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage("Reset rotation...");
                            playerConnection.sendPacket(new PacketPlayOutPosition(0, 0, 0, rotateYaw * -1, rotatePitch * -1, teleportFlags, 0));
                        }
                    }.runTaskLater(WeaponMechanics.getPlugin(), 20);
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 60, 0);
    }
}