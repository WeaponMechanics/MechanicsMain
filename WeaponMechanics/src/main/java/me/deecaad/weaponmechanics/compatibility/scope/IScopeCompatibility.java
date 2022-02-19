package me.deecaad.weaponmechanics.compatibility.scope;

import me.deecaad.core.packetlistener.Packet;
import org.bukkit.entity.Player;

public interface IScopeCompatibility {

    /**
     * Basically this is used with packet listener to modify FOV
     *
     * @param player the player whose abilities to update
     */
    void updateAbilities(Player player);

    /**
     * Sends scope packet night vision for player
     *
     * @param player the player for who to give night vision
     */
    void addNightVision(Player player);

    /**
     * This removes the scope packet night vision from player
     *
     * @param player the player from who to remove night vision
     */
    void removeNightVision(Player player);

    /**
     * @param packet the remove entity effect packet
     * @return true if packet is removing night vision
     */
    boolean isRemoveNightVisionPacket(Packet packet);
}