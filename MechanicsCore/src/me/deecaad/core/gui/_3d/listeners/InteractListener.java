package me.deecaad.core.gui._3d.listeners;

import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import net.minecraft.server.v1_15_R1.PacketPlayInUseEntity;

public class InteractListener extends PacketHandler {

    public InteractListener() {
        super("PacketPlayInUseEntity");
    }

    @Override
    public void onPacket(Packet packet) {

    }
}
