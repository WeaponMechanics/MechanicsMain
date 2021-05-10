package me.deecaad.core.events.triggers;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class EquipListener0 extends PacketHandler {

    /**
     * See {@link net.minecraft.server.v1_16_R3.PacketPlayOutSetSlot}
     */
    public EquipListener0() {
        super("PacketPlayOutSetSlot");
    }

    @Override
    public void onPacket(Packet wrapper) {
        Player player = wrapper.getPlayer();
        int a = (int) wrapper.getFieldValue("a");
        int slotNum = (int) wrapper.getFieldValue("b");

        EquipmentSlot slot = getEquipmentSlot(player, slotNum);
        if (slot == null)
            return;

        ItemStack old = player.getEquipment().getItem(slot);

        new BukkitRunnable() {
            @Override
            public void run() {
                System.out.println("PacketPlayOutSetSlot: ");
                System.out.println("\tWindow: " + a);
                System.out.println("\tSlot: " + slot);
                System.out.println("\tOld: " + old);
                System.out.println("\tNew: " + player.getEquipment().getItem(slot));
            }
        }.runTaskLater(MechanicsCore.getPlugin(), 0);
    }

    private EquipmentSlot getEquipmentSlot(Player player, int slot) {
        if (slot == 36 + player.getInventory().getHeldItemSlot())
            return EquipmentSlot.HAND;
        else {
            switch (slot) {
                case 5:
                    return EquipmentSlot.HEAD;
                case 6:
                    return EquipmentSlot.CHEST;
                case 7:
                    return EquipmentSlot.LEGS;
                case 8:
                    return EquipmentSlot.FEET;
                case 45:
                    return EquipmentSlot.OFF_HAND;
                default:
                    return null;
            }
        }
    }
}
