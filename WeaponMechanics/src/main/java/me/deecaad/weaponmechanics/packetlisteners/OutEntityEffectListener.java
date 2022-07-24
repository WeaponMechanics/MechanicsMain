package me.deecaad.weaponmechanics.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public class OutEntityEffectListener extends PacketAdapter {

    public OutEntityEffectListener(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EFFECT);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        int id = event.getPacket().getIntegers().read(0);

        // A negative ID doesn't happen in Vanilla Minecraft. When this is
        // negative, it means that ScopeCompatibility sent this packet.
        if (id < 0) {
            event.getPacket().getIntegers().write(0, -id);
            return;
        }

        // Only modify this packet if it is for the player it is sent to
        if (id != event.getPlayer().getEntityId())
            return;

        EntityWrapper entity = WeaponMechanics.getEntityWrapper(event.getPlayer());

        if (!entity.getMainHandData().getZoomData().hasZoomNightVision() && !entity.getOffHandData().getZoomData().hasZoomNightVision())
            return;
        if (event.getPacket().getBytes().read(0) != (byte) (PotionEffectType.NIGHT_VISION.getId() & 255))
            return;

        event.setCancelled(true);
    }
}
