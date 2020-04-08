package me.deecaad.core.gui._3d;

import me.deecaad.core.gui.GUI;
import org.bukkit.entity.Player;

public class SpaceGUI implements GUI {

    private SpaceGUI previous, next;
    private double radius;

    public SpaceGUI() {

    }

    public boolean hasPrevious() {
        return previous != null;
    }

    public boolean hasNext() {
        return next != null;
    }

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
