package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;

import java.lang.reflect.Field;

public class OutRemoveEntityEffectListener extends PacketHandler {

    private static final Field idField;

    static {
        Class<?> effectPacket = ReflectionUtil.getPacketClass("PacketPlayOutRemoveEntityEffect");

        idField = ReflectionUtil.getField(effectPacket, int.class);
    }

    public OutRemoveEntityEffectListener() {
        super("PacketPlayOutRemoveEntityEffect");
    }

    @Override
    public void onPacket(Packet packet) {
        // If packet entity id is not player's id
        if ((int) packet.getFieldValue(idField) != packet.getPlayer().getEntityId()) {
            return;
        }

        EntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(packet.getPlayer());

        if (!entityWrapper.getMainHandData().getZoomData().hasZoomNightVision() && !entityWrapper.getOffHandData().getZoomData().hasZoomNightVision()) return;
        if (!WeaponCompatibilityAPI.getScopeCompatibility().isRemoveNightVisionPacket(packet)) return;

        packet.setCancelled(true);
    }
}
