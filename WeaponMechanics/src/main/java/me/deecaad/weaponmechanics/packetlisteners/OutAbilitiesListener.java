package me.deecaad.weaponmechanics.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.scope.ScopeLevel;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.ZoomData;
import org.bukkit.plugin.Plugin;

public class OutAbilitiesListener extends PacketAdapter {

    public OutAbilitiesListener(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ABILITIES);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isPlayerTemporary())
            return;

        EntityWrapper entity = WeaponMechanics.getEntityWrapper(event.getPlayer());

        ZoomData mainZoomData = entity.getMainHandData().getZoomData();
        ZoomData offZoomData = entity.getOffHandData().getZoomData();

        // Player is not scoped in, no need to modify their FOV
        if (!mainZoomData.isZooming() && !offZoomData.isZooming())
            return;

        double zoomAmount = mainZoomData.isZooming() ? mainZoomData.getZoomAmount() : offZoomData.getZoomAmount();

        // Player is in VR (Vivecraft must be installed!)
        if (zoomAmount == 0)
            return;

        event.getPacket().getFloat().write(1, ScopeLevel.getScope(zoomAmount));
    }
}