package me.deecaad.core.inventory.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public interface IWindowPageHolder {

    /**
     * Can be null if this window page holder ins't sub window page holder
     *
     * @return the page holder of this window
     */
    IWindowPageHolder getWindowPageHolder();

    /**
     * @return the list of added windows
     */
    List<IWindow> getWindows();

    /**
     * @return the amount of windows
     */
    int getWindowsAmount();

    /**
     * @param player the player's window to check
     * @return the currently open window for player
     */
    int getCurrentWindow(Player player);

    /**
     * This closes player's inventory first and then opens the new window
     *
     * @param player the player for who to open
     * @param window the window position in windows list
     */
    void openWindow(Player player, int window);

    /**
     * This closes player's inventory first and then opens next window
     *
     * @param player the player for who to open
     */
    void openNextWindow(Player player);


    /**
     * This closes player's inventory first and then opens previous window
     *
     * @param player the player for who to open
     */
    void openPreviousWindow(Player player);

    /**
     * Remember to take care of possible memory leaks in this.
     * This is also called when player changes page
     *
     * @param player    the player closing window
     * @param inventory the inventory closing
     */
    void onClose(Player player, Inventory inventory);
}