package me.deecaad.core.gui;

import org.bukkit.entity.Player;

public interface GUI {

    /**
     * Whether or not the GUI can be displayed to
     * the player. This may be because of Permissions,
     * Combat, etc.
     *
     * @param player The player receiving the gui
     * @return Whether or not they can see it
     */
    boolean canDisplay(Player player);

    /**
     * Should display this <code>GUI</code> to the
     * given <code>Player</code>
     *
     * @param player The player to display to
     */
    void display(Player player);

    /**
     * Should remove/close this <code>GUI</code> so
     * the user can no longer see/use it
     *
     * @param player The player who has the gui
     */
    void remove(Player player);
}
