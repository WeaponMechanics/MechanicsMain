package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.scope.ScopeLevel;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.ZoomData;

import java.lang.reflect.Field;

public class OutAbilitiesListener extends PacketHandler {

    private static final Field walkSpeedField;

    static {
        Class<?> abilitiesPacket = ReflectionUtil.getPacketClass("PacketPlayOutAbilities");

        walkSpeedField = ReflectionUtil.getField(abilitiesPacket, float.class, 1);
    }

    public OutAbilitiesListener() {
        super("PacketPlayOutAbilities");
    }

    @Override
    public void onPacket(Packet packet) {
        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(packet.getPlayer());

        ZoomData main = entityWrapper.getMainHandData().getZoomData();
        ZoomData off = entityWrapper.getOffHandData().getZoomData();

        // Not zooming
        if (!main.isZooming() && !off.isZooming()) return;

        int zoomAmount = main.isZooming() ? main.getZoomAmount() : off.getZoomAmount();

        // If zoom amount is 1-12, its only for attributes (not for this packet)
        if (zoomAmount < 13) return;

        // Set the f field to scope level amount.
        // f field means walk speed field
        packet.setFieldValue(walkSpeedField, ScopeLevel.getScope(zoomAmount));
    }
}