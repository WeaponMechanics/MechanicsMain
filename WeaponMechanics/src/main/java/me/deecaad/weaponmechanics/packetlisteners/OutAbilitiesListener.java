package me.deecaad.weaponmechanics.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.ZoomData;
import org.bukkit.plugin.Plugin;

public class OutAbilitiesListener extends PacketAdapter {

    public OutAbilitiesListener(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.ABILITIES);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        EntityWrapper entity = WeaponMechanics.getEntityWrapper(event.getPlayer());

        ZoomData main = entity.getMainHandData().getZoomData();
        ZoomData off = entity.getOffHandData().getZoomData();

        // Player is not scoped in, no need to modify their FOV
        if (!main.isZooming() && !off.isZooming())
            return;

        double zoomAmount = main.isZooming() ? main.getZoomAmount() : off.getZoomAmount();

        // Player is in VR (Vivecraft must be installed!)
        if (zoomAmount == 0)
            return;

        event.getPacket().getFloat().write(1, (float) zoomAmount);
    }
}