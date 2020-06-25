package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.potion.PotionEffectType;

public class OutEntityEffectListener extends PacketHandler {

    public OutEntityEffectListener() {
        super("PacketPlayOutEntityEffect");
    }

    @Override
    public void onPacket(Packet packet) {
        int id = (int) packet.getFieldValue("a");
        if (id < 0) { // Means that it was sent by ScopeCompatibility

            // Simply convert id back to normal and let packet pass
            packet.setFieldValue("a", (id * -1), 0);
            return;
        }

        // If packet entity id is not player's id
        if (id != packet.getPlayer().getEntityId()) {
            return;
        }

        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(packet.getPlayer());

        if (!entityWrapper.getMainHandData().getZoomData().hasZoomNightVision() && !entityWrapper.getOffHandData().getZoomData().hasZoomNightVision()) return;
        if ((byte) packet.getFieldValue("b") != (byte) (PotionEffectType.NIGHT_VISION.getId() & 255)) return;

        packet.setCancelled(true);
    }
}
