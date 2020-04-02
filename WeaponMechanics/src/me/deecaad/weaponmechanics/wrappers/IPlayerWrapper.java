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
     * Double sneak: if player STARTS sneaking two times within 500 millis.
     *
     * @return whether or not sneak was double sneak
     */
    boolean didDoubleSneak();
}
