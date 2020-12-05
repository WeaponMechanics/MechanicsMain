package me.deecaad.weaponcompatibility.scope;

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
     * Only updates player's attributes for SELF, not for other players.
     * Basically this is used with packet listener to modify FOV
     *
     * @param player the player whose attributes to update
     */
    void updateAttributesFor(Player player);

    /**
     * Modifies the outgoing packet to have attribute of movement speed based on
     * player's scope level. It basically modifies FOV.
     *
     * @param packet     the packet to modify
     * @param zoomAmount the amount of zoom
     */
    void modifyUpdateAttributesPacket(Packet packet, int zoomAmount);

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
     * @param packet the remov entity effect packet
     * @return true if packet is removing night vision
     */
    boolean isRemoveNightVisionPacket(Packet packet);
}