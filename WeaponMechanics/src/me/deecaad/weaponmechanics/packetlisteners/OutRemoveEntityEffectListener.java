package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;

public class OutRemoveEntityEffectListener extends PacketHandler {

    public OutRemoveEntityEffectListener() {
        super("PacketPlayOutRemoveEntityEffect");
    }

    @Override
    public void onPacket(Packet packet) {
        // If packet entity id is not player's id
        if ((int) packet.getFieldValue("a") != packet.getPlayer().getEntityId()) {
            return;
        }

        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(packet.getPlayer());

        if (!entityWrapper.getZoomData().hasZoomNightVision()) return;
        if (!CompatibilityAPI.getCompatibility().getScopeCompatibility().isRemoveNightVisionPacket(packet)) return;

        packet.setCancelled(true);
    }
}
