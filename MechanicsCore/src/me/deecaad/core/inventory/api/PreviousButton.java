package me.deecaad.core.inventory.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PreviousButton extends WindowButton {

    public PreviousButton(IWindowPageHolder windowPageHolder, ItemStack itemStack) {
        super(itemStack, (window, event) -> windowPageHolder.openPreviousWindow((Player) event.getWhoClicked()));
    }
}