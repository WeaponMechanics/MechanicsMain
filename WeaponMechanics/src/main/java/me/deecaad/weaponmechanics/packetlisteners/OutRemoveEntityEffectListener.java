package me.deecaad.weaponmechanics.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

public class OutRemoveEntityEffectListener extends PacketAdapter {

    private static final Field idField;

    static {
        Class<?> effectPacket = ReflectionUtil.getPacketClass("PacketPlayOutRemoveEntityEffect");

        idField = ReflectionUtil.getField(effectPacket, int.class);
    }

    public OutRemoveEntityEffectListener(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.REMOVE_ENTITY_EFFECT);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacket().getIntegers().read(0) != event.getPlayer().getEntityId())
            return;

        EntityWrapper entity = WeaponMechanics.getEntityWrapper(event.getPlayer());

        if (!entity.getMainHandData().getZoomData().hasZoomNightVision() && !entity.getOffHandData().getZoomData().hasZoomNightVision())
            return;
        if (!WeaponCompatibilityAPI.getScopeCompatibility().isRemoveNightVisionPacket(event))
            return;

        event.setCancelled(true);
    }
}
