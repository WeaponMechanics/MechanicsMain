package me.deecaad.weaponmechanics.events;

import org.bukkit.entity.Player;

public class PlayerJumpEvent extends WeaponMechanicsEvent {

    private Player player;
    private boolean doubleJump;

    /**
     * Called when player jumps.
     *
     * @param player the player used in event
     */
    public PlayerJumpEvent(Player player, boolean doubleJump) {
        this.player = player;
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return true if jump is double jump
     */
    public boolean isDoubleJump() {
        return this.doubleJump;
    }
}