package me.deecaad.core.inventory.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Window implements IWindow {

    private IWindowPageHolder windowPageHolder;
    private String name;
    private final int size;
    private final Map<Integer, WindowButton> buttons;
    private final boolean cancelNonButtonSlotClicks;
    private final boolean cancelButtonSlotClicks;
    private final boolean globalWindowUsage;
    private IClose close;

    private Inventory savedGlobalWindow;

    public Window(String name, int size, Map<Integer, WindowButton> buttons, boolean cancelNonButtonSlotClicks, boolean cancelButtonSlotClicks, boolean globalWindowUsage) {
        this.name = name;
        this.size = size;
        this.buttons = buttons;
        this.cancelNonButtonSlotClicks = cancelNonButtonSlotClicks;
        this.cancelButtonSlotClicks = cancelButtonSlotClicks;
        this.globalWindowUsage = globalWindowUsage;
    }

    public void setClose(IClose close) {
        this.close = close;
    }

    @Override
    public Inventory getInventory() {
        if (isGlobalWindowUsage()) {
            if (this.savedGlobalWindow == null) {
                this.savedGlobalWindow = createWindowContents();
            }
            return this.savedGlobalWindow;
        }
        return createWindowContents();
    }

    public Inventory createWindowContents() {
        Inventory inventory = Bukkit.getServer().createInventory(this, this.size, this.name);
        Iterator<Entry<Integer, WindowButton>> buttons = getButtons().entrySet().iterator();
        while (buttons.hasNext()) {
            Entry<Integer, WindowButton> next = buttons.next();
            inventory.setItem(next.getKey(), next.getValue().getItemStack());
        }
        return inventory;
    }

    @Override
    public IWindowPageHolder getWindowPageHolder() {
        return this.windowPageHolder;
    }

    public void setWindowPageHolder(IWindowPageHolder windowPageHolder) {
        this.windowPageHolder = windowPageHolder;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public Map<Integer, WindowButton> getButtons() {
        return this.buttons;
    }

    public void putButton(int slot, WindowButton windowButton) {
        this.savedGlobalWindow = null;
        this.buttons.put(slot, windowButton);
    }

    @Override
    public WindowButton getWindowButton(int slot) {
        return this.buttons.get(slot);
    }

    @Override
    public boolean isCancelNonButtonSlotClicks() {
        return this.cancelNonButtonSlotClicks;
    }

    @Override
    public boolean isCancelButtonSlotClicks() {
        return this.cancelButtonSlotClicks;
    }

    @Override
    public boolean isGlobalWindowUsage() {
        return this.globalWindowUsage;
    }

    @Override
    public void open(Player player) {
        player.openInventory(getInventory());
    }

    @Override
    public void onClose(Player player, Inventory inventory) {
        IWindowPageHolder windowPageHolder = getWindowPageHolder();
        if (windowPageHolder != null) {
            windowPageHolder.onClose(player, inventory);
        }
        if (this.close != null) {
            this.close.onClose(player, inventory);
        }
    }
}