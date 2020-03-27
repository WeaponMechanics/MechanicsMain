package me.deecaad.weaponmechanics.wrappers;

import org.bukkit.entity.Player;

/**
 * This class wraps an Player. Contains
 * basic information for this plugin for
 * each Player.
 */
public interface IPlayerWrapper extends IEntityWrapper {

    /**
     * This basically does same thing as {@link IEntityWrapper#getEntity()},
     * but this just returns it as player instance.
     *
     * @return the player held by player wrapper
     */
    Player getPlayer();

    /**
     * This is used to deny the weapon from going up and down constantly especially when shooting due to NBT tag updates.
     *
     * @return whether or not to deny next out going set slot packet send for player
     */
    boolean isDenyNextSetSlotPacket();

    /**
     * This is used to deny the weapon from going up and down constantly especially when shooting due to NBT tag updates.
     *
     * @param denyNext true if next server sent set slot packet for player should be denied
     */
    void setDenyNextSetSlotPacket(boolean denyNext);

    /**
     * Only used to check if inventory is open when dropping item to ground
     *
     * @return whether inventory is open or not when dropping item
     */
    boolean isInventoryOpen();

    /**
     * Only used to check if inventory is open when dropping item to ground
     *
     * @param isOpen the inventory open state
     */
    void setInventoryOpen(boolean isOpen);

    /**
     * Sets last right click time in millis to current time.
     * This should only be updated on PlayerInteractEvent.
     */
    void rightClicked();

    /**
     * This is only accurate for swords(1.8) and shields(1.9 and above).
     *
     * Otherwise the right click detection is determined by time between last right click
     * and time when this method is called.
     *
     * Right click event is only called every 195-215 millis
     * so this isn't fully accurate. Basically that means that right click
     * detection is about 4 ticks accurate.
     *
     * This also takes player's ping in account. If ping is more than 215 then
     * millis passed check is done with (player ping + 15). Otherwise millis
     * passed check is done with 215 millis.
     *
     * @return true if player may be right clicking
     */
    boolean isRightClicking();
}
