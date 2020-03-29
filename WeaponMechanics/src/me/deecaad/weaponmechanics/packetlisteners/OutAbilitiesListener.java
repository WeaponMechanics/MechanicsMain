package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.scope.ScopeLevel;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;

public class OutAbilitiesListener extends PacketHandler {

    public OutAbilitiesListener() {
        super("PacketPlayOutAbilities");
    }

    @Override
    public void onPacket(Packet packet) {
        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(packet.getPlayer());

        // Not zooming
        if (!entityWrapper.isZooming()) return;

        int zoomAmount = entityWrapper.getZoomData().getZoomAmount();

        // If zoom amount is 1-12, its only for attributes (not for this packet)
        if (zoomAmount < 13) return;

        // Set the f field to scope level amount.
        // f field means walk speed field
        packet.setFieldValue("f", ScopeLevel.getScope(zoomAmount), 0);
    }
}