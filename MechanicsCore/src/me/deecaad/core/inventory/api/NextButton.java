package me.deecaad.core.inventory.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NextButton extends WindowButton {

    public NextButton(IWindowPageHolder windowPageHolder, ItemStack itemStack) {
        super(itemStack, (window, event) -> windowPageHolder.openNextWindow((Player) event.getWhoClicked()));
    }
}