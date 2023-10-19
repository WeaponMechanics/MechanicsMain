package me.deecaad.core.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.deecaad.core.MechanicsCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MechanicsCore is built to be craftbukkit compatible to reduce support
 * requests. We do not use Paper code since we upload premium plugins on
 * Spigot. Unfortunately, this means we must find "creative" solutions when
 * using the Adventure chat API with Minecraft code.
 *
 * <p>This class adds methods to:
 * <ul>
 *     <li>Set item display name</li>
 *     <li>Set item lore</li>
 * </ul>
 */
public final class AdventureUtil {

    // 1.16+ use adventure in item lore and display name (hex code support)
    public static Field loreField;
    public static Field displayField;

    static {
        if (ReflectionUtil.getMCVersion() >= 16) { // before 1.16, hex was not supported by MC
            Class<?> c = ReflectionUtil.getCBClass("inventory.CraftMetaItem");
            loreField = ReflectionUtil.getField(c, "lore");
            displayField = ReflectionUtil.getField(c, "displayName");
        }
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private AdventureUtil() {
    }

    /**
     * Returns the display name of the item in adventure format.
     *
     * @param item The item get the name from.
     * @return The name component.
     */
    public static @NotNull Component getName(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return Component.empty();

        return ReflectionUtil.getMCVersion() < 16
                ? LegacyComponentSerializer.legacySection().deserialize(meta.getDisplayName())
                : GsonComponentSerializer.gson().deserialize((String) ReflectionUtil.invokeField(displayField, meta));

        //return CompatibilityAPI.getNBTCompatibility().getDisplayName(item);
    }

    /**
     * Sets the display name of the item. Parsed using MiniMessage.
     *
     * @param item The item to set the name.
     * @param name The value of the name.
     */
    public static void setNameUnparsed(@NotNull ItemStack item, @NotNull String name) {
        ItemMeta meta = item.getItemMeta();
        setNameUnparsed(Objects.requireNonNull(meta), name);
        item.setItemMeta(meta);
    }

    /**
     * Sets the display name of the item's meta. Parsed using MiniMessage.
     *
     * @param meta The meta to set the name.
     * @param name The value of the name.
     */
    public static void setNameUnparsed(@NotNull ItemMeta meta, @NotNull String name) {
        // <!italic> effectively strips away Minecraft's predefined formatting
        setName(meta, MechanicsCore.getPlugin().message.deserialize("<!italic>" + name));
    }

    /**
     * Sets the display name of the item.
     *
     * @param item The item to set the name.
     * @param name The value of the name.
     */
    public static void setName(@NotNull ItemStack item, @NotNull Component name) {
        ItemMeta meta = item.getItemMeta();
        setName(Objects.requireNonNull(meta), name);
        item.setItemMeta(meta);
    }

    /**
     * Sets the display name of the item's meta.
     *
     * @param meta The meta to set the name.
     * @param name The value of the name.
     */
    public static void setName(@NotNull ItemMeta meta, @NotNull Component name) {
        // before 1.16, hex was not supported
        if (ReflectionUtil.getMCVersion() < 16) {
            String str = LegacyComponentSerializer.legacySection().serialize(name);
            meta.setDisplayName(str);
        } else {
            String str = GsonComponentSerializer.gson().serialize(name);
            ReflectionUtil.setField(displayField, meta, str);
        }
    }

    @Nullable
    public static List<Component> getLore(@NotNull ItemStack item) {
        return getLore(Objects.requireNonNull(item.getItemMeta()));
    }

    @Nullable
    public static List<Component> getLore(@NotNull ItemMeta meta) {
        boolean useLegacy = ReflectionUtil.getMCVersion() < 16; // before 1.16, hex was not supported by MC

        List<String> lore = useLegacy
                ? meta.getLore()
                : (List<String>) ReflectionUtil.invokeField(loreField, meta);

        if (lore == null)
            return null;

        List<Component> components = new ArrayList<>(lore.size());
        for (String line : lore) {
            Component component = useLegacy
                    ? LegacyComponentSerializer.legacySection().deserialize(line)
                    : GsonComponentSerializer.gson().deserialize(line);
            components.add(component);
        }

        return components;
    }

    /**
     * Sets the lore of the item.
     *
     * <p>The list should be a list of strings (or any list where
     * {@link Object#toString()} is acceptable). The strings are then parsed
     * using MiniMessage, then set as the lore. This is quite slow, so consider
     * parsing the list with MiniMessage first then setting it using
     * {@link #setLore(ItemStack, List)}.
     *
     * @param item         The item to set the lore.
     * @param unparsedText The list of strings.
     */
    public static void setLoreUnparsed(@NotNull ItemStack item, @NotNull List<?> unparsedText) {
        ItemMeta meta = item.getItemMeta();
        setLoreUnparsed(Objects.requireNonNull(meta), unparsedText);
        item.setItemMeta(meta);
    }

    /**
     * Sets the lore of the item meta.
     *
     * <p>The list should be a list of strings (or any list where
     * {@link Object#toString()} is acceptable). The strings are then parsed
     * using MiniMessage, then set as the lore. This is quite slow, so consider
     * parsing the list with MiniMessage first then setting it using
     * {@link #setLore(ItemStack, List)}.
     *
     * @param meta         The item meta to set the lore.
     * @param unparsedText The list of strings.
     */
    public static void setLoreUnparsed(@NotNull ItemMeta meta, @NotNull List<?> unparsedText) {
        boolean useLegacy = ReflectionUtil.getMCVersion() < 16; // before 1.16, hex was not supported by MC

        List<String> lore = new ArrayList<>(unparsedText.size());
        for (Object obj : unparsedText) {
            // <!italic> effectively strips away Minecraft's predefined formatting
            Component component = MechanicsCore.getPlugin().message.deserialize("<!italic>" + StringUtil.colorAdventure(obj.toString()));
            String line = useLegacy
                    ? LegacyComponentSerializer.legacySection().serialize(component)
                    : GsonComponentSerializer.gson().serialize(component);
            lore.add(line);
        }

        if (useLegacy)
            meta.setLore(lore);
        else
            ReflectionUtil.setField(loreField, meta, lore);
    }

    /**
     * Sets the lore of the item.
     *
     * @param item  The item to set the lore.
     * @param lines The list of adventure components for lore.
     */
    public static void setLore(@NotNull ItemStack item, @NotNull List<Component> lines) {
        ItemMeta meta = item.getItemMeta();
        setLore(Objects.requireNonNull(meta), lines);
        item.setItemMeta(meta);
    }

    /**
     * Sets the lore of the item.
     *
     * @param meta  The item to set the lore.
     * @param lines The list of adventure components for lore.
     */
    public static void setLore(@NotNull ItemMeta meta, @NotNull List<Component> lines) {
        boolean useLegacy = ReflectionUtil.getMCVersion() < 16; // before 1.16, hex was not supported by MC

        List<String> lore = new ArrayList<>(lines.size());
        for (Component component : lines) {
            String line = useLegacy
                    ? LegacyComponentSerializer.legacySection().serialize(component)
                    : GsonComponentSerializer.gson().serialize(component);
            lore.add(line);
        }

        if (useLegacy)
            meta.setLore(lore);
        else
            ReflectionUtil.setField(loreField, meta, lore);
    }

    /**
     * Replaces any PlaceholderAPI placeholders present in the display name
     * and lore of the item. If PlaceholderAPI is not installed, this
     * method is skipped.
     *
     * @param player    The player holding the item.
     * @param itemStack The item to apply the placeholders to.
     */
    public static void updatePlaceholders(Player player, ItemStack itemStack) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
            return;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return;

        // Slow AF, but this isn't done often and there isn't really a faster way
        // (except by skipping placeholders)
        String name = GsonComponentSerializer.gson().serialize(AdventureUtil.getName(itemStack));
        name = PlaceholderAPI.setPlaceholders(player, name);
        AdventureUtil.setName(itemMeta, GsonComponentSerializer.gson().deserialize(name));

        List<Component> lore = AdventureUtil.getLore(itemMeta);
        if (lore != null && !lore.isEmpty()) {
            // Convert components to strings
            List<String> loreStrings = new ArrayList<>(lore.size());
            for (Component component : lore)
                loreStrings.add(GsonComponentSerializer.gson().serialize(component));

            // Let placeholderapi do its thing
            PlaceholderAPI.setPlaceholders(player, loreStrings);

            // convert strings back to components
            for (int i = 0; i < loreStrings.size(); i++)
                lore.set(i, GsonComponentSerializer.gson().deserialize(loreStrings.get(i)));

            AdventureUtil.setLore(itemMeta, lore);
        }

        itemStack.setItemMeta(itemMeta);
    }
}
