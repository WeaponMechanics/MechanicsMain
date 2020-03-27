package me.deecaad.weaponmechanics.events;

import org.bukkit.entity.Player;

public class PlayerJumpEvent extends WeaponMechanicsEvent {

    private Player player;

    /**
     * Called when player jumps.
     *
     * @param player the player used in event
     */
    public PlayerJumpEvent(Player player) {
        this.player = player;
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return this.player;
    }

}