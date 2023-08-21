package me.deecaad.core.placeholder;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PlaceholderRequestEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Nullable private final Player player;
    @Nullable private final ItemStack item;
    @Nullable private final String itemTitle;
    @Nullable private final EquipmentSlot slot;

    private Map<String, Object> requests;

    public PlaceholderRequestEvent(@Nullable Player player, @Nullable ItemStack item, @Nullable String itemTitle, @Nullable EquipmentSlot slot, Map<String, Object> requests) {
        this.player = player;
        this.item = item;
        this.itemTitle = itemTitle;
        this.slot = slot;
        this.requests = requests;
    }

    public @Nullable Player getPlayer() {
        return player;
    }

    public @Nullable ItemStack getItem() {
        return item;
    }

    public @Nullable String getItemTitle() {
        return itemTitle;
    }

    public @Nullable EquipmentSlot getSlot() {
        return slot;
    }

    public Map<String, Object> getRequests() {
        return requests;
    }

    public void setRequests(Map<String, Object> requests) {
        this.requests = requests;
    }

    public void setRequest(String placeholder, Object value) {
        this.requests.put(placeholder, value);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
