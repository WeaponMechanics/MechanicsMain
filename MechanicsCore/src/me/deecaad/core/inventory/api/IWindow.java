package me.deecaad.core.inventory.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;

public interface IWindow extends InventoryHolder {

    /**
     * Can be null if window doesn't support pages
     *
     * @return the page holder of this window
     */
    IWindowPageHolder getWindowPageHolder();

    /**
     * @return the name of window
     */
    String getName();

    /**
     * Valid values are 9, 18, 27, 36, 45 and 54.
     *
     * @return the size of window
     */
    int getSize();

    /**
     * @return the contents for the window
     */
    Map<Integer, WindowButton> getButtons();

    /**
     * Used to get some specific window button at some slot.
     * This may be null.
     *
     * @param slot the slot to check
     * @return the window button of slot if found
     */
    WindowButton getWindowButton(int slot);

    /**
     * Used to prevent players from interacting in window slots which aren't buttons
     *
     * @return true if non button slot clicks are cancelled
     */
    boolean isCancelNonButtonSlotClicks();

    /**
     * Used to prevent player from interacting in ALL window slots which are buttons.
     * Button's own cancel click event denying or undenying doesn't work if this is true
     *
     * @return true if button slot clicks are cancelled
     */
    boolean isCancelButtonSlotClicks();

    /**
     * If this is false then this window is always made for some specific player.
     * When this is true all interactions with the window should be cancelled
     *
     * @return true if window is global
     */
    boolean isGlobalWindowUsage();

    /**
     * Opens this window for player
     *
     * @param player the player for who to open
     */
    void open(Player player);

    /**
     * Remember to take care of possible memory leaks in this.
     * This is also called when player changes page.
     * If getWindowPageHolder() is not null, you should also call its onClose(player) method.
     *
     * @param player    the player closing window
     * @param inventory the inventory closing
     */
    void onClose(Player player, Inventory inventory);
}