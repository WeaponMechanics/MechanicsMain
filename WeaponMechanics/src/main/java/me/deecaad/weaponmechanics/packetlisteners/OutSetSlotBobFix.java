package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.core.utils.ReflectionUtil;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OutSetSlotBobFix extends PacketHandler implements Listener {

    // * ----- REFLECTIONS ----- * //
    private static final Field windowField;
    private static final Field slotField;
    private static final Field itemField;

    static {
        Class<?> packetClass = ReflectionUtil.getPacketClass("PacketPlayOutSetSlot");
        windowField = ReflectionUtil.getField(packetClass, int.class, 0, true);
        slotField = ReflectionUtil.getField(packetClass, int.class, ReflectionUtil.getMCVersion() >= 17 ? 2 : 1, true);
        itemField = ReflectionUtil.getField(packetClass, ReflectionUtil.getNMSClass("world.item", "ItemStack"));
    }
    // * ----- END OF REFLECTIONS ----- * //

    private final Map<Player, SimpleItemData> mainHand;
    private final Map<Player, SimpleItemData> offHand;

    public OutSetSlotBobFix(Plugin plugin) {
        super("PacketPlayOutSetSlot");
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
        if (!(humanEntity instanceof Player)) return;
        Player player = (Player) humanEntity;
        mainHand.put(player, null);
        offHand.put(player, null);
    }

    @EventHandler
    public void click(InventoryDragEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) return;
        Player player = (Player) humanEntity;
        mainHand.put(player, null);
        offHand.put(player, null);
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        mainHand.remove(event.getPlayer());
        offHand.remove(event.getPlayer());
    }

    @Override
    public void onPacket(Packet wrapper) {
        Player player = wrapper.getPlayer();

        // 0 is the player's inventory.
        if ((int) ReflectionUtil.invokeField(windowField, wrapper.getPacket()) != 0) return;

        int slotNum = (int) ReflectionUtil.invokeField(slotField, wrapper.getPacket());

        boolean mainHand = slotNum == 36 + player.getInventory().getHeldItemSlot();
        if (!mainHand && slotNum != 45) return;

        Map<Player, SimpleItemData> data = mainHand ? this.mainHand : this.offHand;

        ItemStack packetItem = CompatibilityAPI.getNBTCompatibility().getBukkitStack(ReflectionUtil.invokeField(itemField, wrapper.getPacket()));
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

        wrapper.setCancelled(true);
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
            if (itemMeta == null) return;
            if (itemMeta.hasDisplayName()) this.displayName = itemMeta.getDisplayName();
            if (itemMeta.hasLore()) this.lore = itemMeta.getLore();
            double version = CompatibilityAPI.getVersion();
            if (version >= 1.14 && itemMeta.hasCustomModelData()) this.customModelData = itemMeta.getCustomModelData();
            if (version < 1.13) {
                this.durability = itemStack.getDurability();
            } else if (itemMeta instanceof Damageable) {
                Damageable damageableItemMeta = (Damageable) itemMeta;
                if (damageableItemMeta.hasDamage()) this.durability = ((Damageable) itemMeta).getDamage();
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
