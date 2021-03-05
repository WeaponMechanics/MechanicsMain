package me.deecaad.core.events.triggers;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.events.ArmorEquipEvent;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class ArmorEquipListener extends PacketHandler {

    private static final ArmorEquipEvent.ArmorSlot[] SLOTS = new ArmorEquipEvent.ArmorSlot[] {
            ArmorEquipEvent.ArmorSlot.HEAD,
            ArmorEquipEvent.ArmorSlot.CHEST,
            ArmorEquipEvent.ArmorSlot.LEGS,
            ArmorEquipEvent.ArmorSlot.FEET
    };

    public ArmorEquipListener() {
        super("PacketPlayOutSetSlot");
    }

    @Override
    public void onPacket(Packet wrapper) {

        int slotNum = (int) wrapper.getFieldValue("b") - 5; // Slot 5 is helmet, 6 chestplate, and so on
        if (slotNum < 0 || slotNum >= SLOTS.length) {
            return;
        }

        ArmorEquipEvent.ArmorSlot slot = SLOTS[slotNum];

        // Since we are on a Netty thread at this point, we have to get back onto
        // the "main" thread so we can call the event
        Bukkit.getScheduler().runTask(MechanicsCore.getPlugin(), () -> {

            ItemStack item = wrapper.getPlayer().getEquipment().getArmorContents()[3 - slotNum];
            ArmorEquipEvent event = new ArmorEquipEvent(wrapper.getPlayer(), slot, item);
            Bukkit.getPluginManager().callEvent(event);
        });

    }
}
