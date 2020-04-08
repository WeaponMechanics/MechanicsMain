package me.deecaad.core.gui._2d;

import me.deecaad.core.gui.GUI;
import org.bukkit.entity.Player;

public class InventoryGUI implements GUI {

    @Override
    public boolean canDisplay(Player player) {
        return false;
    }

    @Override
    public void display(Player player) {

    }

    @Override
    public void remove(Player player) {

    }
}
