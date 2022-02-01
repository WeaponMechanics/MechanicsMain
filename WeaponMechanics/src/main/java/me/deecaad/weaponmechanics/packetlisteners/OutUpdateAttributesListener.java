package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.ZoomData;

import java.lang.reflect.Field;

public class OutUpdateAttributesListener extends PacketHandler {

    private static final Field idField;

    static {
        Class<?> attributesPacket = ReflectionUtil.getPacketClass("PacketPlayOutUpdateAttributes");

        idField = ReflectionUtil.getField(attributesPacket, int.class);
    }

    public OutUpdateAttributesListener() {
        super("PacketPlayOutUpdateAttributes");
    }

    @Override
    public void onPacket(Packet packet) {

        int id = (int) packet.getFieldValue(idField);

        // If packet entity id is not player's id
        if (id != packet.getPlayer().getEntityId()) {
            return;
        }

        EntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(packet.getPlayer());

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