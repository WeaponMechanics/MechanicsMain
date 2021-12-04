package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;

public class OutEntityEffectListener extends PacketHandler {

    private static final Field idField;
    private static final Field dataField;

    static {
        Class<?> effectPacket = ReflectionUtil.getPacketClass("PacketPlayOutEntityEffect");

        // Pre 1.17, the integer is always stored in the "a" field. In post
        // versions, there are primitive constants, so I am worried about
        // using index to get the id field. I'll, for now, assume "d" will
        // work in newer versions.
        if (CompatibilityAPI.getVersion() < 1.17)
            idField = ReflectionUtil.getField(effectPacket, "a");
        else
            idField = ReflectionUtil.getField(effectPacket, "d");

        dataField = ReflectionUtil.getField(effectPacket, byte.class);
    }

    public OutEntityEffectListener() {
        super("PacketPlayOutEntityEffect");
    }

    @Override
    public void onPacket(Packet packet) {
        int id = (int) packet.getFieldValue(idField);
        if (id < 0) { // Means that it was sent by ScopeCompatibility

            // Simply convert id back to normal and let packet pass
            // By pass I mean, not cancel it, that return statement is there for reason (to cancel night vision remove when it should be on)
            packet.setFieldValue(idField, -id);
            return;
        }

        // If packet entity id is not player's id
        if (id != packet.getPlayer().getEntityId()) {
            return;
        }

        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(packet.getPlayer());

        if (!entityWrapper.getMainHandData().getZoomData().hasZoomNightVision() && !entityWrapper.getOffHandData().getZoomData().hasZoomNightVision()) return;
        if ((byte) packet.getFieldValue(dataField) != (byte) (PotionEffectType.NIGHT_VISION.getId() & 255)) return;

        packet.setCancelled(true);
    }
}
