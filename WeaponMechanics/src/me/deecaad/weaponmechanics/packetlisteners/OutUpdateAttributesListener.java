package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;

public class OutUpdateAttributesListener extends PacketHandler {

    public OutUpdateAttributesListener() {
        super("PacketPlayOutUpdateAttributes");
    }

    @Override
    public void onPacket(Packet packet) {

        // If packet entity id is not player's id
        if ((int) packet.getFieldValue("a") != packet.getPlayer().getEntityId()) {
            return;
        }

        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(packet.getPlayer());

        // Not zooming
        if (!entityWrapper.isZooming()) return;

        int zoomAmount = entityWrapper.getZoomData().getZoomAmount();

        // If zoom amount is 13-32, its only for abilities (not for this packet)
        if (zoomAmount > 12) return;

        CompatibilityAPI.getCompatibility().getScopeCompatibility().modifyUpdateAttributesPacket(packet, zoomAmount);
    }
}