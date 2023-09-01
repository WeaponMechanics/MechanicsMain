package me.deecaad.core.placeholder;

import net.kyori.adventure.text.minimessage.tag.TagPattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * This event is called whenever a {@link PlaceholderMessage} requests values
 * from a {@link PlaceholderHandler} (usually multiple placeholder handlers).
 *
 * <p>WeaponMechanicsPlus uses this event to add special formats to existing
 * placeholders, and add more placeholders.
 */
public class PlaceholderRequestEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull private final PlaceholderData data;

    public PlaceholderRequestEvent(@NotNull PlaceholderData data) {
        super(!Bukkit.isPrimaryThread());
        this.data = data;
    }

    public PlaceholderData getPlaceholderData() {
        return data;
    }

    public @Nullable Player player() {
        return data.player();
    }

    public @Nullable ItemStack item() {
        return data.item();
    }

    public @Nullable String itemTitle() {
        return data.itemTitle();
    }

    public @Nullable EquipmentSlot slot() {
        return data.slot();
    }

    public @NotNull Map<String, String> placeholders() {
        return data.placeholders();
    }

    public boolean hasPlaceholder(@TagPattern String placeholder) {
        return placeholders().containsKey(placeholder);
    }

    public String getPlaceholderValue(@TagPattern String placeholder) {
        return placeholders().get(placeholder);
    }

    public void setPlaceholder(@TagPattern String placeholder, String value) {
        placeholders().put(placeholder, value);
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
