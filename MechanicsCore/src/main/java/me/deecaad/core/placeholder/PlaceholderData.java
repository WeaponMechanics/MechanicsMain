package me.deecaad.core.placeholder;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Denotes any class that has data that can be used for placeholder handlers.
 */
public interface PlaceholderData {

    @Nullable
    default Player player() {
        return null;
    }

    @Nullable
    default ItemStack item() {
        return null;
    }

    @Nullable
    default String itemTitle() {
        return null;
    }

    @Nullable
    default EquipmentSlot slot() {
        return null;
    }

    @NotNull
    Map<String, String> placeholders();

    static Builder builder() {
        return new Builder();
    }

    static PlaceholderData of(
            @Nullable Player player,
            @Nullable ItemStack item,
            @Nullable String itemTitle,
            @Nullable EquipmentSlot slot
    ) {
        return new Direct(player, item, itemTitle, slot, new HashMap<>());
    }

    static PlaceholderData of(
            @Nullable Player player,
            @Nullable ItemStack item,
            @Nullable String itemTitle,
            @Nullable EquipmentSlot slot,
            @Nullable Map<String, String> tempPlaceholders
    ) {
        return new Direct(player, item, itemTitle, slot, tempPlaceholders);
    }


    class Builder implements PlaceholderData {
        @Nullable private Player player;
        @Nullable private ItemStack item;
        @Nullable private String itemTitle;
        @Nullable private EquipmentSlot slot;
        @NotNull private Map<String, String> placeholders = new HashMap<>();

        // Setter methods for each field
        public Builder setPlayer(Player player) {
            this.player = player;
            return this;
        }

        public Builder setItem(ItemStack item) {
            this.item = item;
            return this;
        }

        public Builder setItemTitle(String itemTitle) {
            this.itemTitle = itemTitle;
            return this;
        }

        public Builder setSlot(EquipmentSlot slot) {
            this.slot = slot;
            return this;
        }

        public Builder setPlaceholder(String placeholder, String value) {
            placeholders.put(placeholder, value);
            return this;
        }

        // Implementing PlaceholderData methods
        @Override
        public Player player() {
            return player;
        }

        @Override
        public ItemStack item() {
            return item;
        }

        @Override
        public String itemTitle() {
            return itemTitle;
        }

        @Override
        public EquipmentSlot slot() {
            return slot;
        }

        @Override
        public @NotNull Map<String, String> placeholders() {
            return placeholders;
        }
    }


    record Direct(
            @Nullable Player player,
            @Nullable ItemStack item,
            @Nullable String itemTitle,
            @Nullable EquipmentSlot slot,
            @NotNull Map<String, String> placeholders
    ) implements PlaceholderData {
    }
}
