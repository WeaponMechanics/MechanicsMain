package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.ZoomData;

public class OutUpdateAttributesListener extends PacketHandler {

    public OutUpdateAttributesListener() {
        super("PacketPlayOutUpdateAttributes");
    }

    @Override
    public void onPacket(Packet packet) {

        int id = (int) packet.getFieldValue("a");

        if (id > 0) {
            // Was not sent my ScopeCompatibility
            return;
        }

        id *= -1;
        packet.setFieldValue("a", id, 0);

        // If packet entity id is not player's id
        if ((int) packet.getFieldValue("a") != packet.getPlayer().getEntityId()) {
            return;
        }

        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(packet.getPlayer());

        ZoomData main = entityWrapper.getMainHandData().getZoomData();
        ZoomData off = entityWrapper.getOffHandData().getZoomData();

        // Not zooming
        if (!main.isZooming() && !off.isZooming()) return;

        int zoomAmount = main.isZooming() ? main.getZoomAmount() : off.getZoomAmount();

        // If zoom amount is 13-32, its only for abilities (not for this packet)
        if (zoomAmount > 12) return;

        WeaponCompatibilityAPI.getScopeCompatibility().modifyUpdateAttributesPacket(packet, zoomAmount);
    }
}