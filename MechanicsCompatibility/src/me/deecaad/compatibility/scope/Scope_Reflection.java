package me.deecaad.compatibility.scope;

import me.deecaad.core.packetlistener.Packet;
import org.bukkit.entity.Player;

public class Scope_Reflection implements IScopeCompatibility {

    // todo

    @Override
    public void updateAbilities(Player player) {

    }

    @Override
    public void updateAttributesFor(Player player) {

    }

    @Override
    public void modifyUpdateAttributesPacket(Packet packet, int zoomAmount) {

    }

    @Override
    public void addNightVision(Player player) {

    }

    @Override
    public void removeNightVision(Player player) {

    }

    @Override
    public boolean isRemoveNightVisionPacket(Packet packet) {
        return false;
    }
}