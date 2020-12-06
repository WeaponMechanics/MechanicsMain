package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
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

        if (!entityWrapper.getMainHandData().getZoomData().hasZoomNightVision() && !entityWrapper.getOffHandData().getZoomData().hasZoomNightVision()) return;
        if (!WeaponCompatibilityAPI.getScopeCompatibility().isRemoveNightVisionPacket(packet)) return;

        packet.setCancelled(true);
    }
}
