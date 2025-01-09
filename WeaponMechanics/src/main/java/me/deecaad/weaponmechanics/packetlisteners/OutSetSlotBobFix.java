package me.deecaad.weaponmechanics.packetlisteners;

import com.cjcrafter.foliascheduler.util.MinecraftVersions;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.deecaad.weaponmechanics.utils.CustomTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OutSetSlotBobFix implements PacketListener, Listener {

    private final Map<Player, SimpleItemData> mainHand;
    private final Map<Player, SimpleItemData> offHand;

    public OutSetSlotBobFix(Plugin plugin) {
        this.mainHand = new HashMap<>();
        this.offHand = new HashMap<>();

        // Register normal Bukkit events
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void drop(PlayerDropItemEvent event) {
        mainHand.put(event.getPlayer(), null);
    }

    @EventHandler
    public void click(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player player))
            return;

        mainHand.put(player, null);
        offHand.put(player, null);
    }

    @EventHandler
    public void click(InventoryDragEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player player))
            return;

        mainHand.put(player, null);
        offHand.put(player, null);
    }

    @EventHandler(ignoreCancelled = true)
    public void held(PlayerItemHeldEvent e) {
        mainHand.put(e.getPlayer(), null);
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        mainHand.remove(event.getPlayer());
        offHand.remove(event.getPlayer());
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!event.getPacketType().equals(PacketType.Play.Server.SET_SLOT)) {
            return;
        }

        // Make sure the player is online to try and prevent fake players
        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }

        // Wrap the outgoing SET_SLOT packet for easy reading
        WrapperPlayServerSetSlot wrapper = new WrapperPlayServerSetSlot(event);

        int windowId = wrapper.getWindowId();
        // 0 is the player's main inventory
        if (windowId != 0) {
            return;
        }

        int slotNum = wrapper.getSlot();
        boolean isMainHandSlot = slotNum == 36 + player.getInventory().getHeldItemSlot();
        boolean isOffHandSlot = (slotNum == 45);

        // We only care about main/off-hand
        if (!isMainHandSlot && !isOffHandSlot) {
            return;
        }

        Map<Player, SimpleItemData> dataMap = isMainHandSlot ? mainHand : offHand;

        // Convert the PacketEvents item to a Bukkit ItemStack
        ItemStack packetItem = SpigotConversionUtil.toBukkitItemStack(wrapper.getItem());
        if (packetItem == null) {
            // No item; remove old data and exit
            dataMap.put(player, null);
            return;
        }

        // Check the custom tag
        if (!packetItem.hasItemMeta() || !CustomTag.WEAPON_TITLE.hasString(packetItem)) {
            dataMap.put(player, null);
            return;
        }

        SimpleItemData lastData = dataMap.get(player);
        if (lastData == null) {
            // We haven't stored data for this item slot yet
            dataMap.put(player, new SimpleItemData(slotNum, packetItem));
            return;
        }

        // Check if it's different from what we have stored
        SimpleItemData newData = new SimpleItemData(slotNum, packetItem);
        if (newData.isDifferent(lastData)) {
            dataMap.put(player, newData);
            return;
        }

        // If we get here, it's the same item data as last time
        // => Cancel this packet to prevent the "bob" animation glitch
        event.setCancelled(true);
    }

    public static class SimpleItemData {

        private final int sentToSlot;
        private final Material type;
        private final int amount;
        private String displayName;
        private List<String> lore;
        private int customModelData;
        private int durability;

        public SimpleItemData(int sentToSlot, ItemStack itemStack) {
            this.sentToSlot = sentToSlot;
            this.type = itemStack.getType();
            this.amount = itemStack.getAmount();

            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null)
                return;

            if (itemMeta.hasDisplayName()) {
                this.displayName = itemMeta.getDisplayName();
            }
            if (itemMeta.hasLore()) {
                this.lore = itemMeta.getLore();
            }
            if (MinecraftVersions.VILLAGE_AND_PILLAGE.isAtLeast() && itemMeta.hasCustomModelData()) {
                this.customModelData = itemMeta.getCustomModelData();
            }

            if (itemMeta instanceof Damageable damageableItemMeta) {
                if (damageableItemMeta.hasDamage()) {
                    this.durability = damageableItemMeta.getDamage();
                }
            }
        }

        public boolean isDifferent(SimpleItemData other) {
            return sentToSlot != other.sentToSlot
                || type != other.type
                || amount != other.amount
                || customModelData != other.customModelData
                || durability != other.durability
                || !Objects.equals(displayName, other.displayName)
                || !Objects.equals(lore, other.lore);
        }
    }
}
