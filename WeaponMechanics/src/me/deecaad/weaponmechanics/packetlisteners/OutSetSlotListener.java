package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;

public class OutSetSlotListener extends PacketHandler {

    public OutSetSlotListener() {
        super("PacketPlayOutSetSlot");
    }

    @Override
    public void onPacket(Packet packet) {
        IPlayerWrapper wrapper = WeaponMechanics.getPlayerWrapper(packet.getPlayer());

        if (!wrapper.isDenyNextSetSlotPacket()) {
            return;
        }

        // https://wiki.vg/Protocol#Set_Slot

        // To set the cursor (the item currently dragged with the mouse), use -1 as Window ID and as Slot.

        // This packet can only be used to edit the hotbar of the player's inventory if window ID is set to 0 (slots 36 through 44).
        // If the window ID is set to -2, then any slot in the inventory can be used but no add item animation will be played.

        int windowId = (int) packet.getFieldValue("a");
        if (windowId == -1) { // Cursor set slot packet
            return;
        }

        int slot = (int) packet.getFieldValue("b");
        if (windowId != 0) { // If not hotbar slot
            if (windowId == -2 && slot != 45) { // And if its not off hand slot either)
                return;
            }
        }

        // Now we can cancel the packet from being sent for player
        packet.setCancelled(true);

        wrapper.setDenyNextSetSlotPacket(false);
    }
}
