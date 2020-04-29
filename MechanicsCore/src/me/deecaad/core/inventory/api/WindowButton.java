package me.deecaad.core.inventory.api;

import org.bukkit.inventory.ItemStack;

public class WindowButton {

    private final ItemStack itemStack;
    private IButtonListener buttonListener;

    public WindowButton(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public WindowButton(ItemStack itemStack, IButtonListener buttonListener) {
        this.itemStack = itemStack;
        this.buttonListener = buttonListener;
    }

    /**
     * Item stacks are used as icons for buttons
     *
     * @return the item stack resprenting this window button
     */
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    /**
     * Button listener will be ran when this button is clicked
     *
     * @return the button listener
     */
    public IButtonListener getButtonListener() {
        return this.buttonListener;
    }
}