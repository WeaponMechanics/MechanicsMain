package me.deecaad.weaponmechanics.wrappers;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;

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

    /**
     * Updates last drop weapon time
     */
    void droppedWeapon();

    /**
     * @return the last time when weapon was dropped in main hand in millis
     */
    long getLastDropWeaponTime();

    /**
     * @return the message helper for weapon info display
     */
    MessageHelper getMessageHelper();

    /**
     * Updates last ammo convert time
     */
    void convertedAmmo();

    /**
     * @return the last time ammo was converted
     */
    long getLastAmmoConvert();
}
