package me.deecaad.weaponmechanics.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.cjcrafter.foliascheduler.util.MinecraftVersions;
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

public class OutSetSlotBobFix extends PacketAdapter implements Listener {

    private final Map<Player, SimpleItemData> mainHand;
    private final Map<Player, SimpleItemData> offHand;

    public OutSetSlotBobFix(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.SET_SLOT);
        mainHand = new HashMap<>();
        offHand = new HashMap<>();

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
    public void onPacketReceiving(PacketEvent event) {
    }

    @Override
    public void onPacketSending(PacketEvent event) {

        // Temporary players do not have inventories. This check avoids an unsupported
        // operation exception. Seems to be an issue with NPC plugins/GeyserMC.
        if (event.isPlayerTemporary())
            return;

        Player player = event.getPlayer();

        // 0 is the player's inventory.
        if (event.getPacket().getIntegers().read(0) != 0)
            return;

        int slotNum = event.getPacket().getIntegers().read(MinecraftVersions.CAVES_AND_CLIFFS_1.isAtLeast() ? 2 : 1);

        boolean mainHand = slotNum == 36 + player.getInventory().getHeldItemSlot();
        if (!mainHand && slotNum != 45)
            return;

        Map<Player, SimpleItemData> data = mainHand ? this.mainHand : this.offHand;

        ItemStack packetItem = event.getPacket().getItemModifier().read(0);
        if (!packetItem.hasItemMeta() || !CustomTag.WEAPON_TITLE.hasString(packetItem)) {
            data.put(player, null);
            return;
        }

        SimpleItemData lastData = data.get(player);
        if (lastData == null) {
            data.put(player, new SimpleItemData(slotNum, packetItem));
            return;
        }

        SimpleItemData newData = new SimpleItemData(slotNum, packetItem);
        if (newData.isDifferent(lastData)) {
            data.put(player, newData);
            return;
        }

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

            if (itemMeta.hasDisplayName())
                this.displayName = itemMeta.getDisplayName();
            if (itemMeta.hasLore())
                this.lore = itemMeta.getLore();
            if (MinecraftVersions.VILLAGE_AND_PILLAGE.isAtLeast() && itemMeta.hasCustomModelData())
                this.customModelData = itemMeta.getCustomModelData();
            if (!MinecraftVersions.UPDATE_AQUATIC.isAtLeast()) {
                this.durability = itemStack.getDurability();
            } else if (itemMeta instanceof Damageable damageableItemMeta) {
                if (damageableItemMeta.hasDamage())
                    this.durability = ((Damageable) itemMeta).getDamage();
            }
        }

        public boolean sameSlot(int newSlot) {
            return sentToSlot == newSlot;
        }

        public boolean isDifferent(SimpleItemData other) {
            return sentToSlot != other.sentToSlot || type != other.type || amount != other.amount || customModelData != other.customModelData
                || durability != other.durability || !Objects.equals(displayName, other.displayName)
                || !Objects.equals(lore, other.lore);
        }
    }
}
