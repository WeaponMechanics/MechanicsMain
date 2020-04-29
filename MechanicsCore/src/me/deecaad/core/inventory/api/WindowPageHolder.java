package me.deecaad.core.inventory.api;

import me.deecaad.core.inventory.entitydata.InventoryEntityData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class WindowPageHolder implements IWindowPageHolder {

    private IWindowPageHolder windowPageHolder;
    private final List<IWindow> windows;
    private IClose close;

    public WindowPageHolder(IWindow... windows) {
        this.windows = new ArrayList<>();
        addWindows(windows);
    }

    public WindowPageHolder(List<IWindow> windows) {
        this.windows = windows;
    }

    public void setClose(IClose close) {
        this.close = close;
    }

    public void addWindows(IWindow... windows) {
        for (IWindow window : windows) {
            this.windows.add(window);
        }
    }

    public void addWindows(List<IWindow> windows) {
        this.windows.addAll(windows);
    }

    @Override
    public IWindowPageHolder getWindowPageHolder() {
        return this.windowPageHolder;
    }

    public void setWindowPageHolder(IWindowPageHolder windowPageHolder) {
        this.windowPageHolder = windowPageHolder;
    }

    @Override
    public List<IWindow> getWindows() {
        return this.windows;
    }

    @Override
    public int getWindowsAmount() {
        return this.windows.size();
    }

    @Override
    public int getCurrentWindow(Player player) {
        return InventoryEntityData.getInventoryEntityData(player).getCurrentWindow();
    }

    @Override
    public void openWindow(Player player, int window) {
        player.closeInventory();
        InventoryEntityData.getInventoryEntityData(player).setCurrentWindow(window);
        this.windows.get(window).open(player);
    }

    @Override
    public void openNextWindow(Player player) {
        int nextWindow = getCurrentWindow(player) + 1;
        if (nextWindow > getWindowsAmount() - 1) {
            nextWindow = 0;
        }
        openWindow(player, nextWindow);
    }

    @Override
    public void openPreviousWindow(Player player) {
        int previousWindow = getCurrentWindow(player) - 1;
        if (previousWindow < 0) {
            previousWindow = getWindowsAmount() - 1;
        }
        openWindow(player, previousWindow);
    }

    @Override
    public void onClose(Player player, Inventory inventory) {
        InventoryEntityData.getInventoryEntityData(player).setCurrentWindow(0);
        if (this.close != null) {
            this.close.onClose(player, inventory);
        }
    }
}